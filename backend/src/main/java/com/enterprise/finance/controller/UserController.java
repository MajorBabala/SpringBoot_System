package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.dto.UserCreateRequest;
import com.enterprise.finance.dto.UserResetPasswordRequest;
import com.enterprise.finance.dto.UserUpdateRequest;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.UserService;
import com.enterprise.finance.vo.UserDetailResponse;
import com.enterprise.finance.vo.UserListItemResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<IPage<UserListItemResponse>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId
    ) {
        return ApiResponse.ok(userService.pageUsers(page, limit, username, status, deptId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UserDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Long> create(@Validated @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal && id.equals(jwtPrincipal.getUserId())) {
            return ApiResponse.error(400, "You cannot delete your current login account");
        }
        userService.deleteUser(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/resetPwd")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Validated @RequestBody UserResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.ok(null);
    }
}
