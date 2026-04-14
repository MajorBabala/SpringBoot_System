package com.enterprise.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaimCreateResponse {
    private Long id;
    private Integer status;
}

