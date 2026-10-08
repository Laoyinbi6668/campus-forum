package com.example.forum.dto.response;

import com.example.forum.entity.WarningRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WarningResponse {

    private Long id;
    private Long contentId;
    private String contentType;
    private String content;
    private Double negativeProb;
    private String status;
    private LocalDateTime createdAt;

    public static WarningResponse fromEntity(WarningRecord warningRecord) {
        return WarningResponse.builder()
                .id(warningRecord.getId())
                .contentId(warningRecord.getContentId())
                .contentType(warningRecord.getContentType().name())
                .content(warningRecord.getContent())
                .negativeProb(warningRecord.getNegativeProb())
                .status(warningRecord.getStatus().name())
                .createdAt(warningRecord.getCreatedAt())
                .build();
    }
}
