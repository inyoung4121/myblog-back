package in.myblog.post;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class VisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @Column(name = "user_agent")
    private String userAgent;
}
