package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expense_claim_attachment")
public class ExpenseClaimAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long expenseClaimId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private Long uploaderId;
    private LocalDateTime uploadTime;
}
