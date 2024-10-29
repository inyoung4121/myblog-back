package in.myblog.post.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import in.myblog.comment.domain.QComments;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.comment.repository.CommentRepository;
import in.myblog.like.domain.Like;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.core.types.Projections.constructor;
import static in.myblog.like.domain.QLike.like;
import static in.myblog.post.domain.QPostTags.postTags;
import static in.myblog.post.domain.QPosts.posts;
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
    private final CommentRepository commentRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Long createPost(String title, String content, Long authorId, List<String> tags) {
        Users user = userRepository.findById(authorId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        Posts post = Posts.builder()
                .title(title)
                .content(content)  // 이미 이미지 URL이 포함된 content
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
    @CacheEvict(value = "tags", allEntries = true)
    public Long updatePost(Long postId, String title, String content, Long authorId, List<String> tags) {
        // 1. 게시물과 작성자 검증
        Posts post = validatePostAndAuthor(postId, authorId);

        // 2. 삭제될 태그 ID 미리 조회
        List<Long> tagIds = getTagIdsFromPost(post);

        // 3. 게시물 기본 정보 업데이트
        // 기존 이미지와 새 이미지가 모두 포함된 content
        post.updateTitle(title)
                .updateContent(content)
                .updateUpdatedAt();

        // 4. 태그 업데이트
        updatePostTags(post, tags);

        // 5. 고아 태그 정리
        cleanupOrphanedTags(tagIds);

        // 6. 저장 및 반환
        return postRepository.save(post).getId();
    }

    @Transactional
    public void deletePost(Long postId, Long authorId) {
        // 1. 게시물과 작성자 검증
        Posts post = validatePostAndAuthor(postId, authorId);

        // 2. 삭제될 태그 ID 미리 조회
        List<Long> tagIds = getTagIdsFromPost(post);

        // 3. S3에서 이미지 삭제
        deleteImagesFromContent(post.getContent());

        // 4. 연관 데이터 삭제
        deleteAssociatedData(post);

        // 5. 게시물 삭제
        postRepository.delete(post);

        // 6. 고아 태그 정리
        cleanupOrphanedTags(tagIds);
    }

    // S3에 이미지 업로드
    public String uploadImage(MultipartFile image) {
        try {
            // 파일 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());

            // 파일 이름 생성
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String key = "posts/" + UUID.randomUUID().toString() + extension;

            // S3에 업로드
            amazonS3.putObject(new PutObjectRequest(
                    bucket,
                    key,
                    image.getInputStream(),
                    metadata
            ));

            // URL 반환
            return amazonS3.getUrl(bucket, key).toString();
        } catch (IOException e) {
            log.error("Failed to upload image to S3: {}", e.getMessage());
            throw new CustomPostExceptions.ImageUploadFailedException("Failed to upload image to S3", e);
        }
    }

    // S3에서 이미지 삭제
    private void deleteImagesFromContent(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<img[^>]+src=\"([^\"]+)\"[^>]*>");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            // S3 URL에서 키 추출
            if (imageUrl.contains(bucket + ".s3.")) {
                try {
                    String key = imageUrl.substring(imageUrl.indexOf(bucket) + bucket.length() + 1);
                    // URL 디코딩이 필요할 수 있음
                    key = java.net.URLDecoder.decode(key, "UTF-8");
                    amazonS3.deleteObject(bucket, key);
                } catch (Exception e) {
                    log.error("Failed to delete image from S3: {}", e.getMessage());
                }
            }
        }
    }

    private Posts validatePostAndAuthor(Long postId, Long authorId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        Users user = userRepository.findById(authorId)
                .orElseThrow(CustomUserExceptions.UserNotFoundException::new);

        if (!Objects.equals(user.getId(), post.getAuthor().getId())) {
            throw new CustomPostExceptions.UserMissMatchException(postId);
        }

        return post;
    }

    private void updatePostTags(Posts post, List<String> newTags) {
        // 기존 태그 연결 제거
        postTagRepository.deleteByPostId(post.getId());
        post.getPostTags().clear();

        // 새로운 태그 생성 및 연결
        if (newTags != null && !newTags.isEmpty()) {
            createAndConnectTags(post, newTags);
        }
    }

    private List<Long> getTagIdsFromPost(Posts post) {
        return postTagRepository.findByPostId(post.getId())
                .stream()
                .map(postTag -> postTag.getTag().getId())
                .collect(Collectors.toList());
    }

    private void deleteAssociatedData(Posts post) {
        try {
            Long postId = post.getId();
            likeRepository.deleteByPostId(postId);
            commentRepository.deleteByPostId(postId);
            postTagRepository.deleteByPostId(postId);
        } catch (Exception e) {
            log.error("Failed to delete associated data for post {}: {}", post.getId(), e.getMessage(), e);
            throw new CustomPostExceptions.PostDeleteFailedException();
        }
    }

    private void cleanupOrphanedTags(List<Long> tagIds) {
        if (!tagIds.isEmpty()) {
            try {
                cleanupUnusedTags(tagIds);
            } catch (Exception e) {
                log.warn("Failed to cleanup orphaned tags: {}", e.getMessage(), e);
                // 태그 정리 실패는 전체 작업을 실패시키지 않도록 함
            }
        }
    }

    private void cleanupUnusedTags(List<Long> tagIds) {
        for (Long tagId : tagIds) {
            // 해당 태그를 사용하는 다른 게시글이 있는지 확인
            long postCount = postTagRepository.countByTagId(tagId);
            if (postCount == 0) {
                tagRepository.deleteById(tagId);
                log.debug("Deleted unused tag with id: {}", tagId);
            }
        }
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
        QPostTags postTags = QPostTags.postTags;
        QTags tags = QTags.tags;

        JPAQuery<Tuple> postQuery = queryFactory
                .select(
                        posts.title,
                        posts.content,
                        posts.createdAt,
                        posts.updatedAt,
                        JPAExpressions.select(like.count().intValue())
                                .from(like)
                                .where(like.post.id.eq(posts.id)),
                        postAuthor.username,
                        tags.name
                )
                .from(posts)
                .leftJoin(posts.author, postAuthor)
                .leftJoin(posts.postTags, postTags)
                .leftJoin(postTags.tag, tags)
                .where(posts.id.eq(postId));

        List<Tuple> postResults = postQuery.fetch();

        if (postResults.isEmpty()) {
            throw new CustomPostExceptions.PostNotFoundException(postId);
        }

        // 2. 댓글 목록 별도 조회
        QUsers commentAuthor = new QUsers("commentAuthor");
        QComments comments = QComments.comments;

        List<CommentListDto> commentsList = queryFactory
                .select(constructor(CommentListDto.class,
                        comments.id,
                        comments.content,
                        comments.createdAt,
                        comments.updatedAt,
                        commentAuthor.username,
                        comments.isAnonymous,
                        comments.anonymousName
                ))
                .from(comments)
                .leftJoin(comments.author, commentAuthor)
                .where(comments.post.id.eq(postId))
                .orderBy(comments.createdAt.desc())
                .fetch();

        // 3. 결과 조합
        Tuple firstRow = postResults.get(0);
        Set<String> tagNames = postResults.stream()
                .map(tuple -> tuple.get(tags.name))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        ResponsePageDetailDTO result = new ResponsePageDetailDTO(
                firstRow.get(posts.title),
                firstRow.get(posts.content),
                firstRow.get(posts.createdAt),
                firstRow.get(posts.updatedAt),
                firstRow.get(JPAExpressions.select(like.count().intValue())
                        .from(like)
                        .where(like.post.id.eq(posts.id))),
                String.join(",", tagNames),
                firstRow.get(postAuthor.username),
                commentsList
        );

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