package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("voucher_entry")
public class VoucherEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long voucherId;
    private Long subjectId;
    private String summary;

    private BigDecimal debitAmount;
    private BigDecimal creditAmount;

    private Integer auxiliaryType;
    private Long auxiliaryId;
}

