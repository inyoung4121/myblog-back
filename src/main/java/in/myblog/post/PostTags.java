package in.myblog.post;

import in.myblog.tag.Tags;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PostTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tags tag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}