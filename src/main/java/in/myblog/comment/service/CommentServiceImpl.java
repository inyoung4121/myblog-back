package in.myblog.comment.service;

import in.myblog.comment.domain.Comments;
import in.myblog.comment.domain.AccessResult;
import in.myblog.comment.dto.CommentDto;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.comment.exception.CustomCommentExceptions;
import in.myblog.comment.repository.CommentRepository;
import in.myblog.post.domain.Posts;
import in.myblog.post.exception.CustomPostExceptions;
import in.myblog.post.repository.PostRepository;
import in.myblog.user.domain.Users;
import in.myblog.user.exception.CustomUserExceptions;
import in.myblog.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public List<CommentListDto> getCommentsByPostId(Long postId) {
        Posts post = getPostReference(postId);

        List<Comments> comments = commentRepository.findByPostOrderByCreatedAtDesc(post);
        return comments.stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentListDto createComment(CommentDto commentDto) {
        Posts post = getPostReference(commentDto.getPostId());

        Comments comment = Comments.builder()
                .content(commentDto.getContent())
                .post(post)
                .isAnonymous(commentDto.isAnonymous())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (commentDto.isAnonymous()) {
            comment.setAnonymousInfo(commentDto.getAnonymousName(), commentDto.getDeletePassword());
        } else {
            Users user = userRepository.findById(commentDto.getUserId())
                    .orElseThrow(CustomUserExceptions.UserNotFoundException::new);
            comment.updateAuthor(user);
        }

        Comments savedComment = commentRepository.save(comment);
        return convertToListDto(savedComment);
    }

    @Transactional
    public CommentListDto updateComment(Long commentId, CommentDto commentDto, Long userId, String password) {
        Comments comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomCommentExceptions.CommentNotFoundException(commentId));

        Users user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(CustomUserExceptions.UserNotFoundException::new);
        }

        AccessResult result = comment.canAccess(user, password);
        switch (result) {
            case ALLOWED:
                comment.updateContent(commentDto.getContent());
                Comments updatedComment = commentRepository.save(comment);
                return convertToListDto(updatedComment);
            case NOT_AUTHOR, NOT_ALLOWED:
                throw new CustomCommentExceptions.CommentAccessDeniedException();
            case INVALID_PASSWORD:
                throw new CustomCommentExceptions.InvalidPasswordException();
            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, String deletePassword) {
        Comments comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomCommentExceptions.CommentNotFoundException(commentId));

        Users user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(CustomUserExceptions.UserNotFoundException::new);
        }

        AccessResult result = comment.canAccess(user, deletePassword);
        switch (result) {
            case ALLOWED:
                commentRepository.delete(comment);
                break;
            case NOT_AUTHOR, NOT_ALLOWED:
                throw new CustomCommentExceptions.CommentAccessDeniedException();
            case INVALID_PASSWORD:
                throw new CustomCommentExceptions.InvalidPasswordException();
        }
    }

    private CommentListDto convertToListDto(Comments comment) {
        return CommentListDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null)
                .anonymous(comment.isAnonymous())
                .anonymousName(comment.getAnonymousName())
                .build();
    }

    private Posts getPostReference(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomPostExceptions.PostNotFoundException(postId);
        }
        return entityManager.getReference(Posts.class, postId);
    }
}
