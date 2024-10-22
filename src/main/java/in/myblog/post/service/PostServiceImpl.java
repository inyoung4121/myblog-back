package in.myblog.post.service;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import in.myblog.comment.domain.Comments;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.like.domain.Like;
import in.myblog.like.domain.QLike;
import in.myblog.like.repository.LikeRepository;
import in.myblog.post.domain.*;
import in.myblog.post.dto.*;
import in.myblog.post.exception.CustomPostExceptions;
import in.myblog.post.repository.PostRepository;
import in.myblog.post.repository.PostTagRespository;
import in.myblog.post.repository.TagRepository;
import in.myblog.post.repository.VisitLogRepository;
import in.myblog.user.domain.QUsers;
import in.myblog.user.domain.Users;
import in.myblog.user.exception.CustomUserExceptions;
import in.myblog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;


@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRespository postTagRepository;
    private final VisitLogRepository visitLogRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final JPAQueryFactory queryFactory;


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
    public Page<PostSummaryDTO> getRecentPosts(int page, int size, List<String> tags) {
        QPosts posts = QPosts.posts;
        QPostTags postTags = QPostTags.postTags;
        QTags tag = QTags.tags;
        QUsers users = QUsers.users;
        QLike like = QLike.like;

        // DTO로 직접 조회
        JPAQuery<PostSummaryDTO> query = queryFactory
                .select(Projections.constructor(PostSummaryDTO.class,
                        posts.id,
                        posts.title,
                        users.username,
                        posts.createdAt,
                        Expressions.stringTemplate(
                                "CASE WHEN LENGTH({0}) > 100 THEN SUBSTRING({0}, 1, 100) ELSE {0} END",
                                posts.content
                        ),
                        // GROUP_CONCAT으로 태그들을 하나의 문자열로 모음
                        Expressions.stringTemplate(
                                "GROUP_CONCAT({0})",
                                tag.name
                        ),
                        posts.likes.size()
                ))
                .from(posts)
                .leftJoin(posts.author, users)
                .leftJoin(posts.postTags, postTags)
                .leftJoin(postTags.tag, tag);

        // 태그 필터링
        if (tags != null && !tags.isEmpty()) {
            query.where(posts.id.in(
                    JPAExpressions
                            .select(postTags.post.id)
                            .from(postTags)
                            .join(postTags.tag, tag)
                            .where(tag.name.in(tags))
            ));
        }

        // 그룹화 필요 (GROUP_CONCAT 때문에)
        query.groupBy(
                posts.id,
                posts.title,
                users.username,
                posts.createdAt,
                posts.content,
                posts.likes.size()
        );

        // 정렬
        query.orderBy(posts.createdAt.desc());

        // 카운트 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(posts.countDistinct())
                .from(posts)
                .leftJoin(posts.postTags, postTags)
                .leftJoin(postTags.tag, tag);

        if (tags != null && !tags.isEmpty()) {
            countQuery.where(posts.id.in(
                    JPAExpressions
                            .select(postTags.post.id)
                            .from(postTags)
                            .join(postTags.tag, tag)
                            .where(tag.name.in(tags))
            ));
        }

        Pageable pageable = PageRequest.of(page, size);

        return PageableExecutionUtils.getPage(
                query
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch(),
                pageable,
                countQuery::fetchOne
        );
    }

    // 태그 조건을 별도 메소드로 분리
    private BooleanExpression tagCondition(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        QPosts posts = QPosts.posts;
        QPostTags postTag = QPostTags.postTags;
        QTags tag = QTags.tags;

        return posts.id.in(
                JPAExpressions
                        .select(postTag.post.id)
                        .from(postTag)
                        .join(postTag.tag, tag)
                        .where(tag.name.in(tags))
        );
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