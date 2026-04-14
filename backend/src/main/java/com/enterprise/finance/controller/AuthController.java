package com.enterprise.finance.controller;

import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.dto.LoginRequest;
import com.enterprise.finance.dto.RegisterRequest;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.AuthService;
import com.enterprise.finance.vo.LoginResponse;
import com.enterprise.finance.vo.RegisterResponse;
import com.enterprise.finance.vo.UserInfoResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Validated @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.ok("Logged out");
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> info() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof JwtPrincipal jwtPrincipal)) {
            return ApiResponse.error(401, "Unauthenticated");
        }
        return ApiResponse.ok(authService.getCurrentUserInfo(jwtPrincipal.getUserId()));
    }
}
