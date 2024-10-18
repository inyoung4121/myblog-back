package in.myblog.comment.controller;

import in.myblog.comment.dto.CommentDto;
import in.myblog.comment.dto.CommentListDto;
import in.myblog.comment.service.CommentService;
import in.myblog.comment.service.CommentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentServiceImpl commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "포스트 ID로 댓글 가져오기", description = "특정 포스트에 대한 댓글 목록을 포스트 ID로 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 가져왔습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentListDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 포스트를 찾을 수 없습니다")
    })
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentListDto>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @Operation(summary = "새 댓글 생성", description = "특정 포스트에 새 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 생성했습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터가 잘못되었습니다"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다")
    })
    @PostMapping
    public ResponseEntity<CommentListDto> createComment(@RequestBody CommentDto commentDto) {
        return ResponseEntity.ok(commentService.createComment(commentDto));
    }

    @Operation(summary = "기존 댓글 수정", description = "댓글 ID로 특정 댓글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 수정했습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터가 잘못되었습니다"),
            @ApiResponse(responseCode = "403", description = "해당 댓글에 대한 수정 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없습니다")
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentListDto> updateComment(@PathVariable Long commentId,
                                                    @RequestBody CommentDto commentDto,
                                                    @RequestParam Long userId,
                                                    @RequestParam(required = false) String deletePassword) {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDto, userId, deletePassword));
    }

    @Operation(summary = "댓글 삭제", description = "댓글 ID로 특정 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 삭제했습니다"),
            @ApiResponse(responseCode = "403", description = "해당 댓글에 대한 삭제 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없습니다")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) String deletePassword) {
        System.out.println(commentId+deletePassword);
        commentService.deleteComment(commentId, userId, deletePassword);
        return ResponseEntity.ok().build();
    }
}