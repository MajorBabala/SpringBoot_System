package com.enterprise.finance.controller;

import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.VoucherService;
import com.enterprise.finance.dto.VoucherCreateRequest;
import com.enterprise.finance.vo.VoucherCreateResponse;
import com.enterprise.finance.vo.VoucherDetailResponse;
import com.enterprise.finance.entity.Voucher;
import com.enterprise.finance.entity.VoucherEntry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    private JwtPrincipal currentPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtPrincipal jp) {
            return jp;
        }
        throw new IllegalStateException("未找到认证主体");
    }

    @PostMapping
    public ApiResponse<VoucherCreateResponse> create(@Valid @RequestBody VoucherCreateRequest request) {
        JwtPrincipal principal = currentPrincipal();
        VoucherCreateResponse resp = voucherService.createVoucher(request, principal.getUserId());
        return ApiResponse.ok(resp);
    }

    @GetMapping
    public ApiResponse<Page<Voucher>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String period
    ) {
        return ApiResponse.ok(voucherService.listVouchers(pageNum, pageSize, status, period));
    }

    @GetMapping("/{id}")
    public ApiResponse<VoucherDetailResponse> getDetail(@PathVariable("id") Long id) {
        Voucher voucher = voucherService.getVoucherById(id);
        List<VoucherEntry> entries = voucherService.getVoucherEntries(id);
        return ApiResponse.ok(new VoucherDetailResponse(voucher, entries));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<Object> audit(@PathVariable("id") Long id) {
        JwtPrincipal principal = currentPrincipal();
        voucherService.auditVoucher(id, principal.getUserId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/bookkeep")
    public ApiResponse<Object> bookkeep(@PathVariable("id") Long id) {
        JwtPrincipal principal = currentPrincipal();
        voucherService.bookkeepVoucher(id, principal.getUserId());
        return ApiResponse.ok(null);
    }
}

