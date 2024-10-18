package in.myblog.post.dto;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

// Page<PostSummaryDTO>를 Swagger 문서화할 때 사용하는 클래스
public class PostSummaryDTOPage extends PageImpl<PostSummaryDTO> {
    public PostSummaryDTOPage(List<PostSummaryDTO> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
}