package com.enterprise.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.finance.entity.Menu;
import com.enterprise.finance.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    boolean checkRoleCodeUnique(String roleCode, Long excludeId);

    Long addRole(String roleName, String roleCode, Integer status);

    boolean updateRole(Long id, String roleName, String roleCode, Integer status);

    boolean deleteRole(Long id);

    List<Long> getRoleMenuIds(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Menu> listCurrentUserMenus(Long userId);
}
