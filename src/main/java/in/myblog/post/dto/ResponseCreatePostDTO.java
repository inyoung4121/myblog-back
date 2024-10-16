package in.myblog.post.dto;

import in.myblog.post.domain.Posts;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResponseCreatePostDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;

    public ResponseCreatePostDTO toResponseCreatePostDTO(Posts post) {
        ResponseCreatePostDTO dto = new ResponseCreatePostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        if (post.getAuthor() != null) {
            dto.setAuthorName(post.getAuthor().getUsername()); // 또는 getName() 등 적절한 메서드 사용
        }

        dto.setTags(post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList()));

        return dto;
    }
}
