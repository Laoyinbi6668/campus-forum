package com.example.forum.service;

import com.example.forum.dto.request.AuditRequest;
import com.example.forum.dto.response.WarningResponse;
import com.example.forum.entity.AuditRecord;
import com.example.forum.entity.Comment;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.entity.WarningRecord;
import com.example.forum.enums.AuditStatus;
import com.example.forum.enums.ContentType;
import com.example.forum.enums.PostStatus;
import com.example.forum.enums.UserStatus;
import com.example.forum.repository.AuditRecordRepository;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.repository.WarningRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class WarningService {

    private final WarningRecordRepository warningRecordRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Value("${system.warning.ban-count:3}")
    private int banCount;

    public WarningService(WarningRecordRepository warningRecordRepository,
                          AuditRecordRepository auditRecordRepository,
                          PostRepository postRepository,
                          CommentRepository commentRepository,
                          UserRepository userRepository) {
        this.warningRecordRepository = warningRecordRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void handlePostWarning(Post post, SentimentAnalysisService.SentimentResult sentimentResult) {
        if (isWarning(sentimentResult)) {
            post.setStatus(PostStatus.PENDING);
            postRepository.save(post);
            createWarningRecord(post.getId(), ContentType.POST, post.getContent(), sentimentResult.getNegativeProb());
            return;
        }

        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);
    }

    @Transactional
    public void handleCommentWarning(Comment comment, SentimentAnalysisService.SentimentResult sentimentResult) {
        if (isWarning(sentimentResult)) {
            comment.setStatus(PostStatus.PENDING);
            commentRepository.save(comment);
            createWarningRecord(comment.getId(), ContentType.COMMENT, comment.getContent(), sentimentResult.getNegativeProb());
            return;
        }

        comment.setStatus(PostStatus.PUBLISHED);
        commentRepository.save(comment);
    }

    public Page<WarningResponse> getPendingWarnings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return warningRecordRepository.findByStatusOrderByCreatedAtDesc(AuditStatus.PENDING, pageable)
                .map(WarningResponse::fromEntity);
    }

    @Transactional
    public WarningResponse auditWarning(Long warningId, Long adminId, AuditRequest request) {
        WarningRecord warningRecord = warningRecordRepository.findById(warningId)
                .orElseThrow(() -> new RuntimeException("Warning record not found"));

        if (warningRecord.getStatus() != AuditStatus.PENDING) {
            throw new RuntimeException("Warning record has already been audited");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only administrators can audit warning records");
        }

        AuditStatus auditStatus = parseAuditStatus(request.getResult());
        warningRecord.setStatus(auditStatus);
        warningRecordRepository.save(warningRecord);

        updateContentStatus(warningRecord, auditStatus);
        createAuditRecord(warningId, adminId, auditStatus, request.getRemark());

        if (auditStatus == AuditStatus.REJECT) {
            accumulateViolationAndBanIfNeeded(warningRecord);
        }

        return WarningResponse.fromEntity(warningRecord);
    }

    private boolean isWarning(SentimentAnalysisService.SentimentResult sentimentResult) {
        return sentimentResult != null && Boolean.TRUE.equals(sentimentResult.getWarning());
    }

    private void createWarningRecord(Long contentId, ContentType contentType, String content, Double negativeProb) {
        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setContentId(contentId);
        warningRecord.setContentType(contentType);
        warningRecord.setContent(buildSummary(content));
        warningRecord.setNegativeProb(negativeProb);
        warningRecord.setStatus(AuditStatus.PENDING);
        warningRecordRepository.save(warningRecord);
    }

    private String buildSummary(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 300 ? content : content.substring(0, 300);
    }

    private AuditStatus parseAuditStatus(String result) {
        try {
            return AuditStatus.valueOf(result.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new RuntimeException("Unsupported audit result, expected PASS or REJECT");
        }
    }

    private void updateContentStatus(WarningRecord warningRecord, AuditStatus auditStatus) {
        PostStatus targetStatus = auditStatus == AuditStatus.PASS ? PostStatus.PUBLISHED : PostStatus.REJECTED;
        if (warningRecord.getContentType() == ContentType.POST) {
            Post post = postRepository.findById(warningRecord.getContentId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            post.setStatus(targetStatus);
            postRepository.save(post);
            return;
        }

        Comment comment = commentRepository.findById(warningRecord.getContentId())
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        boolean shouldIncreaseCount = comment.getStatus() != PostStatus.PUBLISHED && auditStatus == AuditStatus.PASS;
        comment.setStatus(targetStatus);
        commentRepository.save(comment);
        if (shouldIncreaseCount) {
            postRepository.findById(comment.getPostId()).ifPresent(post -> {
                post.setCommentCount(post.getCommentCount() + 1);
                postRepository.save(post);
            });
        }
    }

    private void createAuditRecord(Long warningId, Long adminId, AuditStatus auditStatus, String remark) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setWarningId(warningId);
        auditRecord.setAdminId(adminId);
        auditRecord.setResult(auditStatus);
        auditRecord.setRemark(remark);
        auditRecordRepository.save(auditRecord);
    }

    private void accumulateViolationAndBanIfNeeded(WarningRecord warningRecord) {
        Long ownerId = resolveOwnerId(warningRecord);
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Content owner not found"));
        user.setViolationCount(user.getViolationCount() + 1);
        if (user.getViolationCount() >= banCount) {
            user.setStatus(UserStatus.BANNED);
        }
        userRepository.save(user);
    }

    private Long resolveOwnerId(WarningRecord warningRecord) {
        if (warningRecord.getContentType() == ContentType.POST) {
            return postRepository.findById(warningRecord.getContentId())
                    .map(Post::getUserId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
        }
        return commentRepository.findById(warningRecord.getContentId())
                .map(Comment::getUserId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }
}
