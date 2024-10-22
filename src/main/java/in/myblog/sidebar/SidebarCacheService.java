package in.myblog.sidebar;

import com.querydsl.jpa.impl.JPAQueryFactory;
import in.myblog.post.domain.QTotalVisitCount;
import in.myblog.post.domain.Tags;
import in.myblog.post.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SidebarCacheService {

    private final JPAQueryFactory queryFactory;
    private final TagRepository tagRepository;

    @Cacheable(value = "visitorCounts")
    public VisitorCountsDTO getVisitorCounts() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        QTotalVisitCount qVisit = QTotalVisitCount.totalVisitCount;

        //쿼리 한번으로 불러오나 세번으로 불러오나 캐시되어있어 성능차이가 미미 할 것이므로 일자별로 명확하게 불러옴
        Long totalCount = queryFactory
                .select(qVisit.count.sum())
                .from(qVisit)
                .fetchOne();

        Long todayCount = queryFactory
                .select(qVisit.count.sum())
                .from(qVisit)
                .where(qVisit.date.eq(today))
                .fetchOne();

        Long yesterdayCount = queryFactory
                .select(qVisit.count.sum())
                .from(qVisit)
                .where(qVisit.date.eq(yesterday))
                .fetchOne();

        return new VisitorCountsDTO(
                totalCount != null ? totalCount : 0L,
                todayCount != null ? todayCount : 0L,
                yesterdayCount != null ? yesterdayCount : 0L
        );
    }

    @Cacheable(value = "tags")
    public List<String> getTags() {
        return tagRepository.findAll().stream()
                .map(Tags::getName)
                .collect(Collectors.toList());
    }
}
