package in.myblog.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestUpdatePostDTO {
    private String title;
    private String content;
    private List<String> tags;
}
