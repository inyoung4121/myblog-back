package in.myblog.post.service;


import in.myblog.comment.domain.Comments;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.like.domain.Like;
import in.myblog.like.repository.LikeRepository;
import in.myblog.post.domain.Posts;
import in.myblog.post.domain.PostTags;
import in.myblog.post.domain.VisitLog;
import in.myblog.post.dto.*;
import in.myblog.post.exception.CustomPostExceptions;
import in.myblog.post.repository.PostRepository;
import in.myblog.post.repository.PostTagRespository;
import in.myblog.post.repository.TagRepository;
import in.myblog.post.repository.VisitLogRepository;
import in.myblog.post.domain.Tags;
import in.myblog.user.domain.Users;
import in.myblog.user.exception.CustomUserExceptions;
import in.myblog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRespository postTagRepository;
    private final VisitLogRepository visitLogRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;


    @Transactional
    @CachePut(value = "tags")
    public Long createPost(String title, String content, Long authorId, List<String> tags) {
        Users user = userRepository.findById(authorId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .author(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 태그 자동 생성 및 연결
        createAndConnectTags(post, tags);

        Posts savedPost = postRepository.save(post);

        return savedPost.getId();
    }


    @Transactional
    @CachePut(value = "tags")
    public Long updatePost(Long postId, String title, String content, Long authorId, List<String> tags) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        Users user = userRepository.findById(authorId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        if (!Objects.equals(user.getId(), post.getAuthor().getId())) {
            throw new CustomPostExceptions.UserMissMatchException(user.getUsername(), postId);
        }

        post.updateTitle(title).updateContent(content).updateUpdatedAt();

        // 기존 태그 연결 삭제
        postTagRepository.deleteByPostId(postId);
        post.getPostTags().clear(); // 영속성 컨텍스트에서도 제거

        // 새로운 태그 생성 및 연결 (태그가 있는 경우에만)
        if (tags != null && !tags.isEmpty()) {
            createAndConnectTags(post, tags);
        }

        // 변경 사항 저장
        Posts updatedPost = postRepository.save(post);

        return updatedPost.getId();
    }

    @Transactional
    @CachePut(value = "tags")
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> getRecentPosts(int page, int size, List<String> tags1) {

        Page<Posts> postsPage = getPostWithTag(page, size, tags1);

        List<Posts> postsWithTags = postRepository.findPostsWithTags(postsPage.getContent());
        List<Object[]> likeCounts = postRepository.countLikesForPosts(postsPage.getContent());

        Map<Long, Long> postIdToLikeCount = likeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        return postsPage.map(post -> {
            Posts postWithTags = postsWithTags.stream()
                    .filter(p -> p.getId().equals(post.getId()))
                    .findFirst()
                    .orElse(post);

            List<String> tags = postWithTags.getPostTags().stream()
                    .map(postTag -> postTag.getTag().getName())
                    .collect(Collectors.toList());

            Long likeCount = postIdToLikeCount.getOrDefault(post.getId(), 0L);

            return new PostSummaryDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getAuthor().getUsername(),
                    post.getCreatedAt(),
                    post.getContent().length() > 100
                            ? post.getContent().substring(0, 100) + "..."
                            : post.getContent(),
                    tags,
                    likeCount.intValue()
            );
        });
    }

    @Transactional
    public ResponsePageDetailDTO getPost(Long postId, String ipAddress, String userAgent) {
        Posts post = postRepository.findByIdWithAuthorAndComments(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        ResponsePageDetailDTO responsePageDetailDTO = new ResponsePageDetailDTO();
        responsePageDetailDTO.setTitle(post.getTitle());
        responsePageDetailDTO.setContent(post.getContent());
        responsePageDetailDTO.setCreatedAt(post.getCreatedAt());
        responsePageDetailDTO.setUpdatedAt(post.getUpdatedAt());
        responsePageDetailDTO.setAuthorName(post.getAuthor().getUsername());
        responsePageDetailDTO.setLikeCount(likeRepository.countByPostId(postId));

        // 태그 정보 설정
        List<String> tagNames = post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList());
        responsePageDetailDTO.setTags(tagNames);

        // 방문 로그 기록
        VisitLog visitLog = VisitLog.builder()
                .post(post)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .visitedAt(LocalDateTime.now())
                .build();
        visitLogRepository.save(visitLog);

        List<CommentListDto> commentDtos = post.getComments().stream()
                .map(this::convertToCommentListDto)
                .collect(Collectors.toList());
        responsePageDetailDTO.setCommentListDtoList(commentDtos);

        return responsePageDetailDTO;
    }

    @Transactional
    public LikeResponseDTO likePost(Long postId, String deviceId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Like like = likeRepository.findByPostIdAndDeviceId(postId, deviceId)
                .orElse(null);

        boolean isLiked;
        String message;

        if (like == null) {
            // 좋아요가 없으면 새로 생성
            like = Like.builder()
                    .post(post)
                    .deviceId(deviceId)
                    .build();
            likeRepository.save(like);
            isLiked = true;
            message = "좋아요가 추가되었습니다.";
        } else {
            // 좋아요가 이미 있으면 제거
            likeRepository.delete(like);
            isLiked = false;
            message = "좋아요가 취소되었습니다.";
        }

        postRepository.save(post);

        return LikeResponseDTO.builder()
                .postId(postId)
                .liked(isLiked)
                .totalLikes(likeRepository.countByPostId(postId))
                .message(message)
                .build();
    }

    @Transactional(readOnly = true)
    public LikeResponseDTO getLikeStatus(Long postId, String deviceId) {
        boolean isLiked = likeRepository.existsByPostIdAndDeviceId(postId, deviceId);

        return LikeResponseDTO.builder()
                .postId(postId)
                .liked(isLiked)
                .totalLikes(likeRepository.countByPostId(postId))
                .build();
    }


    private CommentListDto convertToCommentListDto(Comments comment) {
        return CommentListDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .authorName(comment.isAnonymous() ? comment.getAnonymousName() : comment.getAuthor().getUsername())
                .anonymous(comment.isAnonymous())
                .anonymousName(comment.getAnonymousName())
                .build();
    }

    private void createAndConnectTags(Posts post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;  // 태그가 없으면 메서드를 즉시 종료
        }

        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {  // 빈 문자열 태그 방지
                Tags tag = tagRepository.findByName(tagName.trim())
                        .orElseGet(() -> {
                            Tags newTag = Tags.builder()
                                    .name(tagName.trim())
                                    .build();
                            return tagRepository.save(newTag);
                        });

                PostTags postTag = PostTags.builder()
                        .tag(tag)
                        .createdAt(LocalDateTime.now())
                        .build();
                post.addPostTag(postTag);
            }
        }
    }

    private Page<Posts> getPostWithTag(int page, int size, List<String> tags){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (tags != null && !tags.isEmpty()) {
            return postRepository.findByTagsIn(tags, pageable);
        } else {
            return postRepository.findAll(pageable);
        }
    }
}