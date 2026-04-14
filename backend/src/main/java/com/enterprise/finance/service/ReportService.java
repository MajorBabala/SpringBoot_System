package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.finance.entity.AccountSubject;
import com.enterprise.finance.entity.SubjectBalance;
import com.enterprise.finance.mapper.AccountSubjectMapper;
import com.enterprise.finance.mapper.SubjectBalanceMapper;
import com.enterprise.finance.vo.BalanceSheetItem;
import com.enterprise.finance.vo.BalanceSheetResponse;
import com.enterprise.finance.vo.ProfitStatementResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final AccountSubjectMapper accountSubjectMapper;
    private final SubjectBalanceMapper subjectBalanceMapper;

    public ReportService(AccountSubjectMapper accountSubjectMapper, SubjectBalanceMapper subjectBalanceMapper) {
        this.accountSubjectMapper = accountSubjectMapper;
        this.subjectBalanceMapper = subjectBalanceMapper;
    }

    public BalanceSheetResponse getBalanceSheet(String period) {
        List<AccountSubject> assetSubjects = accountSubjectMapper.selectList(
                new QueryWrapper<AccountSubject>()
                        .eq("subject_type", 1)
                        .eq("level", 1)
                        .eq("status", 1)
                        .orderByAsc("id")
        );
        List<AccountSubject> liabilitySubjects = accountSubjectMapper.selectList(
                new QueryWrapper<AccountSubject>()
                        .eq("subject_type", 2)
                        .eq("level", 1)
                        .eq("status", 1)
                        .orderByAsc("id")
        );
        List<AccountSubject> equitySubjects = accountSubjectMapper.selectList(
                new QueryWrapper<AccountSubject>()
                        .eq("subject_type", 3)
                        .eq("level", 1)
                        .eq("status", 1)
                        .orderByAsc("id")
        );

        BigDecimal totalAsset = BigDecimal.ZERO;
        BigDecimal totalLiability = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        List<BalanceSheetItem> assets = new ArrayList<>();
        for (AccountSubject subject : assetSubjects) {
            SubjectBalance balance = subjectBalanceMapper.selectOne(
                    new QueryWrapper<SubjectBalance>()
                            .eq("subject_id", subject.getId())
                            .eq("period", period)
            );
            BigDecimal amount = balance == null || balance.getEndingDebit() == null ? BigDecimal.ZERO : balance.getEndingDebit();
            assets.add(new BalanceSheetItem(subject.getSubjectName(), amount));
            totalAsset = totalAsset.add(amount);
        }

        List<BalanceSheetItem> liabilities = new ArrayList<>();
        for (AccountSubject subject : liabilitySubjects) {
            SubjectBalance balance = subjectBalanceMapper.selectOne(
                    new QueryWrapper<SubjectBalance>()
                            .eq("subject_id", subject.getId())
                            .eq("period", period)
            );
            BigDecimal amount = balance == null || balance.getEndingCredit() == null ? BigDecimal.ZERO : balance.getEndingCredit();
            liabilities.add(new BalanceSheetItem(subject.getSubjectName(), amount));
            totalLiability = totalLiability.add(amount);
        }

        List<BalanceSheetItem> equity = new ArrayList<>();
        for (AccountSubject subject : equitySubjects) {
            SubjectBalance balance = subjectBalanceMapper.selectOne(
                    new QueryWrapper<SubjectBalance>()
                            .eq("subject_id", subject.getId())
                            .eq("period", period)
            );
            BigDecimal amount = balance == null || balance.getEndingCredit() == null ? BigDecimal.ZERO : balance.getEndingCredit();
            equity.add(new BalanceSheetItem(subject.getSubjectName(), amount));
            totalEquity = totalEquity.add(amount);
        }

        return new BalanceSheetResponse(
                totalAsset,
                assets,
                totalLiability,
                liabilities,
                totalEquity,
                equity,
                totalLiability.add(totalEquity)
        );
    }

    public ProfitStatementResponse getProfitStatement(String period) {
        List<AccountSubject> subjects = accountSubjectMapper.selectList(
                new QueryWrapper<AccountSubject>()
                        .in("subject_type", 4, 5)
                        .eq("level", 1)
                        .eq("status", 1)
                        .orderByAsc("id")
        );

        BigDecimal netProfit = BigDecimal.ZERO;
        for (AccountSubject subject : subjects) {
            SubjectBalance balance = subjectBalanceMapper.selectOne(
                    new QueryWrapper<SubjectBalance>()
                            .eq("subject_id", subject.getId())
                            .eq("period", period)
            );

            BigDecimal endingDebit = balance == null || balance.getEndingDebit() == null
                    ? BigDecimal.ZERO
                    : balance.getEndingDebit();
            BigDecimal endingCredit = balance == null || balance.getEndingCredit() == null
                    ? BigDecimal.ZERO
                    : balance.getEndingCredit();

            if (Integer.valueOf(2).equals(subject.getBalanceDirection())) {
                netProfit = netProfit.add(endingCredit.subtract(endingDebit));
            } else {
                netProfit = netProfit.subtract(endingDebit.subtract(endingCredit));
            }
        }

        return new ProfitStatementResponse(netProfit);
    }

    public String exportCsv(String period, String type) {
        if ("profit-statement".equalsIgnoreCase(type)) {
            ProfitStatementResponse response = getProfitStatement(period);
            return "period,netProfit\n" + period + "," + response.getNetProfit() + "\n";
        }

        BalanceSheetResponse response = getBalanceSheet(period);
        StringBuilder builder = new StringBuilder();
        builder.append("section,item,amount\n");

        for (BalanceSheetItem item : response.getAssets()) {
            builder.append("asset,").append(escapeCsv(item.getSubjectName())).append(",").append(item.getAmount()).append("\n");
        }
        for (BalanceSheetItem item : response.getLiabilities()) {
            builder.append("liability,").append(escapeCsv(item.getSubjectName())).append(",").append(item.getAmount()).append("\n");
        }
        for (BalanceSheetItem item : response.getEquities()) {
            builder.append("equity,").append(escapeCsv(item.getSubjectName())).append(",").append(item.getAmount()).append("\n");
        }

        builder.append("summary,totalAsset,").append(response.getTotalAsset()).append("\n");
        builder.append("summary,totalLiability,").append(response.getTotalLiability()).append("\n");
        builder.append("summary,totalEquity,").append(response.getTotalEquity()).append("\n");
        builder.append("summary,totalLiabilityAndEquity,").append(response.getTotalLiabilityAndEquity()).append("\n");

        return builder.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n")) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
