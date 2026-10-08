package com.example.forum.service;

import com.example.forum.dto.response.AuditRecordResponse;
import com.example.forum.dto.response.PostResponse;
import com.example.forum.dto.response.StatResponse;
import com.example.forum.dto.response.UserResponse;
import com.example.forum.entity.AuditRecord;
import com.example.forum.entity.User;
import com.example.forum.enums.AuditStatus;
import com.example.forum.enums.PostStatus;
import com.example.forum.enums.UserStatus;
import com.example.forum.repository.AuditRecordRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.repository.WarningRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AdminService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final WarningRecordRepository warningRecordRepository;
    private final AuditRecordRepository auditRecordRepository;

    public AdminService(PostRepository postRepository,
                        UserRepository userRepository,
                        WarningRecordRepository warningRecordRepository,
                        AuditRecordRepository auditRecordRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.warningRecordRepository = warningRecordRepository;
        this.auditRecordRepository = auditRecordRepository;
    }

    public Page<PostResponse> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllOrderByCreatedAtDesc(pageable)
                .map(post -> PostResponse.fromEntity(
                        post,
                        userRepository.findById(post.getUserId()).map(User::getUsername).orElse("Unknown")
                ));
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.delete(postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found")));
    }

    public Page<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    @Transactional
    public UserResponse banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Administrator accounts cannot be banned");
        }
        user.setStatus(UserStatus.BANNED);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public StatResponse getStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        StatResponse response = new StatResponse();
        response.setTotalPostCount(postRepository.count());
        response.setTodayPostCount(postRepository.countByCreatedAtGreaterThanEqual(startOfToday));
        response.setPendingPostCount(postRepository.countByStatus(PostStatus.PENDING));
        response.setPublishedPostCount(postRepository.countByStatus(PostStatus.PUBLISHED));
        response.setTotalUserCount(userRepository.count());
        response.setActiveUserCount(userRepository.countByStatus(UserStatus.ACTIVE));
        response.setBannedUserCount(userRepository.countByStatus(UserStatus.BANNED));
        response.setAdminUserCount(userRepository.countByRole("ADMIN"));
        response.setTodayWarningCount(warningRecordRepository.countByCreatedAtGreaterThanEqual(startOfToday));
        response.setPendingWarningCount(warningRecordRepository.countByStatus(AuditStatus.PENDING));
        response.setTotalAuditCount(auditRecordRepository.count());
        response.setPositivePostCount(postRepository.countBySentiment(2));
        response.setNeutralPostCount(postRepository.countBySentiment(1));
        response.setNegativePostCount(postRepository.countBySentiment(0));
        return response;
    }

    public Page<AuditRecordResponse> getAuditRecords(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return auditRecordRepository.findAll(pageable).map(this::toAuditRecordResponse);
    }

    private AuditRecordResponse toAuditRecordResponse(AuditRecord auditRecord) {
        AuditRecordResponse response = new AuditRecordResponse();
        response.setId(auditRecord.getId());
        response.setWarningId(auditRecord.getWarningId());
        response.setAdminId(auditRecord.getAdminId());
        response.setAdminUsername(userRepository.findById(auditRecord.getAdminId())
                .map(User::getUsername)
                .orElse("Unknown"));
        response.setResult(auditRecord.getResult().name());
        response.setRemark(auditRecord.getRemark());
        response.setCreatedAt(auditRecord.getCreatedAt());

        warningRecordRepository.findById(auditRecord.getWarningId()).ifPresent(warning -> {
            response.setContentId(warning.getContentId());
            response.setContentType(warning.getContentType().name());
        });
        return response;
    }
}
