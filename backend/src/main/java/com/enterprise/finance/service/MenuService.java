package com.enterprise.finance.service;

import com.enterprise.finance.dto.MenuSaveRequest;
import com.enterprise.finance.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> listMenus();

    Menu getMenu(Long id);

    Long createMenu(MenuSaveRequest request);

    void updateMenu(Long id, MenuSaveRequest request);

    void deleteMenu(Long id);
}
