package com.example.forum.controller;

import com.example.forum.dto.request.CommentCreateRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.CommentResponse;
import com.example.forum.security.JwtTokenProvider;
import com.example.forum.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Comment created successfully", response));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Success", comments));
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long id) {
        CommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}