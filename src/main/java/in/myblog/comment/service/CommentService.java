package in.myblog.comment.service;

import in.myblog.comment.dto.CommentDto;
import in.myblog.comment.dto.CommentListDto;

import java.util.List;

public interface CommentService {
    List<CommentListDto> getCommentsByPostId(Long postId);
    CommentListDto createComment(CommentDto commentDto);
    CommentListDto updateComment(Long commentId, CommentDto commentDto, Long userId,String password);
    void deleteComment(Long commentId, Long userId, String deletePassword);

}
