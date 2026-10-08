package com.example.forum.repository;

import com.example.forum.entity.WarningRecord;
import com.example.forum.enums.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface WarningRecordRepository extends JpaRepository<WarningRecord, Long> {

    Page<WarningRecord> findByStatusOrderByCreatedAtDesc(AuditStatus status, Pageable pageable);

    long countByStatus(AuditStatus status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);
}
