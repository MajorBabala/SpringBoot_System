package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("subject_balance")
public class SubjectBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;
    private String period;

    private BigDecimal openingDebit;
    private BigDecimal openingCredit;

    private BigDecimal debitAmount;
    private BigDecimal creditAmount;

    private BigDecimal endingDebit;
    private BigDecimal endingCredit;
}

