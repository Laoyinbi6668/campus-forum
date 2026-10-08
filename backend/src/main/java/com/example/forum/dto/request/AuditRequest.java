package com.example.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditRequest {

    @NotBlank(message = "Audit result is required")
    private String result;

    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
}
