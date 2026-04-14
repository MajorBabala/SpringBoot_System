package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("voucher")
public class Voucher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String voucherNo;
    private String period;
    private LocalDate voucherDate;
    private String summary;

    private Long makerId;
    private Long auditorId;
    private Long bookkeeperId;

    /**
     * 0未审核 1已审核 2已记账 3作废
     */
    private Integer status;

    private Integer attachmentCount;
}

