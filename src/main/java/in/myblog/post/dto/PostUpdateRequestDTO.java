package in.myblog.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequestDTO {
    private String title;
    private String content;
    private List<String> tags;
}