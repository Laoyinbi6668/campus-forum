package com.example.forum.repository;

import com.example.forum.entity.Comment;
import com.example.forum.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    Page<Comment> findByPostIdAndStatus(Long postId, PostStatus status, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.createdAt DESC")
    Page<Comment> findByPostIdOrderByCreatedAtDesc(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findByPostIdAndStatusOrderByCreatedAtDesc(@Param("postId") Long postId, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    Page<Comment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.status = :status")
    long countByPostIdAndStatus(@Param("postId") Long postId, @Param("status") PostStatus status);
}