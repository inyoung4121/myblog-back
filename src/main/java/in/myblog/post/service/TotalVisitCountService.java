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


}
