package com.enterprise.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.dto.RoleCreateRequest;
import com.enterprise.finance.dto.RoleMenuAssignRequest;
import com.enterprise.finance.entity.Menu;
import com.enterprise.finance.entity.Role;
import com.enterprise.finance.security.JwtPrincipal;
import com.enterprise.finance.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<IPage<Role>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {

        Page<Role> pageParam = new Page<>(page, limit);
        IPage<Role> result = roleService.lambdaQuery()
                .like(roleName != null && !roleName.trim().isEmpty(), Role::getRoleName, roleName)
                .eq(status != null, Role::getStatus, status)
                .orderByDesc(Role::getId)
                .page(pageParam);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Role> get(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return ApiResponse.error(404, "Role does not exist");
        }
        return ApiResponse.ok(role);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Long> create(@RequestBody RoleCreateRequest request) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            return ApiResponse.error(400, "Role name is required");
        }
        if (request.getRoleCode() == null || request.getRoleCode().trim().isEmpty()) {
            return ApiResponse.error(400, "Role code is required");
        }
        Long roleId = roleService.addRole(request.getRoleName().trim(), request.getRoleCode().trim(), request.getStatus());
        return ApiResponse.ok(roleId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody RoleCreateRequest request) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            return ApiResponse.error(400, "Role name is required");
        }
        if (request.getRoleCode() == null || request.getRoleCode().trim().isEmpty()) {
            return ApiResponse.error(400, "Role code is required");
        }
        roleService.updateRole(id, request.getRoleName().trim(), request.getRoleCode().trim(), request.getStatus());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<Long>> getRoleMenus(@PathVariable Long id) {
        return ApiResponse.ok(roleService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> assignRoleMenus(@PathVariable Long id, @RequestBody RoleMenuAssignRequest request) {
        roleService.assignMenus(id, request == null ? null : request.getMenuIds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/current/menus")
    public ApiResponse<List<Menu>> currentMenus() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof JwtPrincipal jwtPrincipal)) {
            return ApiResponse.error(401, "Unauthenticated");
        }
        return ApiResponse.ok(roleService.listCurrentUserMenus(jwtPrincipal.getUserId()));
    }

    @GetMapping("/check-code/{roleCode}")
    public ApiResponse<Boolean> checkRoleCode(@PathVariable String roleCode) {
        boolean unique = roleService.checkRoleCodeUnique(roleCode, null);
        return ApiResponse.ok(unique);
    }
}
