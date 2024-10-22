package in.myblog.sidebar;

import in.myblog.post.domain.Tags;
import in.myblog.post.domain.TotalVisitCount;
import in.myblog.post.repository.TagRepository;
import in.myblog.post.repository.TotalVisitCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SidebarService {

    private final TotalVisitCountRepository totalVisitCountRepository;
    private final TagRepository tagRepository;

    @Autowired
    public SidebarService(TotalVisitCountRepository totalVisitCountRepository, TagRepository tagRepository) {
        this.totalVisitCountRepository = totalVisitCountRepository;
        this.tagRepository = tagRepository;
    }

    public SidebarDataDTO getSidebarData() {
        VisitorCountsDTO visitorCounts = getVisitorCounts();
        List<String> tags = getTags();

        return new SidebarDataDTO(visitorCounts, tags);
    }

    @Cacheable(value = "visitCounts")
    public VisitorCountsDTO getVisitorCounts() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long totalCount = totalVisitCountRepository.count();
        long todayCount = totalVisitCountRepository.findByDate(today)
                .map(TotalVisitCount::getCount)
                .orElse(0L);
        long yesterdayCount = totalVisitCountRepository.findByDate(yesterday)
                .map(TotalVisitCount::getCount)
                .orElse(0L);

        return new VisitorCountsDTO(totalCount, todayCount, yesterdayCount);
    }

    @Cacheable(value = "tags")
    public List<String> getTags() {
        return tagRepository.findAll().stream()
                .map(Tags::getName)
                .collect(Collectors.toList());
    }
}