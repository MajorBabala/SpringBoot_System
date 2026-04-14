package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.mapper.AccountSubjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountSubjectService {

    private final AccountSubjectMapper accountSubjectMapper;

    public AccountSubjectService(AccountSubjectMapper accountSubjectMapper) {
        this.accountSubjectMapper = accountSubjectMapper;
    }

    public List<AccountSubject> listEnabledSubjects() {
        return accountSubjectMapper.selectList(
                new QueryWrapper<AccountSubject>()
                        .eq("status", 1)
                        .orderByAsc("subject_code")
        );
    }
}
