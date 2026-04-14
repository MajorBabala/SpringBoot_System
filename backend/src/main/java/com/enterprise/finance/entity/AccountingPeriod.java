package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("accounting_period")
public class AccountingPeriod {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime closeTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
