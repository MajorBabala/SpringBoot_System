package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class VoucherEntryCreateRequest {

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    private String summary;

    @NotNull(message = "借方金额不能为空")
    private BigDecimal debitAmount;

    @NotNull(message = "贷方金额不能为空")
    private BigDecimal creditAmount;

    private Integer auxiliaryType;
    private Long auxiliaryId;
}

