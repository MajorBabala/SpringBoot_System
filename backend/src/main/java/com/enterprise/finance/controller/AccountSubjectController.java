package com.enterprise.finance.controller;

import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.service.AccountSubjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account-subjects")
public class AccountSubjectController {

    private final AccountSubjectService accountSubjectService;

    public AccountSubjectController(AccountSubjectService accountSubjectService) {
        this.accountSubjectService = accountSubjectService;
    }

    @GetMapping
    public ApiResponse<List<AccountSubject>> list() {
        return ApiResponse.ok(accountSubjectService.listEnabledSubjects());
    }
}
