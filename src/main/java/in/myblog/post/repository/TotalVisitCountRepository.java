package in.myblog.post.repository;

import in.myblog.post.domain.TotalVisitCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TotalVisitCountRepository extends JpaRepository<TotalVisitCount, Long> {
    Optional<TotalVisitCount> findByDate(LocalDate date);
}
