package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.dto.VoucherCreateRequest;
import com.enterprise.finance.dto.VoucherEntryCreateRequest;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.entity.AccountingPeriod;
import com.enterprise.finance.entity.SubjectBalance;
import com.enterprise.finance.entity.Voucher;
import com.enterprise.finance.entity.VoucherEntry;
import com.enterprise.finance.mapper.AccountSubjectMapper;
import com.enterprise.finance.mapper.AccountingPeriodMapper;
import com.enterprise.finance.mapper.SubjectBalanceMapper;
import com.enterprise.finance.mapper.VoucherEntryMapper;
import com.enterprise.finance.mapper.VoucherMapper;
import com.enterprise.finance.vo.VoucherCreateResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class VoucherService {

    private final VoucherMapper voucherMapper;
    private final VoucherEntryMapper voucherEntryMapper;
    private final SubjectBalanceMapper subjectBalanceMapper;
    private final AccountSubjectMapper accountSubjectMapper;
    private final AccountingPeriodMapper accountingPeriodMapper;

    public VoucherService(VoucherMapper voucherMapper,
                          VoucherEntryMapper voucherEntryMapper,
                          SubjectBalanceMapper subjectBalanceMapper,
                          AccountSubjectMapper accountSubjectMapper,
                          AccountingPeriodMapper accountingPeriodMapper) {
        this.voucherMapper = voucherMapper;
        this.voucherEntryMapper = voucherEntryMapper;
        this.subjectBalanceMapper = subjectBalanceMapper;
        this.accountSubjectMapper = accountSubjectMapper;
        this.accountingPeriodMapper = accountingPeriodMapper;
    }

    @Transactional
    public VoucherCreateResponse createVoucher(VoucherCreateRequest request, Long currentUserId) {
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new BusinessException(400, "Voucher entries cannot be empty");
        }

        if (request.getPeriod() == null || !request.getPeriod().matches("\\d{6}")) {
            throw new BusinessException(400, "Period must use YYYYMM format");
        }
        AccountingPeriod accountingPeriod = accountingPeriodMapper.selectOne(
                new QueryWrapper<AccountingPeriod>().eq("period", request.getPeriod())
        );
        if (accountingPeriod == null || !Integer.valueOf(1).equals(accountingPeriod.getStatus())) {
            throw new BusinessException(400, "Accounting period is not opened: " + request.getPeriod());
        }

        BigDecimal totalDebit = request.getEntries().stream()
                .map(VoucherEntryCreateRequest::getDebitAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = request.getEntries().stream()
                .map(VoucherEntryCreateRequest::getCreditAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException(400, "Debit amount must equal credit amount");
        }

        for (VoucherEntryCreateRequest entryReq : request.getEntries()) {
            validateEntry(entryReq);
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherNo(request.getVoucherNo());
        voucher.setPeriod(request.getPeriod());
        voucher.setVoucherDate(request.getVoucherDate());
        voucher.setSummary(request.getSummary());
        voucher.setMakerId(currentUserId);
        voucher.setStatus(0);
        voucher.setAttachmentCount(0);

        try {
            voucherMapper.insert(voucher);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(400, "Voucher number already exists in this period");
        }

        List<VoucherEntry> entries = new ArrayList<>();
        for (VoucherEntryCreateRequest entryReq : request.getEntries()) {
            VoucherEntry entry = new VoucherEntry();
            entry.setVoucherId(voucher.getId());
            entry.setSubjectId(entryReq.getSubjectId());
            entry.setSummary(entryReq.getSummary());
            entry.setDebitAmount(defaultZero(entryReq.getDebitAmount()));
            entry.setCreditAmount(defaultZero(entryReq.getCreditAmount()));
            entry.setAuxiliaryType(entryReq.getAuxiliaryType());
            entry.setAuxiliaryId(entryReq.getAuxiliaryId());
            entries.add(entry);
        }

        for (VoucherEntry entry : entries) {
            voucherEntryMapper.insert(entry);
        }

        return new VoucherCreateResponse(voucher.getId(), voucher.getStatus());
    }

    public Page<Voucher> listVouchers(int pageNum, int pageSize, Integer status, String period) {
        Page<Voucher> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Voucher> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        if (period != null && !period.isBlank()) {
            qw.eq("period", period);
        }
        qw.orderByDesc("id");
        return voucherMapper.selectPage(page, qw);
    }

    public Voucher getVoucherById(Long id) {
        Voucher voucher = voucherMapper.selectById(id);
        if (voucher == null) {
            throw new BusinessException(404, "Voucher does not exist");
        }
        return voucher;
    }

    public List<VoucherEntry> getVoucherEntries(Long voucherId) {
        return voucherEntryMapper.selectList(new QueryWrapper<VoucherEntry>().eq("voucher_id", voucherId).orderByAsc("id"));
    }

    @Transactional
    public void auditVoucher(Long voucherId, Long auditorId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException(404, "Voucher does not exist");
        }
        if (!Integer.valueOf(0).equals(voucher.getStatus())) {
            throw new BusinessException(400, "Only unaudited vouchers can be audited");
        }
        voucher.setAuditorId(auditorId);
        voucher.setStatus(1);
        voucherMapper.updateById(voucher);
    }

    @Transactional
    public void bookkeepVoucher(Long voucherId, Long bookkeeperId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException(404, "Voucher does not exist");
        }
        if (!Integer.valueOf(1).equals(voucher.getStatus())) {
            throw new BusinessException(400, "Only audited vouchers can be booked");
        }

        String period = voucher.getPeriod();
        List<VoucherEntry> entries = getVoucherEntries(voucherId);
        if (entries == null || entries.isEmpty()) {
            throw new BusinessException(400, "Voucher entries do not exist");
        }

        for (VoucherEntry entry : entries) {
            AccountSubject subject = accountSubjectMapper.selectById(entry.getSubjectId());
            if (subject == null || !Objects.equals(subject.getStatus(), 1)) {
                throw new BusinessException(400, "Invalid subject: " + entry.getSubjectId());
            }

            SubjectBalance balance = subjectBalanceMapper.selectOne(
                    new QueryWrapper<SubjectBalance>()
                            .eq("subject_id", entry.getSubjectId())
                            .eq("period", period)
            );

            if (balance == null) {
                balance = initBalanceFromPrevious(entry.getSubjectId(), period);
                subjectBalanceMapper.insert(balance);
            }

            BigDecimal debitAmount = defaultZero(entry.getDebitAmount());
            BigDecimal creditAmount = defaultZero(entry.getCreditAmount());

            BigDecimal newDebit = defaultZero(balance.getDebitAmount()).add(debitAmount);
            BigDecimal newCredit = defaultZero(balance.getCreditAmount()).add(creditAmount);

            balance.setDebitAmount(newDebit);
            balance.setCreditAmount(newCredit);

            BigDecimal openingDebit = defaultZero(balance.getOpeningDebit());
            BigDecimal openingCredit = defaultZero(balance.getOpeningCredit());

            balance.setEndingDebit(openingDebit.add(newDebit));
            balance.setEndingCredit(openingCredit.add(newCredit));

            subjectBalanceMapper.updateById(balance);
        }

        voucher.setBookkeeperId(bookkeeperId);
        voucher.setStatus(2);
        voucherMapper.updateById(voucher);
    }

    private void validateEntry(VoucherEntryCreateRequest entryReq) {
        if (entryReq.getSubjectId() == null) {
            throw new BusinessException(400, "Subject ID cannot be empty");
        }

        AccountSubject subject = accountSubjectMapper.selectById(entryReq.getSubjectId());
        if (subject == null) {
            throw new BusinessException(400, "Subject does not exist: " + entryReq.getSubjectId());
        }
        if (!Objects.equals(subject.getStatus(), 1)) {
            throw new BusinessException(400, "Subject is disabled: " + entryReq.getSubjectId());
        }

        BigDecimal debit = defaultZero(entryReq.getDebitAmount());
        BigDecimal credit = defaultZero(entryReq.getCreditAmount());

        if (debit.compareTo(BigDecimal.ZERO) < 0 || credit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "Entry amount cannot be negative");
        }
        if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(400, "Each entry must have a debit or credit amount");
        }
    }

    private SubjectBalance initBalanceFromPrevious(Long subjectId, String period) {
        YearMonth ym = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyyMM"));
        YearMonth prev = ym.minusMonths(1);
        String prevPeriod = prev.format(DateTimeFormatter.ofPattern("yyyyMM"));

        SubjectBalance prevBalance = subjectBalanceMapper.selectOne(
                new QueryWrapper<SubjectBalance>()
                        .eq("subject_id", subjectId)
                        .eq("period", prevPeriod)
        );

        BigDecimal openingDebit = prevBalance == null ? BigDecimal.ZERO : defaultZero(prevBalance.getEndingDebit());
        BigDecimal openingCredit = prevBalance == null ? BigDecimal.ZERO : defaultZero(prevBalance.getEndingCredit());

        SubjectBalance balance = new SubjectBalance();
        balance.setSubjectId(subjectId);
        balance.setPeriod(period);
        balance.setOpeningDebit(openingDebit);
        balance.setOpeningCredit(openingCredit);
        balance.setDebitAmount(BigDecimal.ZERO);
        balance.setCreditAmount(BigDecimal.ZERO);
        balance.setEndingDebit(openingDebit);
        balance.setEndingCredit(openingCredit);
        return balance;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
