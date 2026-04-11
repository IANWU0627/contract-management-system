package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.User;
import com.contracthub.entity.Role;
import com.contracthub.entity.Permission;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.util.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    public AuthController(UserMapper userMapper, RoleMapper roleMapper, 
                          PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper,
                          JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ApiResponse.error("用户名和密码不能为空");
        }
        
        // 查找用户
        List<User> users = userMapper.selectList(null);
        User user = users.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }
        
        // 验证密码
        String storedPassword = user.getPassword();
        boolean passwordValid = false;
        
        if (storedPassword.equals(password)) {
            user.setPassword(passwordEncoder.encode(password));
            userMapper.updateById(user);
            passwordValid = true;
        } else if (passwordEncoder.matches(password, storedPassword)) {
            passwordValid = true;
        }
        
        if (!passwordValid) {
            return ApiResponse.error("用户名或密码错误");
        }
        
        if (user.getStatus() == null || user.getStatus() != 1) {
            return ApiResponse.error("用户已被禁用");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("id", user.getId());
        data.put("role", user.getRole());
        data.put("roles", getRoles(user.getRole()));
        data.put("permissions", getPermissions(user.getRole()));
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        
        return ApiResponse.success(data);
    }
    
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        String nickname = req.get("nickname");
        String email = req.get("email");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ApiResponse.error("用户名和密码不能为空");
        }
        
        if (password.length() < 6) {
            return ApiResponse.error("密码至少6位");
        }
        
        List<User> users = userMapper.selectList(null);
        boolean exists = users.stream().anyMatch(u -> username.equals(u.getUsername()));
        if (exists) {
            return ApiResponse.error("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email != null ? email : "");
        user.setRole("USER");
        user.setStatus(1);
        
        userMapper.insert(user);
        
        return ApiResponse.success("注册成功");
    }
    
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("无效的Token");
        }
        
        String token = authHeader.substring(7);
        String newToken = jwtUtils.refreshToken(token);
        
        if (newToken == null) {
            return ApiResponse.error("Token刷新失败");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        return ApiResponse.success(data);
    }
    
    private List<String> getRoles(String role) {
        if ("ADMIN".equals(role)) {
            return Arrays.asList("ADMIN", "LEGAL", "USER");
        } else if ("LEGAL".equals(role)) {
            return Arrays.asList("LEGAL", "USER");
        }
        return Arrays.asList("USER");
    }
    
    private List<String> getPermissions(String role) {
        Role roleEntity = roleMapper.selectByCode(role);
        if (roleEntity == null) {
            return Collections.emptyList();
        }
        
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleEntity.getId());
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }
}
