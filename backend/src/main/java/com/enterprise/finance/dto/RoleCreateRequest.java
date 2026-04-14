package com.enterprise.finance.dto;

import lombok.Data;

/**
 * 角色新增/编辑请求
 */
@Data
public class RoleCreateRequest {
    private String roleName;
    private String roleCode;
    private Integer status;
}
