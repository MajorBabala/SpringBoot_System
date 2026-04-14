package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.dto.ExpenseClaimCreateRequest;
import com.enterprise.finance.entity.ExpenseClaim;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.ExpenseClaimService;
import com.enterprise.finance.vo.ExpenseClaimAttachmentResponse;
import com.enterprise.finance.vo.ExpenseClaimCreateResponse;
import com.enterprise.finance.vo.ExpenseClaimDetailResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/expense-claims")
public class ExpenseClaimController {

    private final ExpenseClaimService expenseClaimService;

    public ExpenseClaimController(ExpenseClaimService expenseClaimService) {
        this.expenseClaimService = expenseClaimService;
    }

    private JwtPrincipal currentPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtPrincipal jp) {
            return jp;
        }
        throw new IllegalStateException("Authenticated principal not found");
    }

    @GetMapping
    public ApiResponse<Page<ExpenseClaim>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(expenseClaimService.listExpenseClaims(pageNum, pageSize, status));
    }

    @PostMapping
    public ApiResponse<ExpenseClaimCreateResponse> create(@Valid @RequestBody ExpenseClaimCreateRequest request) {
        JwtPrincipal principal = currentPrincipal();
        Long id = expenseClaimService.createExpenseClaim(request, principal.getUserId());
        return ApiResponse.ok(new ExpenseClaimCreateResponse(id, 0));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseClaimDetailResponse> getDetail(@PathVariable("id") Long id) {
        return ApiResponse.ok(expenseClaimService.getDetail(id));
    }

    @PutMapping("/{id}/submit")
    public ApiResponse<Object> submit(@PathVariable("id") Long id) {
        expenseClaimService.submit(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Object> approve(@PathVariable("id") Long id) {
        JwtPrincipal principal = currentPrincipal();
        expenseClaimService.approve(id, principal.getUserId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<Object> pay(@PathVariable("id") Long id) {
        JwtPrincipal principal = currentPrincipal();
        expenseClaimService.pay(id, principal.getUserId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/generate-voucher")
    public ApiResponse<Long> generateVoucher(@PathVariable("id") Long id) {
        JwtPrincipal principal = currentPrincipal();
        return ApiResponse.ok(expenseClaimService.generateVoucher(id, principal.getUserId()));
    }

    @GetMapping("/{id}/attachments")
    public ApiResponse<List<ExpenseClaimAttachmentResponse>> listAttachments(@PathVariable("id") Long id) {
        return ApiResponse.ok(expenseClaimService.listAttachments(id));
    }

    @PostMapping("/{id}/attachments")
    public ApiResponse<ExpenseClaimAttachmentResponse> uploadAttachment(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file
    ) {
        JwtPrincipal principal = currentPrincipal();
        return ApiResponse.ok(expenseClaimService.uploadAttachment(id, file, principal.getUserId()));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ApiResponse<Object> deleteAttachment(
            @PathVariable("id") Long id,
            @PathVariable("attachmentId") Long attachmentId
    ) {
        expenseClaimService.deleteAttachment(id, attachmentId);
        return ApiResponse.ok(null);
    }
}
