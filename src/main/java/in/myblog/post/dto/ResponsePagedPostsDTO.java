package in.myblog.post.dto;

import in.myblog.post.repository.PostSummary;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class ResponsePagedPostsDTO {

    private List<ResponsePagedPostsInnerDTO> list;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public ResponsePagedPostsDTO(Page<PostSummary> postPage) {
        this.list = postPage.getContent().stream()
                .map(this::convertToInnerDTO)
                .collect(Collectors.toList());
        this.pageNo = postPage.getNumber();
        this.pageSize = postPage.getSize();
        this.totalElements = postPage.getTotalElements();
        this.totalPages = postPage.getTotalPages();
        this.last = postPage.isLast();
    }

    private ResponsePagedPostsInnerDTO convertToInnerDTO(PostSummary summary) {
        return new ResponsePagedPostsInnerDTO(
                summary.getId(),
                summary.getTitle(),
                summary.getAuthorName(),
                summary.getCreatedAt(),
                summary.getContentPreview()
        );
    }
}
