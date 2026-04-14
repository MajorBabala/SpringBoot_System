package com.enterprise.finance.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralLedgerItemResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String period;
    private BigDecimal openingDebit;
    private BigDecimal openingCredit;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal endingDebit;
    private BigDecimal endingCredit;
}
