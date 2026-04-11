package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Permission;
import com.contracthub.mapper.PermissionMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Map<String, Object>> getPermissions() {
        List<Permission> permissions = permissionMapper.selectList(null);
        Map<String, Object> result = new HashMap<>();
        result.put("list", permissions);
        result.put("total", permissions.size());
        return ApiResponse.success(result);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<List<Permission>> getActivePermissions() {
        List<Permission> permissions = permissionMapper.selectActivePermissions();
        return ApiResponse.success(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Permission> getPermissionDetail(@PathVariable Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return ApiResponse.error("权限不存在");
        }
        return ApiResponse.success(permission);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Permission> createPermission(@RequestBody Permission permission) {
        permissionMapper.insert(permission);
        return ApiResponse.success(permission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        permissionMapper.updateById(permission);
        return ApiResponse.success(permission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        permissionMapper.deleteById(id);
        return ApiResponse.success(null);
    }
}
