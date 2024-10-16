package in.myblog.post.dto;

import in.myblog.comment.ResponseCommentDTO;
import in.myblog.post.domain.Posts;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class ResponseUpdatePostDTO extends ResponseCreatePostDTO{
    private List<ResponseCommentDTO> commentDTOList;

    public ResponseUpdatePostDTO fromPost(Posts post) {
        ResponseUpdatePostDTO dto = new ResponseUpdatePostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        if (post.getAuthor() != null) {
            dto.setAuthorName(post.getAuthor().getUsername());
        }

        dto.setTags(post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList()));

        dto.setCommentDTOList(post.getComments().stream()
                .map(ResponseCommentDTO::fromComment)
                .collect(Collectors.toList()));

        return dto;
    }
}
