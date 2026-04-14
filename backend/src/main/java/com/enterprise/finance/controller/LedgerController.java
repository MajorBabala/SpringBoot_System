package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.service.LedgerService;
import com.enterprise.finance.vo.DetailLedgerItemResponse;
import com.enterprise.finance.vo.GeneralLedgerItemResponse;
import com.enterprise.finance.vo.JournalEntryItemResponse;
import com.enterprise.finance.vo.SubjectBalanceListItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/subject-balances")
    public ApiResponse<IPage<SubjectBalanceListItemResponse>> pageSubjectBalances(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(ledgerService.pageSubjectBalances(page, limit, period, keyword));
    }

    @GetMapping("/general")
    public ApiResponse<IPage<GeneralLedgerItemResponse>> pageGeneralLedger(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(ledgerService.pageGeneralLedger(page, limit, period, keyword));
    }

    @GetMapping("/detail")
    public ApiResponse<IPage<DetailLedgerItemResponse>> pageDetailLedger(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String period,
            @RequestParam Long subjectId
    ) {
        return ApiResponse.ok(ledgerService.pageDetailLedger(page, limit, period, subjectId));
    }

    @GetMapping("/journal")
    public ApiResponse<IPage<JournalEntryItemResponse>> pageJournal(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(ledgerService.pageJournal(page, limit, period, keyword));
    }
}
