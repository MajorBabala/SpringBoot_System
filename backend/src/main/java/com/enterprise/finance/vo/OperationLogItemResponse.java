package com.enterprise.finance.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogItemResponse {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Integer duration;
    private LocalDateTime createTime;
}
