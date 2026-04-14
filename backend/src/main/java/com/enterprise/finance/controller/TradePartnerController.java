package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.entity.TradePartner;
import com.enterprise.finance.service.TradePartnerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trade-partners")
public class TradePartnerController {

    private final TradePartnerService tradePartnerService;

    public TradePartnerController(TradePartnerService tradePartnerService) {
        this.tradePartnerService = tradePartnerService;
    }

    @GetMapping
    public ApiResponse<Page<TradePartner>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer partnerType,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(tradePartnerService.page(pageNum, pageSize, keyword, partnerType, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<TradePartner> detail(@PathVariable Long id) {
        return ApiResponse.ok(tradePartnerService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody TradePartner request) {
        return ApiResponse.ok(tradePartnerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody TradePartner request) {
        tradePartnerService.update(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tradePartnerService.delete(id);
        return ApiResponse.ok(null);
    }
}
