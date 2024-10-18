package in.myblog.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostSummaryDTO {
    private Long id;
    private String title;
    private String authorName;
    private LocalDateTime createdAt;
    private String contentPreview;
    private List<String> tags;
    private int likeCount;

    public PostSummaryDTO(Long id, String title, String authorName, LocalDateTime createdAt, String contentPreview, List<String> tags, int likeCount) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.contentPreview = contentPreview;
        this.tags = tags;
        this.likeCount = likeCount;
    }

}
