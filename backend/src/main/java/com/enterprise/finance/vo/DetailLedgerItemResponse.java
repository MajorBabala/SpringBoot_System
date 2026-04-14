package com.enterprise.finance.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DetailLedgerItemResponse {
    private Long voucherId;
    private String voucherNo;
    private LocalDate voucherDate;
    private String period;
    private String summary;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal runningDebit;
    private BigDecimal runningCredit;
}
