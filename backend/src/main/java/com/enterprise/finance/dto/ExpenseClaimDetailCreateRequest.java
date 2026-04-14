package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ExpenseClaimDetailCreateRequest {

    @NotBlank(message = "明细名称不能为空")
    private String itemName;

    private Long subjectId;

    @NotNull(message = "明细金额不能为空")
    private BigDecimal amount;

    private Integer expenseType;

    private String remark;
}

