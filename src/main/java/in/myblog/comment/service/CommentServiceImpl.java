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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<CommentListDto> getCommentsByPostId(Long postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(postId));

        List<Comments> comments = commentRepository.findByPostOrderByCreatedAtDesc(post);
        return comments.stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto createComment(CommentDto commentDto) {
        Posts post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new CustomPostExceptions.PostNotFoundException(commentDto.getPostId()));

        Comments comment = new Comments();
        comment.updateContent(commentDto.getContent());
        comment.updatePost(post);
        comment.setAnonymous(commentDto.isAnonymous());

        if (commentDto.isAnonymous()) {
            comment.setAnonymousInfo(commentDto.getAnonymousName(), commentDto.getDeletePassword());
        } else {
            Users user = userRepository.findById(commentDto.getUserId())
                    .orElseThrow(() -> new CustomUserExceptions.UserNotFoundException("사용자를 찾을 수 없습니다"));
            comment.updateAuthor(user);
        }

        Comments savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(Long commentId, CommentDto commentDto, Long userId, String password) {
        Comments comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomCommentExceptions.CommentNotFoundException(commentId));

        Users user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomUserExceptions.UserNotFoundException("사용자를 찾을 수 없습니다"));
        }

        AccessResult result = comment.canAccess(user, password);
        switch (result) {
            case ALLOWED:
                comment.updateContent(commentDto.getContent());
                Comments updatedComment = commentRepository.save(comment);
                return convertToDto(updatedComment);
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
                    .orElseThrow(() -> new CustomUserExceptions.UserNotFoundException("사용자를 찾을 수 없습니다"));
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


    private CommentDto convertToDto(Comments comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .userId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .isAnonymous(comment.isAnonymous())
                .anonymousName(comment.getAnonymousName())
                .build();
    }

    private CommentListDto convertToListDto(Comments comment) {
        return CommentListDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null)
                .isAnonymous(comment.isAnonymous())
                .anonymousName(comment.getAnonymousName())
                .build();
    }
}
