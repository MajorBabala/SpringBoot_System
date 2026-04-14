package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.entity.AccountingPeriod;
import com.enterprise.finance.mapper.AccountingPeriodMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AccountingPeriodService {

    private final AccountingPeriodMapper accountingPeriodMapper;

    public AccountingPeriodService(AccountingPeriodMapper accountingPeriodMapper) {
        this.accountingPeriodMapper = accountingPeriodMapper;
    }

    public Page<AccountingPeriod> page(int pageNum, int pageSize, String keyword, Integer status) {
        Page<AccountingPeriod> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), AccountingPeriod::getPeriod, keyword == null ? null : keyword.trim());
        wrapper.eq(status != null, AccountingPeriod::getStatus, status);
        wrapper.orderByDesc(AccountingPeriod::getPeriod);
        return accountingPeriodMapper.selectPage(page, wrapper);
    }

    public AccountingPeriod getById(Long id) {
        AccountingPeriod item = accountingPeriodMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "Accounting period does not exist");
        }
        return item;
    }

    public Long create(AccountingPeriod request) {
        validate(request, null);
        if (request.getStatus() == null) {
            request.setStatus(0);
        }
        accountingPeriodMapper.insert(request);
        return request.getId();
    }

    public void update(Long id, AccountingPeriod request) {
        AccountingPeriod exists = getById(id);
        if (exists.getStatus() != null && exists.getStatus() == 2) {
            throw new BusinessException(400, "Closed period cannot be edited");
        }
        request.setId(exists.getId());
        request.setStatus(exists.getStatus());
        request.setCloseTime(exists.getCloseTime());
        validate(request, id);
        accountingPeriodMapper.updateById(request);
    }

    public void delete(Long id) {
        AccountingPeriod exists = getById(id);
        if (exists.getStatus() != null && exists.getStatus() != 0) {
            throw new BusinessException(400, "Only draft period can be deleted");
        }
        accountingPeriodMapper.deleteById(id);
    }

    @Transactional
    public void open(Long id) {
        AccountingPeriod target = getById(id);
        if (target.getStatus() != null && target.getStatus() == 2) {
            throw new BusinessException(400, "Closed period cannot be reopened");
        }
        accountingPeriodMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AccountingPeriod>()
                .set(AccountingPeriod::getStatus, 2)
                .eq(AccountingPeriod::getStatus, 1));

        target.setStatus(1);
        target.setCloseTime(null);
        accountingPeriodMapper.updateById(target);
    }

    public void close(Long id) {
        AccountingPeriod target = getById(id);
        if (target.getStatus() == null || target.getStatus() != 1) {
            throw new BusinessException(400, "Only opened period can be closed");
        }
        target.setStatus(2);
        target.setCloseTime(LocalDateTime.now());
        accountingPeriodMapper.updateById(target);
    }

    private void validate(AccountingPeriod request, Long id) {
        if (request == null) {
            throw new BusinessException(400, "Accounting period data is required");
        }
        if (!StringUtils.hasText(request.getPeriod()) || !request.getPeriod().trim().matches("\\d{6}")) {
            throw new BusinessException(400, "Period must use YYYYMM format");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException(400, "Start and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(400, "Start date cannot be later than end date");
        }

        String period = request.getPeriod().trim();
        LambdaQueryWrapper<AccountingPeriod> wrapper = new LambdaQueryWrapper<AccountingPeriod>()
                .eq(AccountingPeriod::getPeriod, period);
        if (id != null) {
            wrapper.ne(AccountingPeriod::getId, id);
        }
        if (accountingPeriodMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "Period already exists");
        }
        request.setPeriod(period);
    }
}
