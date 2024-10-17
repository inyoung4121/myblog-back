package in.myblog.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private boolean isAnonymous;
    private String anonymousName;
    private String deletePassword;
}
