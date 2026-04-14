package com.enterprise.finance.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * JWT 认证主体：只保留最小关键信息，便于后续业务取当前用户ID。
 */
@Data
@AllArgsConstructor
public class JwtPrincipal {
    private Long userId;
    private String username;
}

