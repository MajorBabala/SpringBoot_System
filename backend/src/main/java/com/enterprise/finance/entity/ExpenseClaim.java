package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("expense_claim")
public class ExpenseClaim {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String claimNo;
    private String period;
    private String reason;
    private BigDecimal totalAmount;
    /**
     * 费用类型（0/1/2 由前端/后续配置）
     */
    private Integer expenseType;

    private Long applicantId;
    private Long deptId;

    /**
     * 0草稿 1待审批 2已审批 3已付款 4驳回
     */
    private Integer status;

    private Long approverId;
    private LocalDateTime approveTime;

    private Long payerId;
    private LocalDateTime paymentTime;

    private Long voucherId;
}

