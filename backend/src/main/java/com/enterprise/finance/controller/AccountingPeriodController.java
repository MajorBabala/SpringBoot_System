package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.entity.AccountingPeriod;
import com.enterprise.finance.service.AccountingPeriodService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounting-periods")
public class AccountingPeriodController {

    private final AccountingPeriodService accountingPeriodService;

    public AccountingPeriodController(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    @GetMapping
    public ApiResponse<Page<AccountingPeriod>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(accountingPeriodService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountingPeriod> detail(@PathVariable Long id) {
        return ApiResponse.ok(accountingPeriodService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody AccountingPeriod request) {
        return ApiResponse.ok(accountingPeriodService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AccountingPeriod request) {
        accountingPeriodService.update(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accountingPeriodService.delete(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/open")
    public ApiResponse<Void> open(@PathVariable Long id) {
        accountingPeriodService.open(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        accountingPeriodService.close(id);
        return ApiResponse.ok(null);
    }
}
