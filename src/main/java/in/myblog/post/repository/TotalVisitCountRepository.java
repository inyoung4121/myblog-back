package in.myblog.post.repository;

import in.myblog.post.domain.TotalVisitCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TotalVisitCountRepository extends JpaRepository<TotalVisitCount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM TotalVisitCount d WHERE d.date = :date")
    Optional<TotalVisitCount> findByDateForUpdate(@Param("date") LocalDate date);

    Optional<TotalVisitCount> findByDate(LocalDate date);

    @Query("SELECT SUM(d.count) FROM TotalVisitCount d")
    Long getTotalViewCount();
}
