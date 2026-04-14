package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("expense_claim_detail")
public class ExpenseClaimDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long expenseClaimId;
    private Integer lineNo;

    private String itemName;
    private Long subjectId;
    private Integer expenseType;
    private BigDecimal amount;
    private String remark;
}

