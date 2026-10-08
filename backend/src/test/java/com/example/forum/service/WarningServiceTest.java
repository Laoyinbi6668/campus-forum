package com.example.forum.service;

import com.example.forum.dto.request.AuditRequest;
import com.example.forum.dto.response.WarningResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningServiceTest {

    @Mock
    private WarningRecordRepository warningRecordRepository;
    @Mock
    private AuditRecordRepository auditRecordRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WarningService warningService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(warningService, "banCount", 3);
    }

    @Test
    void handlePostWarningShouldCreatePendingWarningRecord() {
        Post post = Post.builder()
                .id(1L)
                .userId(2L)
                .title("test")
                .content("negative content")
                .status(PostStatus.PENDING)
                .build();
        SentimentAnalysisService.SentimentResult sentimentResult = sentiment(true, 0.91);

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warningRecordRepository.save(any(WarningRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        warningService.handlePostWarning(post, sentimentResult);

        assertEquals(PostStatus.PENDING, post.getStatus());
        ArgumentCaptor<WarningRecord> captor = ArgumentCaptor.forClass(WarningRecord.class);
        verify(warningRecordRepository).save(captor.capture());
        assertEquals(ContentType.POST, captor.getValue().getContentType());
        assertEquals(AuditStatus.PENDING, captor.getValue().getStatus());
        assertEquals(0.91, captor.getValue().getNegativeProb());
    }

    @Test
    void handleCommentWarningShouldAutoPublishNormalComment() {
        Comment comment = Comment.builder()
                .id(10L)
                .postId(1L)
                .userId(2L)
                .content("normal content")
                .status(PostStatus.PENDING)
                .build();
        SentimentAnalysisService.SentimentResult sentimentResult = sentiment(false, 0.12);

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        warningService.handleCommentWarning(comment, sentimentResult);

        assertEquals(PostStatus.PUBLISHED, comment.getStatus());
        verify(warningRecordRepository, times(0)).save(any(WarningRecord.class));
    }

    @Test
    void auditWarningRejectShouldIncreaseViolationAndBanUserAtThreshold() {
        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setId(5L);
        warningRecord.setContentId(7L);
        warningRecord.setContentType(ContentType.POST);
        warningRecord.setContent("bad");
        warningRecord.setNegativeProb(0.95);
        warningRecord.setStatus(AuditStatus.PENDING);

        Post post = Post.builder().id(7L).userId(20L).status(PostStatus.PENDING).build();
        User admin = adminUser(1L);
        User owner = user(20L, 2, UserStatus.ACTIVE);

        when(warningRecordRepository.findById(5L)).thenReturn(Optional.of(warningRecord));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(postRepository.findById(7L)).thenReturn(Optional.of(post));
        when(userRepository.findById(20L)).thenReturn(Optional.of(owner));
        when(warningRecordRepository.save(any(WarningRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditRequest request = new AuditRequest();
        request.setResult("REJECT");
        request.setRemark("violation");

        WarningResponse response = warningService.auditWarning(5L, 1L, request);

        assertEquals("REJECT", response.getStatus());
        assertEquals(PostStatus.REJECTED, post.getStatus());
        assertEquals(3, owner.getViolationCount());
        assertEquals(UserStatus.BANNED, owner.getStatus());
    }

    @Test
    void auditWarningShouldRejectNonAdminOperator() {
        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setId(5L);
        warningRecord.setStatus(AuditStatus.PENDING);

        when(warningRecordRepository.findById(5L)).thenReturn(Optional.of(warningRecord));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, 0, UserStatus.ACTIVE)));

        AuditRequest request = new AuditRequest();
        request.setResult("PASS");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> warningService.auditWarning(5L, 2L, request));
        assertEquals("Only administrators can audit warning records", exception.getMessage());
    }

    private SentimentAnalysisService.SentimentResult sentiment(boolean warning, double negativeProb) {
        SentimentAnalysisService.SentimentResult result = new SentimentAnalysisService.SentimentResult();
        result.setWarning(warning);
        result.setNegativeProb(negativeProb);
        result.setPositiveProb(1 - negativeProb);
        result.setSentiment(warning ? 0 : 2);
        return result;
    }

    private User adminUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("admin");
        user.setRole("ADMIN");
        user.setStatus(UserStatus.ACTIVE);
        user.setViolationCount(0);
        return user;
    }

    private User user(Long id, int violationCount, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername("user");
        user.setRole("USER");
        user.setStatus(status);
        user.setViolationCount(violationCount);
        return user;
    }
}
