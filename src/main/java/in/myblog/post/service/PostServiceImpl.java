package in.myblog.post.service;


import in.myblog.comment.domain.Comments;
import in.myblog.comment.dto.CommentListDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRespository postTagRepository;
    private final VisitLogRepository visitLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseCreatePostDTO createPost(String title, String content, Long authorId, List<String> tags) {
        Users user = userRepository.findById(authorId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        Posts post = Posts
                .builder()
                .title(title)
                .content(content)
                .author(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Posts savedPost = postRepository.save(post);

        // 태그 자동 생성 및 연결
        createAndConnectTags(savedPost, tags);


        ResponseCreatePostDTO responseCreatePostDTO = new ResponseCreatePostDTO();
        return responseCreatePostDTO.toResponseCreatePostDTO(savedPost);
    }


    @Transactional
    public ResponseUpdatePostDTO updatePost(Long postId, String title, String content, Long authorId, List<String> tags) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        Users user = userRepository.findById(authorId).orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        if (Objects.equals(user.getId(), post.getAuthor().getId())) {
            post.updateTitle(title).updateContent(content).updateUpdatedAt();
        } else {
            throw new CustomPostExceptions.UserMissMatchException(user.getUsername(),postId);
        }

        // 기존 태그 연결 삭제
        postTagRepository.deleteByPostId(postId);

        // 새로운 태그 생성 및 연결
        createAndConnectTags(post, tags);

        ResponseUpdatePostDTO responseUpdatePostDTO = new ResponseUpdatePostDTO();
        return responseUpdatePostDTO.fromPost(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> getRecentPosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Posts> postsPage = postRepository.findAllPosts(pageRequest);

        List<Posts> postsWithTags = postRepository.findPostsWithTags(postsPage.getContent());

        return postsPage.map(post -> {
            Posts postWithTags = postsWithTags.stream()
                    .filter(p -> p.getId().equals(post.getId()))
                    .findFirst()
                    .orElse(post);

            List<String> tags = postWithTags.getPostTags().stream()
                    .map(postTag -> postTag.getTag().getName())
                    .collect(Collectors.toList());

            return new PostSummaryDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getAuthor().getUsername(),
                    post.getCreatedAt(),
                    post.getContent().length() > 100
                            ? post.getContent().substring(0, 100) + "..."
                            : post.getContent(),
                    tags
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
        for (String tagName : tagNames) {
            Tags tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tags newTag = Tags.builder()
                                .name(tagName)
                                .build();
                        return tagRepository.save(newTag);
                    });

            PostTags postTag = PostTags.builder()
                    .post(post)
                    .tag(tag)
                    .createdAt(LocalDateTime.now())
                    .build();
            postTagRepository.save(postTag);
        }
    }
}