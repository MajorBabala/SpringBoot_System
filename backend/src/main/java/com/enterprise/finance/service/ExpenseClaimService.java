package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.dto.ExpenseClaimCreateRequest;
import com.enterprise.finance.dto.ExpenseClaimDetailCreateRequest;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.entity.AccountingPeriod;
import com.enterprise.finance.entity.Dept;
import com.enterprise.finance.entity.ExpenseClaim;
import com.enterprise.finance.entity.ExpenseClaimAttachment;
import com.enterprise.finance.entity.ExpenseClaimDetail;
import com.enterprise.finance.entity.Voucher;
import com.enterprise.finance.entity.VoucherEntry;
import com.enterprise.finance.mapper.AccountSubjectMapper;
import com.enterprise.finance.mapper.AccountingPeriodMapper;
import com.enterprise.finance.mapper.DeptMapper;
import com.enterprise.finance.mapper.ExpenseClaimAttachmentMapper;
import com.enterprise.finance.mapper.ExpenseClaimDetailMapper;
import com.enterprise.finance.mapper.ExpenseClaimMapper;
import com.enterprise.finance.mapper.VoucherEntryMapper;
import com.enterprise.finance.mapper.VoucherMapper;
import com.enterprise.finance.vo.ExpenseClaimAttachmentResponse;
import com.enterprise.finance.vo.ExpenseClaimDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseClaimService {

    private static final long BANK_DEPOSIT_SUBJECT_ID = 2L;

    private final ExpenseClaimMapper expenseClaimMapper;
    private final ExpenseClaimDetailMapper expenseClaimDetailMapper;
    private final ExpenseClaimAttachmentMapper expenseClaimAttachmentMapper;
    private final DeptMapper deptMapper;
    private final AccountingPeriodMapper accountingPeriodMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherEntryMapper voucherEntryMapper;
    private final AccountSubjectMapper accountSubjectMapper;

    public ExpenseClaimService(ExpenseClaimMapper expenseClaimMapper,
                               ExpenseClaimDetailMapper expenseClaimDetailMapper,
                               ExpenseClaimAttachmentMapper expenseClaimAttachmentMapper,
                               DeptMapper deptMapper,
                               AccountingPeriodMapper accountingPeriodMapper,
                               VoucherMapper voucherMapper,
                               VoucherEntryMapper voucherEntryMapper,
                               AccountSubjectMapper accountSubjectMapper) {
        this.expenseClaimMapper = expenseClaimMapper;
        this.expenseClaimDetailMapper = expenseClaimDetailMapper;
        this.expenseClaimAttachmentMapper = expenseClaimAttachmentMapper;
        this.deptMapper = deptMapper;
        this.accountingPeriodMapper = accountingPeriodMapper;
        this.voucherMapper = voucherMapper;
        this.voucherEntryMapper = voucherEntryMapper;
        this.accountSubjectMapper = accountSubjectMapper;
    }

    @Transactional
    public Long createExpenseClaim(ExpenseClaimCreateRequest request, Long applicantId) {
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new BusinessException(400, "Expense claim details cannot be empty");
        }

        Dept dept = deptMapper.selectById(request.getDeptId());
        if (dept == null || !Objects.equals(dept.getStatus(), 1)) {
            throw new BusinessException(400, "Invalid department: " + request.getDeptId());
        }
        String period = request.getPeriod() == null ? null : request.getPeriod().trim();
        if (period == null || !period.matches("\\d{6}")) {
            throw new BusinessException(400, "Period must use YYYYMM format");
        }
        AccountingPeriod accountingPeriod = accountingPeriodMapper.selectOne(
                new QueryWrapper<AccountingPeriod>().eq("period", period)
        );
        if (accountingPeriod == null || !Objects.equals(accountingPeriod.getStatus(), 1)) {
            throw new BusinessException(400, "Accounting period is not opened: " + period);
        }

        BigDecimal computedTotal = request.getDetails().stream()
                .map(ExpenseClaimDetailCreateRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getTotalAmount() != null && request.getTotalAmount().compareTo(computedTotal) != 0) {
            throw new BusinessException(400, "Total amount must equal detail amount sum");
        }

        BigDecimal totalAmount = request.getTotalAmount() == null ? computedTotal : request.getTotalAmount();

        ExpenseClaim claim = new ExpenseClaim();
        claim.setClaimNo(request.getClaimNo());
        claim.setPeriod(period);
        claim.setReason(request.getReason());
        claim.setDeptId(request.getDeptId());
        claim.setApplicantId(applicantId);
        claim.setExpenseType(request.getExpenseType());
        claim.setTotalAmount(totalAmount);
        claim.setStatus(0);

        expenseClaimMapper.insert(claim);

        for (int i = 0; i < request.getDetails().size(); i++) {
            ExpenseClaimDetailCreateRequest detailRequest = request.getDetails().get(i);
            ExpenseClaimDetail detail = new ExpenseClaimDetail();
            detail.setExpenseClaimId(claim.getId());
            detail.setLineNo(i + 1);
            detail.setItemName(detailRequest.getItemName());
            detail.setSubjectId(detailRequest.getSubjectId());
            detail.setExpenseType(detailRequest.getExpenseType() == null ? request.getExpenseType() : detailRequest.getExpenseType());
            detail.setAmount(detailRequest.getAmount());
            detail.setRemark(detailRequest.getRemark());
            expenseClaimDetailMapper.insert(detail);
        }

        return claim.getId();
    }

    public Page<ExpenseClaim> listExpenseClaims(int pageNum, int pageSize, Integer status) {
        Page<ExpenseClaim> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ExpenseClaim> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("id");
        return expenseClaimMapper.selectPage(page, queryWrapper);
    }

    public ExpenseClaimDetailResponse getDetail(Long id) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
        List<ExpenseClaimDetail> details = expenseClaimDetailMapper.selectList(
                new QueryWrapper<ExpenseClaimDetail>().eq("expense_claim_id", id).orderByAsc("line_no")
        );
        List<ExpenseClaimAttachmentResponse> attachments = listAttachments(id);
        return new ExpenseClaimDetailResponse(claim, details, attachments);
    }

    @Transactional
    public void submit(Long id) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
        if (!Integer.valueOf(0).equals(claim.getStatus())) {
            throw new BusinessException(400, "Only draft claims can be submitted");
        }
        claim.setStatus(1);
        expenseClaimMapper.updateById(claim);
    }

    @Transactional
    public void approve(Long id, Long approverId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
        if (!Integer.valueOf(1).equals(claim.getStatus())) {
            throw new BusinessException(400, "Only pending claims can be approved");
        }
        claim.setApproverId(approverId);
        claim.setApproveTime(LocalDateTime.now());
        claim.setStatus(2);
        expenseClaimMapper.updateById(claim);
    }

    @Transactional
    public void pay(Long id, Long payerId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
        if (!Integer.valueOf(2).equals(claim.getStatus())) {
            throw new BusinessException(400, "Only approved claims can be paid");
        }
        claim.setPayerId(payerId);
        claim.setPaymentTime(LocalDateTime.now());
        claim.setStatus(3);
        expenseClaimMapper.updateById(claim);
    }

    @Transactional
    public Long generateVoucher(Long id, Long makerId) {
        ExpenseClaim claim = expenseClaimMapper.selectById(id);
        if (claim == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
        if (!Integer.valueOf(2).equals(claim.getStatus()) && !Integer.valueOf(3).equals(claim.getStatus())) {
            throw new BusinessException(400, "Only approved or paid claims can generate voucher");
        }
        if (claim.getVoucherId() != null) {
            return claim.getVoucherId();
        }

        List<ExpenseClaimDetail> details = expenseClaimDetailMapper.selectList(
                new QueryWrapper<ExpenseClaimDetail>().eq("expense_claim_id", id).orderByAsc("line_no")
        );
        if (details.isEmpty()) {
            throw new BusinessException(400, "Expense claim details do not exist");
        }

        Voucher voucher = createVoucherForClaim(claim, details, makerId);
        claim.setVoucherId(voucher.getId());
        expenseClaimMapper.updateById(claim);
        return voucher.getId();
    }

    public List<ExpenseClaimAttachmentResponse> listAttachments(Long claimId) {
        ensureClaimExists(claimId);
        return expenseClaimAttachmentMapper.selectList(
                        new QueryWrapper<ExpenseClaimAttachment>()
                                .eq("expense_claim_id", claimId)
                                .orderByDesc("id"))
                .stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseClaimAttachmentResponse uploadAttachment(Long claimId, MultipartFile file, Long uploaderId) {
        ensureClaimExists(claimId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "Attachment file cannot be empty");
        }

        try {
            String originalName = file.getOriginalFilename();
            String safeName = (originalName == null || originalName.isBlank())
                    ? "file.bin"
                    : Paths.get(originalName).getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + safeName;

            Path baseDir = Paths.get(System.getProperty("user.dir"), "uploads", "expense-claims", String.valueOf(claimId));
            Files.createDirectories(baseDir);
            Path targetPath = baseDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            ExpenseClaimAttachment attachment = new ExpenseClaimAttachment();
            attachment.setExpenseClaimId(claimId);
            attachment.setFileName(safeName);
            attachment.setFilePath(targetPath.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploaderId(uploaderId);
            attachment.setUploadTime(LocalDateTime.now());
            expenseClaimAttachmentMapper.insert(attachment);

            return toAttachmentResponse(attachment);
        } catch (Exception ex) {
            throw new BusinessException(500, "Attachment upload failed");
        }
    }

    @Transactional
    public void deleteAttachment(Long claimId, Long attachmentId) {
        ensureClaimExists(claimId);
        ExpenseClaimAttachment attachment = expenseClaimAttachmentMapper.selectById(attachmentId);
        if (attachment == null || !Objects.equals(attachment.getExpenseClaimId(), claimId)) {
            throw new BusinessException(404, "Attachment does not exist");
        }

        if (attachment.getFilePath() != null && !attachment.getFilePath().isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            } catch (Exception ignored) {
                // best effort
            }
        }
        expenseClaimAttachmentMapper.deleteById(attachmentId);
    }

    private ExpenseClaimAttachmentResponse toAttachmentResponse(ExpenseClaimAttachment item) {
        ExpenseClaimAttachmentResponse resp = new ExpenseClaimAttachmentResponse();
        resp.setId(item.getId());
        resp.setExpenseClaimId(item.getExpenseClaimId());
        resp.setFileName(item.getFileName());
        resp.setFilePath(item.getFilePath());
        resp.setFileSize(item.getFileSize());
        resp.setContentType(item.getContentType());
        resp.setUploadTime(item.getUploadTime());
        return resp;
    }

    private void ensureClaimExists(Long claimId) {
        if (expenseClaimMapper.selectById(claimId) == null) {
            throw new BusinessException(404, "Expense claim does not exist");
        }
    }

    private Voucher createVoucherForClaim(ExpenseClaim claim, List<ExpenseClaimDetail> details, Long makerId) {
        AccountSubject bankSubject = accountSubjectMapper.selectById(BANK_DEPOSIT_SUBJECT_ID);
        if (bankSubject == null || !Objects.equals(bankSubject.getStatus(), 1)) {
            throw new BusinessException(500, "Bank deposit subject is missing");
        }

        String period = claim.getPeriod();
        LocalDate voucherDate = LocalDate.now();
        if (period != null && period.matches("\\d{6}")) {
            voucherDate = LocalDate.parse(period + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherNo("EXP-" + claim.getId());
        voucher.setPeriod(period);
        voucher.setVoucherDate(voucherDate);
        voucher.setSummary("Expense claim " + claim.getClaimNo() + " auto voucher");
        voucher.setMakerId(makerId);
        voucher.setStatus(0);
        voucher.setAttachmentCount(details.size());
        voucherMapper.insert(voucher);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ExpenseClaimDetail detail : details) {
            AccountSubject subject = accountSubjectMapper.selectById(detail.getSubjectId());
            if (subject == null || !Objects.equals(subject.getStatus(), 1)) {
                throw new BusinessException(400, "Invalid expense subject: " + detail.getSubjectId());
            }

            VoucherEntry debitEntry = new VoucherEntry();
            debitEntry.setVoucherId(voucher.getId());
            debitEntry.setSubjectId(detail.getSubjectId());
            debitEntry.setSummary(detail.getItemName());
            debitEntry.setDebitAmount(defaultZero(detail.getAmount()));
            debitEntry.setCreditAmount(BigDecimal.ZERO);
            voucherEntryMapper.insert(debitEntry);

            totalAmount = totalAmount.add(defaultZero(detail.getAmount()));
        }

        VoucherEntry creditEntry = new VoucherEntry();
        creditEntry.setVoucherId(voucher.getId());
        creditEntry.setSubjectId(BANK_DEPOSIT_SUBJECT_ID);
        creditEntry.setSummary("Expense payment for " + claim.getClaimNo());
        creditEntry.setDebitAmount(BigDecimal.ZERO);
        creditEntry.setCreditAmount(totalAmount);
        voucherEntryMapper.insert(creditEntry);

        return voucher;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
