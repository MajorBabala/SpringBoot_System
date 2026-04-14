package com.enterprise.finance.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpenseClaimAttachmentResponse {
    private Long id;
    private Long expenseClaimId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadTime;
}
