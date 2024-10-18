package in.myblog.comment.domain;

import in.myblog.post.domain.Posts;
import in.myblog.user.domain.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users author;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "anonymous_name")
    private String anonymousName;

    @Column(name = "delete_password")
    private String deletePassword;

    public AccessResult canAccess(Users user, String providedPassword) {
        if (this.author != null) {
            return this.author.equals(user) ? AccessResult.ALLOWED : AccessResult.NOT_AUTHOR;
        }
        if (this.isAnonymous && this.deletePassword != null) {
            return this.deletePassword.equals(providedPassword) ? AccessResult.ALLOWED : AccessResult.INVALID_PASSWORD;
        }
        return AccessResult.NOT_ALLOWED;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePost(Posts post) {
        this.post = post;
    }

    public void updateAuthor(Users author) {
        this.author = author;
    }

    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public void setAnonymousInfo(String anonymousName, String deletePassword) {
        this.anonymousName = anonymousName;
        this.deletePassword = deletePassword;
    }
}