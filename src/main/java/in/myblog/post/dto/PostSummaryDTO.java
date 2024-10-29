package in.myblog.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PostSummaryDTO {
    private Long id;
    private String title;
    private String authorName;
    private LocalDateTime createdAt;
    private String content;
    private List<String> tags;
    private int likeCount;

    public PostSummaryDTO(Long id, String title, String authorName,
                          LocalDateTime createdAt, String content,
                          String tagString, int likeCount) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.content = processContent(content);
        this.tags = tagString != null ?
                Arrays.asList(tagString.split(",")) :
                new ArrayList<>();
        this.likeCount = likeCount;
    }

    private String processContent(String content) {
        if (content != null) {
            content = content
                    .replaceAll("<img[^>]*(?:>|\\.{3}|\\s|$)", "")
                    .replaceAll("!\\[[^\\]]*\\]\\([^)]+\\)", "")
                    .replaceAll("https?://[^\\s]{20,}", "")
                    .trim();

            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
        }
        return content;
    }
}
