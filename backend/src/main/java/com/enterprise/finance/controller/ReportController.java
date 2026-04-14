package com.enterprise.finance.controller;

import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.service.ReportService;
import com.enterprise.finance.vo.BalanceSheetResponse;
import com.enterprise.finance.vo.ProfitStatementResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/balance-sheet")
    public ApiResponse<BalanceSheetResponse> balanceSheet(@RequestParam String period) {
        return ApiResponse.ok(reportService.getBalanceSheet(period));
    }

    @GetMapping("/profit-statement")
    public ApiResponse<ProfitStatementResponse> profitStatement(@RequestParam String period) {
        return ApiResponse.ok(reportService.getProfitStatement(period));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam String period,
            @RequestParam(defaultValue = "balance-sheet") String type
    ) {
        String csv = reportService.exportCsv(period, type);
        String fileName = ("profit-statement".equalsIgnoreCase(type) ? "profit-statement-" : "balance-sheet-")
                + period + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}

