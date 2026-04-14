package com.enterprise.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetResponse {
    private BigDecimal totalAsset;
    private List<BalanceSheetItem> assets;
    private BigDecimal totalLiability;
    private List<BalanceSheetItem> liabilities;
    private BigDecimal totalEquity;
    private List<BalanceSheetItem> equities;
    private BigDecimal totalLiabilityAndEquity;
}

