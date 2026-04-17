package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.mapper.PermissionMapper;
import com.contracthub.mapper.RoleMapper;
import com.contracthub.mapper.RolePermissionMapper;
import com.contracthub.entity.Permission;
import com.contracthub.entity.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    public AdminController(PermissionMapper permissionMapper, RoleMapper roleMapper, 
                          RolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }
    
    @PostMapping("/init-permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> initPermissions() {
        int added = 0;
        
        // 定义需要添加的权限
        List<Map<String, String>> permissionsToAdd = Arrays.asList(
            createPerm("合同管理", "CONTRACT_MANAGE", "/api/contracts/*", "GET", "查看合同管理"),
            createPerm("合同审批", "CONTRACT_APPROVE", "/api/approvals", "GET", "审批合同"),
            createPerm("提醒管理", "REMINDER_MANAGE", "/api/reminders", "GET", "提醒管理"),
            createPerm("收藏管理", "FAVORITE_MANAGE", "/api/favorites", "GET", "收藏管理"),
            createPerm("续约管理", "RENEWAL_MANAGE", "/api/renewals", "GET", "续约管理"),
            createPerm("报表查看", "REPORT_VIEW", "/api/statistics", "GET", "查看报表"),
            createPerm("标签管理", "TAG_MANAGE", "/api/tags", "GET", "标签管理"),
            createPerm("提醒规则管理", "REMINDER_RULE_MANAGE", "/api/reminder-rules", "GET", "提醒规则管理"),
            createPerm("角色管理", "ROLE_MANAGE", "/api/roles", "GET", "角色管理"),
            createPerm("用户管理", "USER_MANAGE", "/api/users", "GET", "用户管理"),
            createPerm("设置管理", "SETTING_MANAGE", "/api/settings", "GET", "设置管理"),
            createPerm("类型字段配置", "TYPE_FIELD_CONFIG", "/api/contract-type-fields", "GET", "类型字段配置"),
            createPerm("变更记录", "CHANGE_LOG_VIEW", "/api/change-logs", "GET", "查看变更记录"),
            createPerm("类型管理", "CATEGORY_MANAGE", "/api/template-categories", "GET", "合同类型与分类（与 @PreAuthorize 一致）"),
            createPerm("模板与条款库", "TEMPLATE_MANAGE", "/api/templates", "GET", "合同模板与条款库（/api/clauses 同源权限）"),
            createPerm("快速代码", "QUICK_CODE_MANAGE", "/api/quick-codes", "GET", "快速代码"),
            createPerm("变量管理", "VARIABLE_MANAGE", "/api/template-variables", "GET", "模板变量"),
            createPerm("文件夹管理", "FOLDER_MANAGE", "/api/contract-folders", "GET", "合同文件夹")
        );
        
        // 添加权限
        List<Permission> existingPerms = permissionMapper.selectList(null);
        Set<String> existingCodes = new HashSet<>();
        for (Permission p : existingPerms) {
            existingCodes.add(p.getCode());
        }
        
        for (Map<String, String> perm : permissionsToAdd) {
            if (!existingCodes.contains(perm.get("code"))) {
                Permission p = new Permission();
                p.setName(perm.get("name"));
                p.setCode(perm.get("code"));
                p.setPath(perm.get("path"));
                p.setMethod(perm.get("method"));
                p.setDescription(perm.get("description"));
                p.setStatus(1);
                permissionMapper.insert(p);
                added++;
            }
        }
        
        // 为管理员角色添加所有新权限
        Role adminRole = roleMapper.selectList(null).stream()
            .filter(r -> "ADMIN".equals(r.getCode()))
            .findFirst().orElse(null);
        
        if (adminRole != null) {
            List<Permission> allPerms = permissionMapper.selectList(null);
            for (Permission p : allPerms) {
                // 检查是否已有关联
                List<Role> roles = rolePermissionMapper.selectRolesByPermissionId(p.getId());
                boolean hasAdmin = roles.stream().anyMatch(r -> "ADMIN".equals(r.getCode()));
                if (!hasAdmin) {
                    rolePermissionMapper.insertRolePermission(adminRole.getId(), p.getId());
                }
            }
        }
        
        // 为法务角色添加相关权限
        Role legalRole = roleMapper.selectList(null).stream()
            .filter(r -> "LEGAL".equals(r.getCode()))
            .findFirst().orElse(null);
        
        if (legalRole != null) {
            List<String> legalPerms = Arrays.asList(
                "CONTRACT_MANAGE", "CONTRACT_APPROVE", "REMINDER_MANAGE", "FAVORITE_MANAGE",
                "RENEWAL_MANAGE", "REPORT_VIEW", "TAG_MANAGE", "REMINDER_RULE_MANAGE", "CHANGE_LOG_VIEW",
                "CATEGORY_MANAGE", "TEMPLATE_MANAGE", "QUICK_CODE_MANAGE",
                "VARIABLE_MANAGE", "FOLDER_MANAGE"
            );
            
            List<Permission> allPerms = permissionMapper.selectList(null);
            for (Permission p : allPerms) {
                if (legalPerms.contains(p.getCode())) {
                    List<Role> roles = rolePermissionMapper.selectRolesByPermissionId(p.getId());
                    boolean hasLegal = roles.stream().anyMatch(r -> "LEGAL".equals(r.getCode()));
                    if (!hasLegal) {
                        rolePermissionMapper.insertRolePermission(legalRole.getId(), p.getId());
                    }
                }
            }
        }
        
        return ApiResponse.success("已添加 " + added + " 个权限并更新角色关联");
    }
    
    private Map<String, String> createPerm(String name, String code, String path, String method, String desc) {
        Map<String, String> m = new HashMap<>();
        m.put("name", name);
        m.put("code", code);
        m.put("path", path);
        m.put("method", method);
        m.put("description", desc);
        return m;
    }
}
