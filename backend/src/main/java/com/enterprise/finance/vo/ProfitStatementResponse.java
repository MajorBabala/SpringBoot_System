package com.enterprise.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitStatementResponse {
    /**
     * MVP 先给出净利润（可按后续规则扩展科目明细）
     */
    private BigDecimal netProfit;
}

