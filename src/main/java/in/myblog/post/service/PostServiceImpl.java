package in.myblog.post.service;


import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import in.myblog.comment.domain.Comments;
import in.myblog.comment.domain.QComments;
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
import lombok.extern.slf4j.Slf4j;
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

import static com.querydsl.core.types.Projections.constructor;
import static in.myblog.comment.domain.QComments.comments;
import static in.myblog.like.domain.QLike.like;
import static in.myblog.post.domain.QPostTags.postTags;
import static in.myblog.post.domain.QPosts.posts;
import static in.myblog.post.domain.QTags.tags;
import static in.myblog.user.domain.QUsers.users;

@Slf4j
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
        QTags tag = QTags.tags;

        // DTO로 직접 조회
        JPAQuery<PostSummaryDTO> query = queryFactory
                .select(constructor(PostSummaryDTO.class,
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

    @Transactional
    public ResponsePageDetailDTO getPost(Long postId, String ipAddress, String userAgent) {
        QUsers postAuthor = new QUsers("postAuthor");
        QUsers commentAuthor = new QUsers("commentAuthor");
        QComments comments = QComments.comments;
        QPostTags postTags = QPostTags.postTags;
        QTags tags = QTags.tags;

        // DTO로 직접 조회
        JPAQuery<ResponsePageDetailDTO> query = queryFactory
                .select(constructor(ResponsePageDetailDTO.class,
                        posts.title,
                        posts.content,
                        posts.createdAt,
                        posts.updatedAt,
                        JPAExpressions.select(like.count().intValue())
                                .from(like)
                                .where(like.post.id.eq(posts.id)),
                        Expressions.stringTemplate(
                                "GROUP_CONCAT({0})",
                                tags.name
                        ),
                        postAuthor.username,
                        GroupBy.list(constructor(CommentListDto.class,
                                comments.id,
                                comments.content,
                                comments.createdAt,
                                comments.updatedAt,
                                commentAuthor.username,
                                comments.isAnonymous,
                                comments.anonymousName
                        ))
                ))
                .from(posts)
                .leftJoin(posts.author, postAuthor)
                .leftJoin(posts.comments, comments)
                .leftJoin(comments.author, commentAuthor)
                .leftJoin(posts.postTags, postTags)
                .leftJoin(postTags.tag, tags)
                .where(posts.id.eq(postId))
                .groupBy(posts.id, postAuthor.username, posts.createdAt, posts.updatedAt, posts.title, posts.content)
                .orderBy(comments.createdAt.desc());

        ResponsePageDetailDTO result = Optional.ofNullable(query.fetchOne())
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        // 방문 로그 기록
        saveVisitLog(postId, ipAddress, userAgent);

        return result;
    }

    private void saveVisitLog(Long postId, String ipAddress, String userAgent) {
        VisitLog visitLog = VisitLog.builder()
                .post(Posts.builder().id(postId).build())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .visitedAt(LocalDateTime.now())
                .build();
        visitLogRepository.save(visitLog);
    }

    @Transactional
    public LikeResponseDTO likePost(Long postId, String deviceId) {

        if (!postRepository.existsById(postId)) {
            throw new CustomPostExceptions.PostNotFoundException(postId);
        }

        // 좋아요 존재 여부 확인
        Optional<Like> existingLike = postRepository.findLikeByPostIdAndDeviceId(postId, deviceId);
        long likeCheckTime = System.currentTimeMillis();

        boolean isLiked;
        String message;

        if (existingLike.isPresent()) {
            // 좋아요 삭제
            likeRepository.deleteById(existingLike.get().getId());
            isLiked = false;
            message = "좋아요가 취소되었습니다.";
        } else {
            // 새 좋아요 추가
            Like newLike = Like.builder()
                    .post(Posts.builder().id(postId).build())
                    .deviceId(deviceId)
                    .build();
            likeRepository.save(newLike);
            isLiked = true;
            message = "좋아요가 추가되었습니다.";
        }

        long totalLikes = postRepository.countLikesByPostId(postId);

        LikeResponseDTO response = LikeResponseDTO.builder()
                .postId(postId)
                .liked(isLiked)
                .totalLikes(totalLikes)
                .message(message)
                .build();

        return response;
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
}