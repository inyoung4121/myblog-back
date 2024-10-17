package in.myblog.comment.service;

import in.myblog.comment.domain.Comments;
import in.myblog.comment.dto.CommentDto;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.post.domain.Posts;
import in.myblog.user.domain.Users;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentService {
    List<CommentListDto> getCommentsByPostId(Long postId);
    CommentDto createComment(CommentDto commentDto);
    CommentDto updateComment(Long commentId, CommentDto commentDto, Long userId,String password);
    void deleteComment(Long commentId, Long userId, String deletePassword);

}
