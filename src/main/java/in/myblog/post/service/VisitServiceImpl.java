package in.myblog.post.service;

import in.myblog.post.domain.TotalVisitCount;
import in.myblog.post.repository.TotalVisitCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService{
    private final TotalVisitCountRepository totalVisitCountRepository;

    @Transactional
    @CachePut(value = "visitorCounts")
    public void incrementVisitCount() {
        LocalDate today = LocalDate.now();
        TotalVisitCount visitCount = totalVisitCountRepository.findByDate(today)
                .orElse(new TotalVisitCount(today, 0L));

        visitCount.incrementCount();
        totalVisitCountRepository.save(visitCount);
    }
}
