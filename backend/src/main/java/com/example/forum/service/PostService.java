
package com.example.forum.service;

import com.example.forum.dto.request.PostCreateRequest;
import com.example.forum.dto.response.PostResponse;
import com.example.forum.entity.Post;
import com.example.forum.enums.PostStatus;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final WarningService warningService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       SentimentAnalysisService sentimentAnalysisService,
                       WarningService warningService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.warningService = warningService;
    }

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(PostStatus.PENDING);
        post.setViewCount(0);
        post.setCommentCount(0);
        
        SentimentAnalysisService.SentimentResult sentimentResult = sentimentAnalysisService.analyze(request.getContent());
        if (sentimentResult != null) {
            post.setPositiveProb(sentimentResult.getPositiveProb());
            post.setNegativeProb(sentimentResult.getNegativeProb());
            post.setSentiment(sentimentResult.getSentiment());
            log.info("Post sentiment analysis: positive={}, negative={}, sentiment={}", 
                     sentimentResult.getPositiveProb(), sentimentResult.getNegativeProb(), sentimentResult.getSentiment());
        }
        
        post = postRepository.save(post);
        warningService.handlePostWarning(post, sentimentResult);
        
        String username = userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("Unknown");
        
        return PostResponse.fromEntity(post, username);
    }

    public Page<PostResponse> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
                .map(post -> {
                    String username = userRepository.findById(post.getUserId())
                            .map(u -> u.getUsername())
                            .orElse("Unknown");
                    return PostResponse.fromEntity(post, username);
                });
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        
        String username = userRepository.findById(post.getUserId())
                .map(u -> u.getUsername())
                .orElse("Unknown");
        
        return PostResponse.fromEntity(post, username);
    }

    @Transactional
    public PostResponse updatePost(Long id, Long userId, PostCreateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update post");
        }
        
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(PostStatus.PENDING);
        
        SentimentAnalysisService.SentimentResult sentimentResult = sentimentAnalysisService.analyze(request.getContent());
        if (sentimentResult != null) {
            post.setPositiveProb(sentimentResult.getPositiveProb());
            post.setNegativeProb(sentimentResult.getNegativeProb());
            post.setSentiment(sentimentResult.getSentiment());
            log.info("Post updated sentiment analysis: positive={}, negative={}, sentiment={}", 
                     sentimentResult.getPositiveProb(), sentimentResult.getNegativeProb(), sentimentResult.getSentiment());
        }
        
        post = postRepository.save(post);
        warningService.handlePostWarning(post, sentimentResult);
        String username = userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("Unknown");
        
        return PostResponse.fromEntity(post, username);
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete post");
        }
        
        postRepository.delete(post);
    }

    @Transactional
    public void updateCommentCount(Long postId, int delta) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setCommentCount(post.getCommentCount() + delta);
            postRepository.save(post);
        });
    }

    public Post findPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public void updatePostStatus(Long postId, PostStatus status) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setStatus(status);
            postRepository.save(post);
        });
    }
}
