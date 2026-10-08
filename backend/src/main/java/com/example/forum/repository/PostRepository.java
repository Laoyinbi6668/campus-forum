
package com.example.forum.repository;

import com.example.forum.entity.Post;
import com.example.forum.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    List<Post> findByStatus(PostStatus status);

    Page<Post> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Post> findByStatusOrderByCreatedAtDesc(@Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startTime")
    List<Post> findByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.createdAt >= :startTime")
    List<Post> findByStatusAndCreatedAtAfter(@Param("status") PostStatus status, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT p FROM Post p WHERE p.status IN :statuses ORDER BY p.createdAt DESC")
    Page<Post> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<PostStatus> statuses, Pageable pageable);

    long countByCreatedAtGreaterThanEqual(LocalDateTime startTime);

    long countByStatus(PostStatus status);

    long countBySentiment(Integer sentiment);
}
