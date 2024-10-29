package in.myblog.post.controller;


import in.myblog.post.domain.Posts;
import in.myblog.post.dto.*;
import in.myblog.post.exception.CustomPostExceptions;
import in.myblog.post.service.PostServiceImpl;
import in.myblog.user.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Post Management", description = "APIs for managing blog posts")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostServiceImpl postService;

    @Operation(summary = "Create a new post", description = "Creates a new blog post with images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created post",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostCreationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    public ResponseEntity<PostCreationResponseDTO> createPost(
            @RequestBody PostCreateRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUsername());

        Long postId = postService.createPost(
                request.getTitle(),
                request.getContent(),
                userId,
                request.getTags()
        );
        return ResponseEntity.ok(new PostCreationResponseDTO(postId));
    }


    @Operation(summary = "Update an existing post", description = "Updates an existing blog post with images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated post",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostCreationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/update/{postId}")
    public ResponseEntity<PostCreationResponseDTO> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUsername());

        Long updatedPostId = postService.updatePost(
                postId,
                request.getTitle(),
                request.getContent(),
                userId,
                request.getTags()
        );
        return ResponseEntity.ok(new PostCreationResponseDTO(updatedPostId));
    }

    @Operation(summary = "Delete a post", description = "Deletes an existing blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted post"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(userDetails.getUsername());
        postService.deletePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get recent posts", description = "Retrieves a paged list of recent blog posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostSummaryDTOPage.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<Page<PostSummaryDTO>> getRecentPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "16") int size,
            @RequestParam(required = false) List<String> tags) {
        Page<PostSummaryDTO> response = postService.getRecentPosts(page, size, tags);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a specific post", description = "Retrieves a specific blog post by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponsePageDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<ResponsePageDetailDTO> getPost(@PathVariable Long postId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        ResponsePageDetailDTO post = postService.getPost(postId, ipAddress, userAgent);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "Like a post", description = "Likes or unlikes a post based on the device ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully liked/unliked the post",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LikeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponseDTO> likePost(@PathVariable Long postId,
                                                    @RequestParam("deviceId") String deviceId) {
        LikeResponseDTO response = postService.likePost(postId, deviceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get like status of a post", description = "Checks if a post is liked by a specific device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved like status"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{postId}/like")
    public ResponseEntity<LikeResponseDTO> getLikeStatus(
            @PathVariable Long postId,
            @RequestParam String deviceId) {
        LikeResponseDTO response = postService.getLikeStatus(postId, deviceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-image")
    @Operation(summary = "Upload an image", description = "Uploads an image and returns its URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded image"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestPart("image") MultipartFile image) {
        try {
            String imageUrl = postService.uploadImage(image);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            throw new CustomPostExceptions.ImageUploadFailedException("Failed to upload image", e);
        }
    }
}