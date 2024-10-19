package in.myblog.post.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "total_visit_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TotalVisitCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long count;

    public TotalVisitCount(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }
}
