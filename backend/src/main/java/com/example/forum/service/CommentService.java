
package com.example.forum.service;

import com.example.forum.dto.request.CommentCreateRequest;
import com.example.forum.dto.response.CommentResponse;
import com.example.forum.entity.Comment;
import com.example.forum.enums.PostStatus;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final WarningService warningService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, 
                         UserRepository userRepository, PostService postService,
                         SentimentAnalysisService sentimentAnalysisService,
                         WarningService warningService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postService = postService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.warningService = warningService;
    }

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentCreateRequest request) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setStatus(PostStatus.PENDING);
        
        SentimentAnalysisService.SentimentResult sentimentResult = sentimentAnalysisService.analyze(request.getContent());
        if (sentimentResult != null) {
            comment.setPositiveProb(sentimentResult.getPositiveProb());
            comment.setNegativeProb(sentimentResult.getNegativeProb());
            comment.setSentiment(sentimentResult.getSentiment());
            log.info("Comment sentiment analysis: positive={}, negative={}, sentiment={}", 
                     sentimentResult.getPositiveProb(), sentimentResult.getNegativeProb(), sentimentResult.getSentiment());
        }

        comment = commentRepository.save(comment);
        warningService.handleCommentWarning(comment, sentimentResult);
        if (comment.getStatus() == PostStatus.PUBLISHED) {
            postService.updateCommentCount(postId, 1);
        }

        String username = userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("Unknown");

        return CommentResponse.fromEntity(comment, username);
    }

    public Page<CommentResponse> getCommentsByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostIdAndStatusOrderByCreatedAtDesc(postId, PostStatus.PUBLISHED, pageable)
                .map(comment -> {
                    String username = userRepository.findById(comment.getUserId())
                            .map(u -> u.getUsername())
                            .orElse("Unknown");
                    return CommentResponse.fromEntity(comment, username);
                });
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        String username = userRepository.findById(comment.getUserId())
                .map(u -> u.getUsername())
                .orElse("Unknown");

        return CommentResponse.fromEntity(comment, username);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete comment");
        }

        Long postId = comment.getPostId();
        boolean publishedComment = comment.getStatus() == PostStatus.PUBLISHED;
        commentRepository.delete(comment);
        if (publishedComment) {
            postService.updateCommentCount(postId, -1);
        }
    }

    @Transactional
    public void updateCommentStatus(Long commentId, PostStatus status) {
        commentRepository.findById(commentId).ifPresent(comment -> {
            comment.setStatus(status);
            commentRepository.save(comment);
        });
    }

    public Comment findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }
}
