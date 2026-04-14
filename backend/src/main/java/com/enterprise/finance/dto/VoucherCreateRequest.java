package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class VoucherCreateRequest {

    @NotBlank(message = "凭证号不能为空")
    private String voucherNo;

    @NotBlank(message = "会计期间不能为空（YYYYMM）")
    private String period;

    @NotNull(message = "凭证日期不能为空")
    private LocalDate voucherDate;

    private String summary;

    private List<@Valid VoucherEntryCreateRequest> entries;

    /**
     * maker_id 在后端取当前登录用户；该字段只作为兼容，实际不使用也可以。
     */
    private Long makerId;
}

