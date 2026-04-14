package com.enterprise.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.dto.UserCreateRequest;
import com.enterprise.finance.dto.UserResetPasswordRequest;
import com.enterprise.finance.dto.UserUpdateRequest;
import com.enterprise.finance.entity.Role;
import com.enterprise.finance.entity.User;
import com.enterprise.finance.entity.UserRole;
import com.enterprise.finance.mapper.RoleMapper;
import com.enterprise.finance.mapper.UserMapper;
import com.enterprise.finance.mapper.UserRoleMapper;
import com.enterprise.finance.service.UserService;
import com.enterprise.finance.vo.UserDetailResponse;
import com.enterprise.finance.vo.UserListItemResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           UserRoleMapper userRoleMapper,
                           RoleMapper roleMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public IPage<UserListItemResponse> pageUsers(long page, long limit, String username, Integer status, Long deptId) {
        Page<User> pageParam = new Page<>(page, limit);

        QueryWrapper<User> query = new QueryWrapper<User>()
                .like(StringUtils.hasText(username), "username", username)
                .eq(status != null, "status", status)
                .eq(deptId != null, "dept_id", deptId)
                .orderByDesc("id");

        IPage<User> userPage = userMapper.selectPage(pageParam, query);
        List<User> users = userPage.getRecords();
        Map<Long, List<String>> roleMap = loadRoleCodesByUserIds(users.stream().map(User::getId).toList());

        List<UserListItemResponse> records = users.stream().map(user -> {
            UserListItemResponse item = new UserListItemResponse();
            item.setId(user.getId());
            item.setUsername(user.getUsername());
            item.setNickname(user.getNickname());
            item.setEmail(user.getEmail());
            item.setMobile(user.getMobile());
            item.setDeptId(user.getDeptId());
            item.setStatus(user.getStatus());
            item.setCreateTime(user.getCreateTime());
            item.setUpdateTime(user.getUpdateTime());
            item.setLastLoginTime(user.getLastLoginTime());
            item.setRoleCodes(roleMap.getOrDefault(user.getId(), Collections.emptyList()));
            return item;
        }).toList();

        Page<UserListItemResponse> result = new Page<>();
        result.setCurrent(userPage.getCurrent());
        result.setSize(userPage.getSize());
        result.setTotal(userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public UserDetailResponse getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User does not exist");
        }

        List<UserRole> userRoles = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", id));
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> roleCodeMap = roleIds.isEmpty()
                ? Collections.emptyMap()
                : roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Role::getRoleCode, (a, b) -> a));
        List<String> roleCodes = roleIds.stream()
                .map(roleCodeMap::get)
                .filter(Objects::nonNull)
                .toList();

        UserDetailResponse resp = new UserDetailResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setEmail(user.getEmail());
        resp.setMobile(user.getMobile());
        resp.setDeptId(user.getDeptId());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setUpdateTime(user.getUpdateTime());
        resp.setLastLoginTime(user.getLastLoginTime());
        resp.setRoleIds(roleIds);
        resp.setRoleCodes(roleCodes);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();
        String email = normalizeBlank(request.getEmail());
        String mobile = normalizeBlank(request.getMobile());
        List<Long> roleIds = sanitizeRoleIds(request.getRoleIds());

        checkUniqueForCreate(username, email, mobile);
        ensureRolesValid(roleIds);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);

        replaceUserRoles(user.getId(), roleIds);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User does not exist");
        }

        String nickname = request.getNickname().trim();
        String email = normalizeBlank(request.getEmail());
        String mobile = normalizeBlank(request.getMobile());
        List<Long> roleIds = sanitizeRoleIds(request.getRoleIds());

        checkUniqueForUpdate(id, email, mobile);
        if (request.getRoleIds() != null) {
            ensureRolesValid(roleIds);
        }

        user.setNickname(nickname);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setDeptId(request.getDeptId());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);

        if (request.getRoleIds() != null) {
            replaceUserRoles(id, roleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User does not exist");
        }
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", id));
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, UserResetPasswordRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User does not exist");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.updateById(user);
    }

    private void checkUniqueForCreate(String username, String email, String mobile) {
        User existsByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existsByUsername != null) {
            throw new BusinessException(400, "Username already exists");
        }

        if (email != null) {
            User existsByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
            if (existsByEmail != null) {
                throw new BusinessException(400, "Email already exists");
            }
        }

        if (mobile != null) {
            User existsByMobile = userMapper.selectOne(new QueryWrapper<User>().eq("mobile", mobile));
            if (existsByMobile != null) {
                throw new BusinessException(400, "Mobile already exists");
            }
        }
    }

    private void checkUniqueForUpdate(Long userId, String email, String mobile) {
        if (email != null) {
            User existsByEmail = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("email", email)
                    .ne("id", userId)
                    .last("limit 1"));
            if (existsByEmail != null) {
                throw new BusinessException(400, "Email already exists");
            }
        }

        if (mobile != null) {
            User existsByMobile = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("mobile", mobile)
                    .ne("id", userId)
                    .last("limit 1"));
            if (existsByMobile != null) {
                throw new BusinessException(400, "Mobile already exists");
            }
        }
    }

    private void ensureRolesValid(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(400, "Some roles do not exist");
        }
        boolean hasDisabled = roles.stream().anyMatch(role -> !Objects.equals(role.getStatus(), 1));
        if (hasDisabled) {
            throw new BusinessException(400, "Disabled roles cannot be assigned");
        }
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", userId));
        if (roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRole rel = new UserRole();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            userRoleMapper.insert(rel);
        }
    }

    private List<Long> sanitizeRoleIds(List<Long> roleIds) {
        if (roleIds == null) {
            return Collections.emptyList();
        }
        Set<Long> unique = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId != null) {
                unique.add(roleId);
            }
        }
        List<Long> result = new ArrayList<>(unique);
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private Map<Long, List<String>> loadRoleCodesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserRole> relations = userRoleMapper.selectList(new QueryWrapper<UserRole>().in("user_id", userIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> roleIds = relations.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> roleCodeMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Role::getRoleCode, (a, b) -> a));

        Map<Long, List<String>> userRoleCodes = new HashMap<>();
        for (UserRole rel : relations) {
            String roleCode = roleCodeMap.get(rel.getRoleId());
            if (roleCode == null) {
                continue;
            }
            userRoleCodes.computeIfAbsent(rel.getUserId(), k -> new ArrayList<>()).add(roleCode);
        }
        userRoleCodes.values().forEach(list -> list.sort(Comparator.naturalOrder()));
        return userRoleCodes;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
