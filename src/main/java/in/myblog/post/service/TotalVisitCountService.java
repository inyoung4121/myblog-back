package in.myblog.post.service;

import in.myblog.post.domain.TotalVisitCount;
import in.myblog.post.repository.TotalVisitCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class TotalVisitCountService {
    private final TotalVisitCountRepository totalVisitCountRepository;

    private void incrementDailyViewCount() {
        LocalDate today = LocalDate.now();
        TotalVisitCount dailyViewCount = totalVisitCountRepository.findByDateForUpdate(today)
                .orElseGet(() -> new TotalVisitCount(today, 0L));
        dailyViewCount.increment();
    }

    public Long getTotalViewCount() {
        return totalVisitCountRepository.getTotalViewCount();
    }

    public Long getDailyViewCount(LocalDate date) {
        return totalVisitCountRepository.findByDate(date)
                .map(TotalVisitCount::getCount)
                .orElse(0L);
    }
}
