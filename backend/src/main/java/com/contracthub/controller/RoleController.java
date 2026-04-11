package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Role;
import com.contracthub.entity.Permission;
import com.contracthub.entity.RolePermission;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;

    public RoleController(RoleMapper roleMapper, PermissionMapper permissionMapper, 
                          RolePermissionMapper rolePermissionMapper, UserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> getRoles() {
        List<Role> roles = roleMapper.selectList(null);
        Map<String, Object> result = new HashMap<>();
        result.put("list", roles);
        result.put("total", roles.size());
        return ApiResponse.success(result);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<List<Role>> getActiveRoles() {
        List<Role> roles = roleMapper.selectActiveRoles();
        return ApiResponse.success(roles);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> getAllPermissions() {
        List<Permission> permissions = permissionMapper.selectActivePermissions();
        
        // 按模块分组
        Map<String, List<Permission>> groupedPermissions = permissions.stream()
            .collect(Collectors.groupingBy(p -> {
                String code = p.getCode();
                if (code == null) return "其他";
                if (code.startsWith("contract")) return "合同管理";
                if (code.startsWith("template")) return "模板管理";
                if (code.startsWith("user")) return "用户管理";
                if (code.startsWith("role")) return "角色管理";
                if (code.startsWith("approval")) return "审批管理";
                if (code.startsWith("statistics")) return "统计分析";
                return "其他";
            }));
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", permissions);
        result.put("grouped", groupedPermissions);
        result.put("total", permissions.size());
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> getRoleDetail(@PathVariable Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }
        
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(id);
        List<Permission> permissions = new ArrayList<>();
        if (!permissionIds.isEmpty()) {
            permissions = permissionMapper.selectBatchIds(permissionIds);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("permissions", permissions);
        return ApiResponse.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> roleData) {
        String name = (String) roleData.get("name");
        String code = (String) roleData.get("code");
        
        // 必填校验
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("角色名称不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return ApiResponse.error("角色编码不能为空");
        }
        
        // 编码重复校验
        Role existingByCode = roleMapper.selectByCode(code.trim());
        if (existingByCode != null) {
            return ApiResponse.error("角色编码已存在");
        }
        
        Role role = new Role();
        role.setName(name.trim());
        role.setCode(code.trim().toUpperCase());
        role.setDescription((String) roleData.getOrDefault("description", ""));
        role.setStatus(1);
        
        roleMapper.insert(role);
        
        // 处理权限分配
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) roleData.getOrDefault("permissionIds", new ArrayList<>());
        if (!permissionIds.isEmpty()) {
            assignPermissions(role.getId(), permissionIds);
        }
        
        // 返回完整信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", role.getId());
        result.put("name", role.getName());
        result.put("code", role.getCode());
        result.put("description", role.getDescription());
        result.put("status", role.getStatus());
        result.put("permissionIds", permissionIds);
        
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> roleData) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }
        
        String name = (String) roleData.get("name");
        String code = (String) roleData.get("code");
        
        // 必填校验
        if (name != null && name.trim().isEmpty()) {
            return ApiResponse.error("角色名称不能为空");
        }
        if (code != null && code.trim().isEmpty()) {
            return ApiResponse.error("角色编码不能为空");
        }
        
        // 编码重复校验（排除自身）
        if (code != null && !code.trim().equalsIgnoreCase(role.getCode())) {
            Role existingByCode = roleMapper.selectByCode(code.trim());
            if (existingByCode != null && !existingByCode.getId().equals(id)) {
                return ApiResponse.error("角色编码已存在");
            }
        }
        
        if (name != null) {
            role.setName(name.trim());
        }
        if (code != null) {
            role.setCode(code.trim().toUpperCase());
        }
        if (roleData.containsKey("description")) {
            role.setDescription((String) roleData.get("description"));
        }
        if (roleData.containsKey("status")) {
            role.setStatus((Integer) roleData.get("status"));
        }
        
        roleMapper.updateById(role);
        
        // 处理权限分配
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) roleData.getOrDefault("permissionIds", new ArrayList<>());
        assignPermissions(id, permissionIds);
        
        // 获取更新后的权限
        List<Permission> permissions = new ArrayList<>();
        if (!permissionIds.isEmpty()) {
            permissions = permissionMapper.selectBatchIds(permissionIds);
        }
        
        // 返回完整信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", role.getId());
        result.put("name", role.getName());
        result.put("code", role.getCode());
        result.put("description", role.getDescription());
        result.put("status", role.getStatus());
        result.put("permissionIds", permissionIds);
        result.put("permissions", permissions);
        
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }
        
        // 检查是否有用户使用此角色
        if (userMapper.countByRoleId(id) > 0) {
            return ApiResponse.error("该角色下存在用户，无法删除");
        }
        
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);
        
        // 删除角色
        roleMapper.deleteById(id);
        
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> toggleRole(@PathVariable Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }
        
        if ("ADMIN".equals(role.getCode())) {
            return ApiResponse.error("系统管理员角色无法禁用");
        }
        
        role.setStatus(role.getStatus() == 1 ? 0 : 1);
        roleMapper.updateById(role);
        
        return ApiResponse.success(null);
    }

    private void assignPermissions(Long roleId, List<?> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        
        for (Object pid : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            if (pid instanceof Number) {
                rolePermission.setPermissionId(((Number) pid).longValue());
            }
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
