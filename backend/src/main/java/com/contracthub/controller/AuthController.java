package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.User;
import com.contracthub.entity.Role;
import com.contracthub.entity.Permission;
import com.contracthub.entity.UserSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.mapper.UserSessionMapper;
import com.contracthub.service.NotificationService;
import com.contracthub.service.SessionConfigService;
import com.contracthub.util.JwtUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserSessionMapper userSessionMapper;
    private final NotificationService notificationService;
    private final SessionConfigService sessionConfigService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, FailedLoginState> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MILLIS = 10 * 60 * 1000L;
    
    public AuthController(UserMapper userMapper, RoleMapper roleMapper, 
                          PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper,
                          UserSessionMapper userSessionMapper,
                          NotificationService notificationService,
                          SessionConfigService sessionConfigService,
                          JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userSessionMapper = userSessionMapper;
        this.notificationService = notificationService;
        this.sessionConfigService = sessionConfigService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String username = req.get("username");
        String password = req.get("password");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ApiResponse.error("用户名和密码不能为空");
        }

        String ip = getClientIp(request);
        String loginAttemptKey = username + "@" + ip;
        FailedLoginState failedState = failedLoginAttempts.get(loginAttemptKey);
        if (failedState != null && failedState.lockedUntil > System.currentTimeMillis()) {
            long remainingSeconds = Math.max(1, (failedState.lockedUntil - System.currentTimeMillis()) / 1000);
            return ApiResponse.error("登录失败次数过多，请在 " + remainingSeconds + " 秒后重试");
        }
        
        // 查找用户（避免全表扫描）
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            recordFailedLogin(loginAttemptKey);
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
            recordFailedLogin(loginAttemptKey);
            return ApiResponse.error("用户名或密码错误");
        }
        
        if (user.getStatus() == null || user.getStatus() != 1) {
            return ApiResponse.error("用户已被禁用");
        }
        failedLoginAttempts.remove(loginAttemptKey);
        
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
        
        // 创建会话记录
        try {
            if (sessionConfigService.isSingleSession(user.getId())) {
                LambdaQueryWrapper<UserSession> clearWrapper = new LambdaQueryWrapper<>();
                clearWrapper.eq(UserSession::getUserId, user.getId());
                userSessionMapper.delete(clearWrapper);
            }

            UserSession session = new UserSession();
            session.setUserId(user.getId());
            session.setUsername(user.getUsername());
            session.setToken(token);
            session.setIpAddress(ip);
            String userAgent = request.getHeader("User-Agent");
            session.setUserAgent(userAgent);
            session.setDevice(userAgent);
            session.setLocation(getLocationFromIp(ip));
            session.setLoginTime(java.time.LocalDateTime.now());
            session.setLastActiveTime(java.time.LocalDateTime.now());
            int inserted = userSessionMapper.insert(session);
            if (inserted <= 0) {
                return ApiResponse.error("创建会话失败，请稍后重试");
            }

            if (isAnomalousLogin(user.getId(), ip, userAgent, token)) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("ipAddress", ip);
                alertData.put("device", session.getDevice());
                alertData.put("location", session.getLocation());
                notificationService.sendToUser(
                        user.getId(),
                        "security_alert",
                        "检测到新的登录设备",
                        "检测到新设备或IP登录，请确认是否为本人操作",
                        alertData
                );
            }
        } catch (Exception e) {
            return ApiResponse.error("创建会话失败，请稍后重试");
        }
        
        return ApiResponse.success(data);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private String getLocationFromIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知";
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
            return "本地";
        }
        return "未知";
    }

    private void recordFailedLogin(String key) {
        FailedLoginState state = failedLoginAttempts.getOrDefault(key, new FailedLoginState());
        state.count += 1;
        if (state.count >= MAX_LOGIN_ATTEMPTS) {
            state.lockedUntil = System.currentTimeMillis() + LOCK_DURATION_MILLIS;
            state.count = 0;
        }
        failedLoginAttempts.put(key, state);
    }

    private boolean isAnomalousLogin(Long userId, String ip, String userAgent, String currentToken) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.ne(UserSession::getToken, currentToken);
        List<UserSession> sessions = userSessionMapper.selectList(wrapper);
        boolean seenIp = sessions.stream()
                .anyMatch(s -> ip != null && ip.equals(s.getIpAddress()));
        boolean seenDevice = sessions.stream()
                .anyMatch(s -> userAgent != null && userAgent.equals(s.getUserAgent()));
        return !seenIp || !seenDevice;
    }

    private static class FailedLoginState {
        private int count = 0;
        private long lockedUntil = 0L;
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
        
        if (userMapper.selectByUsername(username) != null) {
            return ApiResponse.error("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email != null ? email : "");
        user.setRole("USER");
        user.setStatus(1);
        
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            return ApiResponse.error("用户名已存在");
        }
        
        return ApiResponse.success("注册成功");
    }
    
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("无效的Token");
        }
        
        String token = authHeader.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.error("无效的Token");
        }

        LambdaQueryWrapper<UserSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(UserSession::getToken, token);
        UserSession session = userSessionMapper.selectOne(sessionWrapper);
        if (session == null) {
            return ApiResponse.error("会话不存在或已失效，请重新登录");
        }
        if (!userId.equals(session.getUserId())) {
            return ApiResponse.error("会话校验失败，请重新登录");
        }

        if (!jwtUtils.canRefreshToken(token)) {
            return ApiResponse.error("刷新令牌已过期，请重新登录");
        }

        String newToken = jwtUtils.refreshToken(token);
        
        if (newToken == null) {
            return ApiResponse.error("Token刷新失败");
        }

        session.setToken(newToken);
        session.setLastActiveTime(java.time.LocalDateTime.now());
        userSessionMapper.updateById(session);
        
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
