package com.enterprise.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.entity.Menu;
import com.enterprise.finance.entity.Role;
import com.enterprise.finance.entity.RoleMenu;
import com.enterprise.finance.entity.UserRole;
import com.enterprise.finance.mapper.MenuMapper;
import com.enterprise.finance.mapper.RoleMapper;
import com.enterprise.finance.mapper.RoleMenuMapper;
import com.enterprise.finance.mapper.UserRoleMapper;
import com.enterprise.finance.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    public RoleServiceImpl(RoleMapper roleMapper,
                           UserRoleMapper userRoleMapper,
                           RoleMenuMapper roleMenuMapper,
                           MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public boolean checkRoleCodeUnique(String roleCode, Long excludeId) {
        long count = lambdaQuery()
                .eq(Role::getRoleCode, roleCode)
                .ne(excludeId != null, Role::getId, excludeId)
                .count();
        return count == 0;
    }

    @Override
    public Long addRole(String roleName, String roleCode, Integer status) {
        if (!checkRoleCodeUnique(roleCode, null)) {
            throw BusinessException.of("Role code already exists");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setStatus(status != null ? status : 1);

        save(role);
        return role.getId();
    }

    @Override
    public boolean updateRole(Long id, String roleName, String roleCode, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw BusinessException.of("Role does not exist");
        }

        if (!role.getRoleCode().equals(roleCode) && !checkRoleCodeUnique(roleCode, id)) {
            throw BusinessException.of("Role code already exists");
        }

        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setStatus(status != null ? status : 1);

        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw BusinessException.of("Role does not exist");
        }

        long userCount = userRoleMapper.selectCount(new QueryWrapper<UserRole>().eq("role_id", id));
        if (userCount > 0) {
            throw BusinessException.of("Role is already assigned to users");
        }

        roleMenuMapper.delete(new QueryWrapper<RoleMenu>().eq("role_id", id));
        return removeById(id);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        ensureRoleExists(roleId);
        return roleMenuMapper.selectList(new QueryWrapper<RoleMenu>().eq("role_id", roleId))
                .stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        ensureRoleExists(roleId);
        List<Long> normalizedIds = normalizeIds(menuIds);
        ensureMenusExist(normalizedIds);

        roleMenuMapper.delete(new QueryWrapper<RoleMenu>().eq("role_id", roleId));
        for (Long menuId : normalizedIds) {
            RoleMenu rel = new RoleMenu();
            rel.setRoleId(roleId);
            rel.setMenuId(menuId);
            roleMenuMapper.insert(rel);
        }
    }

    @Override
    public List<Menu> listCurrentUserMenus(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<UserRole> userRoles = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", userId));
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(new QueryWrapper<RoleMenu>().in("role_id", roleIds));
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        return menuMapper.selectBatchIds(menuIds).stream()
                .sorted(Comparator.comparing(Menu::getParentId, Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(Menu::getSort, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(Menu::getId))
                .collect(Collectors.toList());
    }

    private void ensureRoleExists(Long roleId) {
        Role role = getById(roleId);
        if (role == null) {
            throw new BusinessException(404, "Role does not exist");
        }
    }

    private void ensureMenusExist(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        List<Menu> menus = menuMapper.selectBatchIds(menuIds);
        if (menus.size() != menuIds.size()) {
            throw new BusinessException(400, "Some menus do not exist");
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> unique = new HashSet<>();
        for (Long id : ids) {
            if (id != null) {
                unique.add(id);
            }
        }
        List<Long> list = new ArrayList<>(unique);
        list.sort(Long::compareTo);
        return list;
    }
}
