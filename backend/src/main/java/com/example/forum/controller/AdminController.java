
package com.example.forum.controller;

import com.example.forum.dto.request.AuditRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.AuditRecordResponse;
import com.example.forum.dto.response.PostResponse;
import com.example.forum.dto.response.StatResponse;
import com.example.forum.dto.response.UserResponse;
import com.example.forum.dto.response.WarningResponse;
import com.example.forum.security.JwtTokenProvider;
import com.example.forum.service.AdminService;
import com.example.forum.service.WarningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final WarningService warningService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("success", adminService.getPosts(page, size)));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("success", adminService.getUsers(page, size)));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User banned successfully", adminService.banUser(id)));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User unbanned successfully", adminService.unbanUser(id)));
    }

    @GetMapping("/warnings")
    public ResponseEntity<ApiResponse<Page<WarningResponse>>> getWarnings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("success", warningService.getPendingWarnings(page, size)));
    }

    @PostMapping("/warnings/{id}/audit")
    public ResponseEntity<ApiResponse<WarningResponse>> auditWarning(
            @PathVariable Long id,
            @Valid @RequestBody AuditRequest request,
            @RequestHeader("Authorization") String authorization) {
        Long adminId = jwtTokenProvider.getUserIdFromToken(authorization.replace("Bearer ", ""));
        WarningResponse response = warningService.auditWarning(id, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Audit completed successfully", response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("success", adminService.getStats()));
    }

    @GetMapping("/audit-records")
    public ResponseEntity<ApiResponse<Page<AuditRecordResponse>>> getAuditRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("success", adminService.getAuditRecords(page, size)));
    }
}
