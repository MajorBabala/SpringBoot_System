package com.enterprise.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.finance.common.BusinessException;
import com.enterprise.finance.dto.LoginRequest;
import com.enterprise.finance.dto.RegisterRequest;
import com.enterprise.finance.entity.Role;
import com.enterprise.finance.entity.User;
import com.enterprise.finance.entity.UserRole;
import com.enterprise.finance.mapper.RoleMapper;
import com.enterprise.finance.mapper.UserMapper;
import com.enterprise.finance.mapper.UserRoleMapper;
import com.enterprise.finance.security.JwtTokenService;
import com.enterprise.finance.vo.LoginResponse;
import com.enterprise.finance.vo.RegisterResponse;
import com.enterprise.finance.vo.UserInfoResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String DEFAULT_REGISTER_ROLE = "EMPLOYEE";

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserMapper userMapper,
                       UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (user == null || !Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(401, "Invalid username or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        List<String> roleCodes = loadRoleCodes(user.getId());
        String token = jwtTokenService.generateToken(user.getId(), user.getUsername(), roleCodes);

        return new LoginResponse(token, user.getId(), user.getUsername(), roleCodes);
    }

    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request) {
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException(400, "The two passwords do not match");
        }

        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();
        String email = normalizeBlank(request.getEmail());
        String mobile = normalizeBlank(request.getMobile());

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

        Role defaultRole = roleMapper.selectOne(new QueryWrapper<Role>()
                .eq("role_code", DEFAULT_REGISTER_ROLE)
                .eq("status", 1)
                .last("limit 1"));
        if (defaultRole == null) {
            throw new BusinessException(500, "Default register role EMPLOYEE is missing");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setDeptId(request.getDeptId());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRoleMapper.insert(userRole);

        return new RegisterResponse(user.getId(), user.getUsername(), defaultRole.getRoleCode());
    }

    public UserInfoResponse getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(404, "User does not exist");
        }

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getDeptId(),
                loadRoleCodes(userId)
        );
    }

    private List<String> loadRoleCodes(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", userId));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).filter(Objects::nonNull).toList();
        List<Role> roles = roleIds.isEmpty() ? Collections.emptyList() : roleMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
