package in.myblog.post.service;

import in.myblog.post.domain.Posts;
import in.myblog.post.dto.ResponseCreatePostDTO;
import in.myblog.post.dto.ResponsePagedPostsDTO;
import in.myblog.post.dto.ResponseUpdatePostDTO;

import java.util.List;

public interface PostService {
    ResponseCreatePostDTO createPost(String title, String content, Long authorId, List<String> tags);
    ResponseUpdatePostDTO updatePost(Long postId, String title, String content, Long authorId, List<String> tags);
    void deletePost(Long postId) ;
    Posts getPost(Long postId, String ipAddress, String userAgent) ;
    ResponsePagedPostsDTO getRecentPosts(int page, int size);
}
