package in.myblog.post.service;

import in.myblog.post.domain.TotalVisitCount;
import in.myblog.post.repository.TotalVisitCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService{
    private final TotalVisitCountRepository totalVisitCountRepository;

    @Transactional
    @CacheEvict(value = "visitorCounts", allEntries = true)
    public void incrementVisitCount() {
        LocalDate today = LocalDate.now();
        TotalVisitCount visitCount = totalVisitCountRepository.findByDate(today)
                .orElseGet(() -> {
                    TotalVisitCount newCount = new TotalVisitCount(today, 0L);
                    return totalVisitCountRepository.save(newCount);
                });

        visitCount.incrementCount();
    }
}
