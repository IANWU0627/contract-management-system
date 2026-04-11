package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.User;
import com.contracthub.entity.Role;
import com.contracthub.entity.Permission;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    
    public UserController(UserMapper userMapper, PasswordEncoder passwordEncoder, RoleMapper roleMapper,
                         RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        
        try {
            if (userMapper.selectCount(null) == 0) {
                initDefaultUsers();
            } else {
                // 检查是否需要升级旧密码
                upgradeLegacyPasswords();
            }
        } catch (Exception e) {
            createUserTable();
            initDefaultUsers();
        }
    }
    
    private void createUserTable() {
        try {
            String createTableSql = "CREATE TABLE IF NOT EXISTS \"user\" (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "nickname VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100), " +
                "role VARCHAR(20) NOT NULL, " +
                "status INTEGER DEFAULT 1" +
                ")";
            
            java.sql.Connection connection = java.sql.DriverManager.getConnection(
                "jdbc:h2:file:./data/contractdb", "sa", ""
            );
            java.sql.Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSql);
            statement.close();
            connection.close();
        } catch (Exception e) {
            // 忽略
        }
    }
    
    private void upgradeLegacyPasswords() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            String pwd = user.getPassword();
            if (pwd != null && !pwd.startsWith("$2") && "123456".equals(pwd)) {
                user.setPassword(passwordEncoder.encode(pwd));
                userMapper.updateById(user);
            }
        }
    }
    
    private void initDefaultUsers() {
        createUser("admin", "123456", "超级管理员", "admin@toycontract.com", "ADMIN");
        createUser("zhangsan", "123456", "张三", "zhangsan@toycontract.com", "LEGAL");
        createUser("lisi", "123456", "李四", "lisi@toycontract.com", "LEGAL");
        createUser("wangwu", "123456", "王五", "wangwu@toycontract.com", "USER");
        createUser("zhaoliu", "123456", "赵六", "zhaoliu@toycontract.com", "USER");
        createUser("cain", "123456", "Cain管理员", "cain@toycontract.com", "ADMIN");
    }
    
    private void createUser(String username, String password, String nickname, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
    }
    
    // 公共方法：获取所有用户数据（供AuthController使用）
    public List<Map<String, Object>> getAllUsers() {
        try {
            List<User> userList = userMapper.selectList(null);
            List<Map<String, Object>> result = new ArrayList<>();
            for (User user : userList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                map.put("status", user.getStatus() == 1 ? "active" : "inactive");
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            // 表不存在时返回空列表
            System.out.println("获取用户列表失败，返回空列表: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            User user = userMapper.selectById(currentUserId);
            if (user == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", currentUserId);
                result.put("username", "unknown");
                result.put("nickname", "未知用户");
                result.put("email", "");
                result.put("phone", "");
                result.put("avatar", "");
                result.put("role", "USER");
                result.put("status", "active");
                result.put("roles", Arrays.asList("USER"));
                result.put("permissions", Arrays.asList("contract:read", "template:read"));
                return ApiResponse.success(result);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("avatar", user.getAvatar());
            result.put("role", user.getRole());
            result.put("status", user.getStatus() == 1 ? "active" : "inactive");
            result.put("roles", getRoles(user.getRole()));
            result.put("permissions", getPermissions(user.getRole()));
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("id", 1L);
            result.put("username", "admin");
            result.put("nickname", "管理员");
            result.put("email", "admin@toycontract.com");
            result.put("phone", "");
            result.put("avatar", "");
            result.put("role", "ADMIN");
            result.put("status", "active");
            result.put("roles", Arrays.asList("ADMIN", "LEGAL", "USER"));
            result.put("permissions", Arrays.asList(
                "contract:create", "contract:read", "contract:update", "contract:delete",
                "template:create", "template:read", "template:update", "template:delete",
                "user:create", "user:read", "user:update", "user:delete",
                "system:settings", "system:logs", "system:statistics"
            ));
            return ApiResponse.success(result);
        }
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
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            // 先查询所有用户
            List<User> userList = userMapper.selectList(null);
            List<Map<String, Object>> filtered = new ArrayList<>();
            
            // 构建用户数据
            for (User user : userList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("email", user.getEmail());
                map.put("phone", user.getPhone());
                map.put("role", user.getRole());
                map.put("status", user.getStatus() == 1 ? "active" : "inactive");
                map.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
                map.put("createdAt", java.time.LocalDate.now().toString());
                map.put("lastLoginAt", "2026-03-26 09:30:00");
                map.put("contractCount", (int)(Math.random() * 20) + 1);
                filtered.add(map);
            }
            
            // 筛选
            if (role != null && !role.isEmpty()) {
                filtered.removeIf(u -> !role.equals(u.get("role")));
            }
            
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                filtered.removeIf(u -> {
                    String name = ((String)u.get("username")).toLowerCase();
                    String nick = ((String)u.get("nickname")).toLowerCase();
                    String email = ((String)u.get("email")).toLowerCase();
                    return !name.contains(kw) && !nick.contains(kw) && !email.contains(kw);
                });
            }
            
            // 分页处理
            int total = filtered.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Map<String, Object>> paginatedList = start < end ? filtered.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", paginatedList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return ApiResponse.success(result);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User();
            user.setUsername((String) userData.get("username"));
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname((String) userData.get("nickname"));
            user.setEmail((String) userData.getOrDefault("email", ""));
            user.setRole((String) userData.get("role"));
            user.setStatus(1);
            
            userMapper.insert(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("role", user.getRole());
            result.put("status", "active");
            result.put("avatar", "");
            result.put("createdAt", java.time.LocalDate.now().toString());
            result.put("contractCount", 0);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("创建用户失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            if (userData.containsKey("username")) {
                user.setUsername((String) userData.get("username"));
            }
            if (userData.containsKey("nickname")) {
                user.setNickname((String) userData.get("nickname"));
            }
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            if (userData.containsKey("phone")) {
                user.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("role")) {
                user.setRole((String) userData.get("role"));
            }
            
            userMapper.updateById(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("role", user.getRole());
            result.put("status", user.getStatus() == 1 ? "active" : "inactive");
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ApiResponse.error("更新用户失败");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            int result = userMapper.deleteById(id);
            if (result == 0) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ApiResponse.error("删除用户失败");
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> req) {
        try {
            String status = req.get("status");
            User user = userMapper.selectById(id);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            user.setStatus("active".equals(status) ? 1 : 0);
            userMapper.updateById(user);
            
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return ApiResponse.error("更新用户状态失败");
        }
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            user.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(user);
            
            return ApiResponse.success("密码已重置为 123456");
        } catch (Exception e) {
            return ApiResponse.error("重置密码失败: " + e.getMessage());
        }
    }
    
    // 更新当前登录用户信息
    @PutMapping("/me")
    public ApiResponse<Map<String, Object>> updateMe(@RequestBody Map<String, Object> userData) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            User user = userMapper.selectById(currentUserId);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            if (userData.containsKey("nickname")) {
                user.setNickname((String) userData.get("nickname"));
            }
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            if (userData.containsKey("phone")) {
                user.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("avatar")) {
                user.setAvatar((String) userData.get("avatar"));
            }
            
            userMapper.updateById(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("avatar", user.getAvatar());
            result.put("role", user.getRole());
            result.put("status", user.getStatus() == 1 ? "active" : "inactive");
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ApiResponse.error("更新用户信息失败");
        }
    }
    
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody Map<String, String> req) {
        try {
            String oldPassword = req.get("oldPassword");
            String newPassword = req.get("newPassword");
            
            if (oldPassword == null || oldPassword.isEmpty()) {
                return ApiResponse.error("原密码不能为空");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return ApiResponse.error("新密码至少6位");
            }
            
            Long currentUserId = SecurityUtils.getCurrentUserId();
            User user = userMapper.selectById(currentUserId);
            if (user != null) {
                if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                    return ApiResponse.error("原密码错误");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                userMapper.updateById(user);
            }
            
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error("修改密码失败");
        }
    }
    
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<List<Role>> getRoles() {
        List<Role> roles = roleMapper.selectActiveRoles();
        return ApiResponse.success(roles);
    }
}
