package com.enterprise.finance.vo;

import com.enterprise.finance.entity.Voucher;
import com.enterprise.finance.entity.VoucherEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDetailResponse {
    private Voucher voucher;
    private List<VoucherEntry> entries;
}

