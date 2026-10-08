package com.example.forum.controller;

import com.example.forum.dto.request.PostCreateRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.PostResponse;
import com.example.forum.security.JwtTokenProvider;
import com.example.forum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Post created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PostResponse> posts = postService.getPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success("Success", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostCreateRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        PostResponse response = postService.updatePost(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }
}