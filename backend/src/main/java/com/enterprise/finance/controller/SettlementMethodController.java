package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.entity.SettlementMethod;
import com.enterprise.finance.service.SettlementMethodService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlement-methods")
public class SettlementMethodController {

    private final SettlementMethodService settlementMethodService;

    public SettlementMethodController(SettlementMethodService settlementMethodService) {
        this.settlementMethodService = settlementMethodService;
    }

    @GetMapping
    public ApiResponse<Page<SettlementMethod>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(settlementMethodService.page(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<SettlementMethod> detail(@PathVariable Long id) {
        return ApiResponse.ok(settlementMethodService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody SettlementMethod request) {
        return ApiResponse.ok(settlementMethodService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SettlementMethod request) {
        settlementMethodService.update(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        settlementMethodService.delete(id);
        return ApiResponse.ok(null);
    }
}
