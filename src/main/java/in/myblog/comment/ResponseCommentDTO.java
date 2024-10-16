package in.myblog.comment;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseCommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private boolean isAnonymous;
    private String anonymousName;

    public static ResponseCommentDTO fromComment(Comments comment) {
        ResponseCommentDTO dto = new ResponseCommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setAnonymous(comment.isAnonymous());

        if (comment.isAnonymous()) {
            dto.setAnonymousName(comment.getAnonymousName());
        } else if (comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getUsername());
        }

        return dto;
    }
}