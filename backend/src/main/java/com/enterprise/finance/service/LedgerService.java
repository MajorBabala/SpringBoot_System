package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.entity.SubjectBalance;
import com.enterprise.finance.entity.Voucher;
import com.enterprise.finance.entity.VoucherEntry;
import com.enterprise.finance.mapper.AccountSubjectMapper;
import com.enterprise.finance.mapper.SubjectBalanceMapper;
import com.enterprise.finance.mapper.VoucherEntryMapper;
import com.enterprise.finance.mapper.VoucherMapper;
import com.enterprise.finance.vo.DetailLedgerItemResponse;
import com.enterprise.finance.vo.GeneralLedgerItemResponse;
import com.enterprise.finance.vo.JournalEntryItemResponse;
import com.enterprise.finance.vo.SubjectBalanceListItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final SubjectBalanceMapper subjectBalanceMapper;
    private final AccountSubjectMapper accountSubjectMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherEntryMapper voucherEntryMapper;

    public LedgerService(SubjectBalanceMapper subjectBalanceMapper,
                         AccountSubjectMapper accountSubjectMapper,
                         VoucherMapper voucherMapper,
                         VoucherEntryMapper voucherEntryMapper) {
        this.subjectBalanceMapper = subjectBalanceMapper;
        this.accountSubjectMapper = accountSubjectMapper;
        this.voucherMapper = voucherMapper;
        this.voucherEntryMapper = voucherEntryMapper;
    }

    public IPage<SubjectBalanceListItemResponse> pageSubjectBalances(
            long page,
            long limit,
            String period,
            String keyword
    ) {
        Page<SubjectBalance> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<SubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(period), SubjectBalance::getPeriod, period != null ? period.trim() : null);

        Set<Long> matchedSubjectIds = null;
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            List<AccountSubject> matchedSubjects = accountSubjectMapper.selectList(
                    new LambdaQueryWrapper<AccountSubject>()
                            .like(AccountSubject::getSubjectCode, kw)
                            .or()
                            .like(AccountSubject::getSubjectName, kw)
            );
            if (matchedSubjects.isEmpty()) {
                return new Page<>(pageParam.getCurrent(), pageParam.getSize(), 0);
            }
            matchedSubjectIds = matchedSubjects.stream()
                    .map(AccountSubject::getId)
                    .collect(Collectors.toSet());
            wrapper.in(SubjectBalance::getSubjectId, matchedSubjectIds);
        }

        wrapper.orderByDesc(SubjectBalance::getPeriod)
                .orderByAsc(SubjectBalance::getSubjectId)
                .orderByAsc(SubjectBalance::getId);

        Page<SubjectBalance> balancePage = subjectBalanceMapper.selectPage(pageParam, wrapper);

        List<Long> subjectIds = balancePage.getRecords().stream()
                .map(SubjectBalance::getSubjectId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, AccountSubject> subjectMap;
        if (subjectIds.isEmpty()) {
            subjectMap = Collections.emptyMap();
        } else {
            subjectMap = accountSubjectMapper.selectBatchIds(subjectIds).stream()
                    .collect(Collectors.toMap(AccountSubject::getId, Function.identity(), (a, b) -> a));
        }

        List<SubjectBalanceListItemResponse> records = balancePage.getRecords().stream()
                .map(item -> {
                    SubjectBalanceListItemResponse row = new SubjectBalanceListItemResponse();
                    row.setId(item.getId());
                    row.setSubjectId(item.getSubjectId());
                    row.setPeriod(item.getPeriod());
                    row.setOpeningDebit(item.getOpeningDebit());
                    row.setOpeningCredit(item.getOpeningCredit());
                    row.setDebitAmount(item.getDebitAmount());
                    row.setCreditAmount(item.getCreditAmount());
                    row.setEndingDebit(item.getEndingDebit());
                    row.setEndingCredit(item.getEndingCredit());
                    AccountSubject subject = subjectMap.get(item.getSubjectId());
                    if (subject != null) {
                        row.setSubjectCode(subject.getSubjectCode());
                        row.setSubjectName(subject.getSubjectName());
                    }
                    return row;
                })
                .collect(Collectors.toList());

        Page<SubjectBalanceListItemResponse> result = new Page<>(
                balancePage.getCurrent(),
                balancePage.getSize(),
                balancePage.getTotal()
        );
        result.setRecords(records);
        return result;
    }

    public IPage<GeneralLedgerItemResponse> pageGeneralLedger(long page, long limit, String period, String keyword) {
        Page<SubjectBalanceListItemResponse> source = (Page<SubjectBalanceListItemResponse>) pageSubjectBalances(page, limit, period, keyword);
        List<GeneralLedgerItemResponse> records = source.getRecords().stream().map(item -> {
            GeneralLedgerItemResponse row = new GeneralLedgerItemResponse();
            row.setSubjectId(item.getSubjectId());
            row.setSubjectCode(item.getSubjectCode());
            row.setSubjectName(item.getSubjectName());
            row.setPeriod(item.getPeriod());
            row.setOpeningDebit(defaultZero(item.getOpeningDebit()));
            row.setOpeningCredit(defaultZero(item.getOpeningCredit()));
            row.setDebitAmount(defaultZero(item.getDebitAmount()));
            row.setCreditAmount(defaultZero(item.getCreditAmount()));
            row.setEndingDebit(defaultZero(item.getEndingDebit()));
            row.setEndingCredit(defaultZero(item.getEndingCredit()));
            return row;
        }).collect(Collectors.toList());

        Page<GeneralLedgerItemResponse> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }

    public IPage<DetailLedgerItemResponse> pageDetailLedger(long page, long limit, String period, Long subjectId) {
        if (subjectId == null) {
            return new Page<>(page, limit, 0);
        }

        List<Voucher> vouchers = voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .eq(StringUtils.hasText(period), Voucher::getPeriod, period != null ? period.trim() : null)
                        .eq(Voucher::getStatus, 2)
                        .orderByAsc(Voucher::getVoucherDate)
                        .orderByAsc(Voucher::getId)
        );
        if (vouchers.isEmpty()) {
            return new Page<>(page, limit, 0);
        }

        Map<Long, Voucher> voucherMap = vouchers.stream().collect(Collectors.toMap(Voucher::getId, Function.identity(), (a, b) -> a));
        List<Long> voucherIds = vouchers.stream().map(Voucher::getId).collect(Collectors.toList());

        List<VoucherEntry> entries = voucherEntryMapper.selectList(
                new LambdaQueryWrapper<VoucherEntry>()
                        .in(VoucherEntry::getVoucherId, voucherIds)
                        .eq(VoucherEntry::getSubjectId, subjectId)
                        .orderByAsc(VoucherEntry::getVoucherId)
                        .orderByAsc(VoucherEntry::getId)
        );
        if (entries.isEmpty()) {
            return new Page<>(page, limit, 0);
        }

        AccountSubject subject = accountSubjectMapper.selectById(subjectId);
        SubjectBalance balance = subjectBalanceMapper.selectOne(
                new LambdaQueryWrapper<SubjectBalance>()
                        .eq(SubjectBalance::getSubjectId, subjectId)
                        .eq(StringUtils.hasText(period), SubjectBalance::getPeriod, period != null ? period.trim() : null)
        );
        BigDecimal runningDebit = defaultZero(balance == null ? null : balance.getOpeningDebit());
        BigDecimal runningCredit = defaultZero(balance == null ? null : balance.getOpeningCredit());

        List<DetailLedgerItemResponse> rows = entries.stream().map(entry -> {
            Voucher voucher = voucherMap.get(entry.getVoucherId());
            DetailLedgerItemResponse row = new DetailLedgerItemResponse();
            row.setVoucherId(entry.getVoucherId());
            row.setVoucherNo(voucher == null ? null : voucher.getVoucherNo());
            row.setVoucherDate(voucher == null ? null : voucher.getVoucherDate());
            row.setPeriod(voucher == null ? null : voucher.getPeriod());
            row.setSummary(StringUtils.hasText(entry.getSummary()) ? entry.getSummary() : (voucher == null ? null : voucher.getSummary()));
            row.setSubjectId(subjectId);
            row.setSubjectCode(subject == null ? null : subject.getSubjectCode());
            row.setSubjectName(subject == null ? null : subject.getSubjectName());
            row.setDebitAmount(defaultZero(entry.getDebitAmount()));
            row.setCreditAmount(defaultZero(entry.getCreditAmount()));
            return row;
        }).collect(Collectors.toList());

        for (DetailLedgerItemResponse row : rows) {
            runningDebit = runningDebit.add(defaultZero(row.getDebitAmount()));
            runningCredit = runningCredit.add(defaultZero(row.getCreditAmount()));
            row.setRunningDebit(runningDebit);
            row.setRunningCredit(runningCredit);
        }

        return pageFromList(rows, page, limit);
    }

    public IPage<JournalEntryItemResponse> pageJournal(long page, long limit, String period, String keyword) {
        List<Voucher> vouchers = voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .eq(StringUtils.hasText(period), Voucher::getPeriod, period != null ? period.trim() : null)
                        .eq(Voucher::getStatus, 2)
                        .orderByAsc(Voucher::getVoucherDate)
                        .orderByAsc(Voucher::getId)
        );
        if (vouchers.isEmpty()) {
            return new Page<>(page, limit, 0);
        }

        Map<Long, Voucher> voucherMap = vouchers.stream().collect(Collectors.toMap(Voucher::getId, Function.identity(), (a, b) -> a));
        List<Long> voucherIds = vouchers.stream().map(Voucher::getId).collect(Collectors.toList());

        Set<Long> matchedSubjectIds = null;
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            List<AccountSubject> subjects = accountSubjectMapper.selectList(
                    new LambdaQueryWrapper<AccountSubject>()
                            .like(AccountSubject::getSubjectCode, kw)
                            .or()
                            .like(AccountSubject::getSubjectName, kw)
            );
            if (subjects.isEmpty()) {
                return new Page<>(page, limit, 0);
            }
            matchedSubjectIds = subjects.stream().map(AccountSubject::getId).collect(Collectors.toSet());
        }

        LambdaQueryWrapper<VoucherEntry> wrapper = new LambdaQueryWrapper<VoucherEntry>()
                .in(VoucherEntry::getVoucherId, voucherIds);
        if (matchedSubjectIds != null && !matchedSubjectIds.isEmpty()) {
            wrapper.in(VoucherEntry::getSubjectId, matchedSubjectIds);
        }
        wrapper.orderByAsc(VoucherEntry::getVoucherId)
                .orderByAsc(VoucherEntry::getId);

        List<VoucherEntry> entries = voucherEntryMapper.selectList(wrapper);
        if (entries.isEmpty()) {
            return new Page<>(page, limit, 0);
        }

        Set<Long> subjectIds = entries.stream().map(VoucherEntry::getSubjectId).collect(Collectors.toSet());
        Map<Long, AccountSubject> subjectMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : accountSubjectMapper.selectBatchIds(subjectIds).stream()
                .collect(Collectors.toMap(AccountSubject::getId, Function.identity(), (a, b) -> a));

        List<JournalEntryItemResponse> rows = entries.stream().map(entry -> {
            Voucher voucher = voucherMap.get(entry.getVoucherId());
            AccountSubject subject = subjectMap.get(entry.getSubjectId());
            JournalEntryItemResponse row = new JournalEntryItemResponse();
            row.setVoucherId(entry.getVoucherId());
            row.setVoucherNo(voucher == null ? null : voucher.getVoucherNo());
            row.setVoucherDate(voucher == null ? null : voucher.getVoucherDate());
            row.setPeriod(voucher == null ? null : voucher.getPeriod());
            row.setSummary(StringUtils.hasText(entry.getSummary()) ? entry.getSummary() : (voucher == null ? null : voucher.getSummary()));
            row.setSubjectId(entry.getSubjectId());
            row.setSubjectCode(subject == null ? null : subject.getSubjectCode());
            row.setSubjectName(subject == null ? null : subject.getSubjectName());
            row.setDebitAmount(defaultZero(entry.getDebitAmount()));
            row.setCreditAmount(defaultZero(entry.getCreditAmount()));
            return row;
        }).collect(Collectors.toList());

        return pageFromList(rows, page, limit);
    }

    private <T> IPage<T> pageFromList(List<T> list, long page, long limit) {
        int from = Math.max(0, (int) ((page - 1) * limit));
        int to = Math.min(list.size(), from + (int) limit);
        List<T> records = from >= list.size() ? Collections.emptyList() : list.subList(from, to);
        Page<T> result = new Page<>(page, limit, list.size());
        result.setRecords(records);
        return result;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
