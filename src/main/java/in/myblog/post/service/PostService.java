package in.myblog.post.service;

import in.myblog.post.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {
    Long createPost(String title, String content, Long authorId, List<String> tags);
    Long updatePost(Long postId, String title, String content, Long authorId, List<String> tags);
    void deletePost(Long postId) ;
    ResponsePageDetailDTO getPost(Long postId, String ipAddress, String userAgent) ;
    Page<PostSummaryDTO> getRecentPosts(int page, int size);
    LikeResponseDTO likePost(Long postId, String deviceId);
}
