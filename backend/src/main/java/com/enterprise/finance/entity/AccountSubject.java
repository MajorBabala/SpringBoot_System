package com.enterprise.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会计科目
 */
@Data
@TableName("account_subject")
public class AccountSubject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectCode;
    private String subjectName;

    /**
     * 1资产 2负债 3权益 4成本 5损益
     */
    private Integer subjectType;

    private Long parentId;
    private Integer level;

    /**
     * 1借 2贷
     */
    private Integer balanceDirection;

    private Integer status;

    private Integer hasAuxiliary;
}

