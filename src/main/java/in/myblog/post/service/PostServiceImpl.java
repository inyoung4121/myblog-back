package in.myblog.post.service;


import in.myblog.post.domain.Posts;
import in.myblog.post.domain.PostTags;
import in.myblog.post.domain.VisitLog;
import in.myblog.post.repository.PostSummary;
import in.myblog.post.dto.ResponseCreatePostDTO;
import in.myblog.post.dto.ResponsePagedPostsDTO;
import in.myblog.post.dto.ResponseUpdatePostDTO;
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
    public ResponsePagedPostsDTO getRecentPosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PostSummary> postsPage = postRepository.findAllPostSummaries(pageRequest);
        return new ResponsePagedPostsDTO(postsPage);
    }

    @Transactional
    public Posts getPost(Long postId, String ipAddress, String userAgent) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        // 방문 로그 기록
        VisitLog visitLog = VisitLog.builder()
                .post(post)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .visitedAt(LocalDateTime.now())
                .build();
        visitLogRepository.save(visitLog);

        return post;
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