package in.myblog.post.dto;

import in.myblog.comment.dto.CommentListDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponsePageDetailDTO {
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private List<String> tags;
    private String authorName;
    private List<CommentListDto> commentListDtoList;

    public ResponsePageDetailDTO(
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int likeCount,
            String tagString,  // List<String> 대신 String으로 받기
            String authorName,
            List<CommentListDto> commentListDtoList
    ) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.tags = tagString != null ?
                Arrays.asList(tagString.split(",")) :
                new ArrayList<>();
        this.authorName = authorName;
        this.commentListDtoList = commentListDtoList;
    }
}
