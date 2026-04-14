package com.enterprise.finance.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserListItemResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String mobile;
    private Long deptId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
    private List<String> roleCodes;
}
