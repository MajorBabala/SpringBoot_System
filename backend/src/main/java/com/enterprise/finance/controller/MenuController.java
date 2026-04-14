package com.enterprise.finance.controller;

import com.enterprise.finance.common.ApiResponse;
import com.enterprise.finance.dto.MenuSaveRequest;
import com.enterprise.finance.entity.Menu;
import com.enterprise.finance.service.MenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<Menu>> list() {
        return ApiResponse.ok(menuService.listMenus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Menu> detail(@PathVariable Long id) {
        return ApiResponse.ok(menuService.getMenu(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Long> create(@Validated @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok(menuService.createMenu(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody MenuSaveRequest request) {
        menuService.updateMenu(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.ok(null);
    }
}
