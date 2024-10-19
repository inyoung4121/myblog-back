package in.myblog.post.dto;

import in.myblog.comment.dto.CommentListDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResponsePageDetailDTO {
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private List<String> tags;


    private String authorName;
    private List<CommentListDto> commentListDtoList;
}
