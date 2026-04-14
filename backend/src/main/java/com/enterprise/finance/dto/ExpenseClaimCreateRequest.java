package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseClaimCreateRequest {

    @NotBlank(message = "Claim number is required")
    private String claimNo;

    @NotBlank(message = "Accounting period is required")
    private String period;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Department is required")
    private Long deptId;

    @NotNull(message = "Expense type is required")
    private Integer expenseType;

    private BigDecimal totalAmount;

    @NotNull(message = "Expense details are required")
    @Valid
    private List<@Valid ExpenseClaimDetailCreateRequest> details;
}
