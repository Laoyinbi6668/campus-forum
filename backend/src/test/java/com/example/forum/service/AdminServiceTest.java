package com.example.forum.service;

import com.example.forum.dto.response.AuditRecordResponse;
import com.example.forum.dto.response.PostResponse;
import com.example.forum.dto.response.StatResponse;
import com.example.forum.dto.response.UserResponse;
import com.example.forum.entity.AuditRecord;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.entity.WarningRecord;
import com.example.forum.enums.AuditStatus;
import com.example.forum.enums.ContentType;
import com.example.forum.enums.PostStatus;
import com.example.forum.enums.UserStatus;
import com.example.forum.repository.AuditRecordRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.repository.WarningRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WarningRecordRepository warningRecordRepository;
    @Mock
    private AuditRecordRepository auditRecordRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getPostsShouldReturnMappedAuthorNames() {
        Post post = Post.builder()
                .id(1L)
                .userId(10L)
                .title("test")
                .content("content")
                .status(PostStatus.PUBLISHED)
                .build();
        User author = user(10L, "alice", "USER", UserStatus.ACTIVE);

        when(postRepository.findAllOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));

        Page<PostResponse> response = adminService.getPosts(0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals("alice", response.getContent().get(0).getAuthorUsername());
    }

    @Test
    void banUserShouldRejectAdministrator() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "admin", "ADMIN", UserStatus.ACTIVE)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.banUser(1L));

        assertEquals("Administrator accounts cannot be banned", exception.getMessage());
    }

    @Test
    void getStatsShouldAggregateCoreCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        when(postRepository.count()).thenReturn(12L);
        when(postRepository.countByCreatedAtGreaterThanEqual(startOfToday)).thenReturn(3L);
        when(postRepository.countByStatus(PostStatus.PENDING)).thenReturn(2L);
        when(postRepository.countByStatus(PostStatus.PUBLISHED)).thenReturn(8L);
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(4L);
        when(userRepository.countByStatus(UserStatus.BANNED)).thenReturn(1L);
        when(userRepository.countByRole("ADMIN")).thenReturn(1L);
        when(warningRecordRepository.countByCreatedAtGreaterThanEqual(startOfToday)).thenReturn(2L);
        when(warningRecordRepository.countByStatus(AuditStatus.PENDING)).thenReturn(1L);
        when(auditRecordRepository.count()).thenReturn(6L);
        when(postRepository.countBySentiment(2)).thenReturn(7L);
        when(postRepository.countBySentiment(1)).thenReturn(3L);
        when(postRepository.countBySentiment(0)).thenReturn(2L);

        StatResponse response = adminService.getStats();

        assertEquals(12L, response.getTotalPostCount());
        assertEquals(3L, response.getTodayPostCount());
        assertEquals(5L, response.getTotalUserCount());
        assertEquals(2L, response.getTodayWarningCount());
        assertEquals(7L, response.getPositivePostCount());
    }

    @Test
    void getAuditRecordsShouldIncludeWarningContextAndAdminName() {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setId(9L);
        auditRecord.setWarningId(4L);
        auditRecord.setAdminId(2L);
        auditRecord.setResult(AuditStatus.REJECT);
        auditRecord.setRemark("bad");
        auditRecord.setCreatedAt(LocalDateTime.now());

        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setId(4L);
        warningRecord.setContentId(99L);
        warningRecord.setContentType(ContentType.POST);

        when(auditRecordRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(auditRecord)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, "moderator", "ADMIN", UserStatus.ACTIVE)));
        when(warningRecordRepository.findById(4L)).thenReturn(Optional.of(warningRecord));

        Page<AuditRecordResponse> response = adminService.getAuditRecords(0, 10);

        assertEquals(1, response.getTotalElements());
        assertEquals("moderator", response.getContent().get(0).getAdminUsername());
        assertEquals("POST", response.getContent().get(0).getContentType());
    }

    @Test
    void deletePostShouldRemoveExistingPost() {
        Post post = Post.builder().id(8L).userId(3L).build();
        when(postRepository.findById(8L)).thenReturn(Optional.of(post));

        adminService.deletePost(8L);

        verify(postRepository).delete(post);
    }

    private User user(Long id, String username, String role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
