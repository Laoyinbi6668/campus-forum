package com.example.forum.dto.response;

import com.example.forum.entity.Comment;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private Long postId;
    private String content;
    private String status;
    private String authorUsername;
    private Long authorId;
    private Double positiveProb;
    private Double negativeProb;
    private Integer sentiment;
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .status(comment.getStatus().name())
                .positiveProb(comment.getPositiveProb())
                .negativeProb(comment.getNegativeProb())
                .sentiment(comment.getSentiment())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static CommentResponse fromEntity(Comment comment, String authorUsername) {
        CommentResponse response = fromEntity(comment);
        response.setAuthorUsername(authorUsername);
        response.setAuthorId(comment.getUserId());
        return response;
    }
}