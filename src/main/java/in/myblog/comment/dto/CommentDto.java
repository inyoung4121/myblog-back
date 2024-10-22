package in.myblog.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class CommentDto {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;

    @JsonProperty("anonymous")
    private boolean anonymous;

    private String anonymousName;
    private String deletePassword;
}
