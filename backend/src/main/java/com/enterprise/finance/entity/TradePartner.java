package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trade_partner")
public class TradePartner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partnerCode;
    private String partnerName;
    private Integer partnerType;
    private String contactName;
    private String phone;
    private String address;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
