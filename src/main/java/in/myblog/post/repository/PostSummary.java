package in.myblog.post.repository;

import java.time.LocalDateTime;

public interface PostSummary {
    Long getId();
    String getTitle();
    String getAuthorName();
    LocalDateTime getCreatedAt();
    String getContentPreview();
}
