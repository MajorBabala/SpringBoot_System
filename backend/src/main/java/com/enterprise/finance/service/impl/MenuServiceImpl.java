package com.enterprise.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.dto.MenuSaveRequest;
import com.enterprise.finance.entity.Menu;
import com.enterprise.finance.mapper.MenuMapper;
import com.enterprise.finance.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<Menu> listMenus() {
        return menuMapper.selectList(new QueryWrapper<Menu>()
                .orderByAsc("parent_id")
                .orderByAsc("sort")
                .orderByAsc("id"));
    }

    @Override
    public Menu getMenu(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "Menu does not exist");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuSaveRequest request) {
        Long parentId = normalizeParentId(request.getParentId());
        ensureParentExists(parentId);

        Menu menu = new Menu();
        menu.setParentId(parentId);
        menu.setMenuName(request.getMenuName().trim());
        menu.setPath(normalizeBlank(request.getPath()));
        menu.setPermission(normalizeBlank(request.getPermission()));
        menu.setIcon(normalizeBlank(request.getIcon()));
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setType(request.getType());
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Long id, MenuSaveRequest request) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "Menu does not exist");
        }

        Long parentId = normalizeParentId(request.getParentId());
        if (id.equals(parentId)) {
            throw new BusinessException(400, "Menu parent cannot be itself");
        }
        ensureParentExists(parentId);

        existing.setParentId(parentId);
        existing.setMenuName(request.getMenuName().trim());
        existing.setPath(normalizeBlank(request.getPath()));
        existing.setPermission(normalizeBlank(request.getPermission()));
        existing.setIcon(normalizeBlank(request.getIcon()));
        existing.setSort(request.getSort() == null ? 0 : request.getSort());
        existing.setType(request.getType());
        menuMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "Menu does not exist");
        }

        Long childCount = menuMapper.selectCount(new QueryWrapper<Menu>().eq("parent_id", id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(400, "Please delete child menus first");
        }

        menuMapper.deleteById(id);
    }

    private void ensureParentExists(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        Menu parent = menuMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(400, "Parent menu does not exist");
        }
    }

    private Long normalizeParentId(Long parentId) {
        if (parentId == null || parentId < 0) {
            return 0L;
        }
        return parentId;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
