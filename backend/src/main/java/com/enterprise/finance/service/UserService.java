package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.finance.dto.UserCreateRequest;
import com.enterprise.finance.dto.UserResetPasswordRequest;
import com.enterprise.finance.dto.UserUpdateRequest;
import com.enterprise.finance.entity.User;
import com.enterprise.finance.vo.UserDetailResponse;
import com.enterprise.finance.vo.UserListItemResponse;

public interface UserService extends IService<User> {
    IPage<UserListItemResponse> pageUsers(long page, long limit, String username, Integer status, Long deptId);

    UserDetailResponse getUserDetail(Long id);

    Long createUser(UserCreateRequest request);

    void updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void resetPassword(Long id, UserResetPasswordRequest request);
}
