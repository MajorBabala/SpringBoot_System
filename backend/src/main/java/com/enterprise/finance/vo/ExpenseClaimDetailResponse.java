package com.enterprise.finance.vo;

import com.enterprise.finance.entity.ExpenseClaim;
import com.enterprise.finance.entity.ExpenseClaimDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaimDetailResponse {
    private ExpenseClaim claim;
    private List<ExpenseClaimDetail> details;
    private List<ExpenseClaimAttachmentResponse> attachments;
}

