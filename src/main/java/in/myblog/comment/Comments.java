package in.myblog.comment;

import in.myblog.post.domain.Posts;
import in.myblog.user.domain.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

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

    public boolean canDelete(Users user, String providedPassword) {
        if (this.author != null && this.author.equals(user)) {
            return true;
        }
        if (this.isAnonymous && this.deletePassword != null) {
            return this.deletePassword.equals(providedPassword);
        }
        return false;
    }
}