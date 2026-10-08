package com.example.forum.dto.response;

import com.example.forum.entity.Post;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String status;
    private String authorUsername;
    private Long authorId;
    private Double positiveProb;
    private Double negativeProb;
    private Integer sentiment;
    private Integer viewCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus().name())
                .positiveProb(post.getPositiveProb())
                .negativeProb(post.getNegativeProb())
                .sentiment(post.getSentiment())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponse fromEntity(Post post, String authorUsername) {
        PostResponse response = fromEntity(post);
        response.setAuthorUsername(authorUsername);
        response.setAuthorId(post.getUserId());
        return response;
    }
}