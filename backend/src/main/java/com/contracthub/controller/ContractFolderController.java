package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractFolder;
import com.contracthub.mapper.ContractFolderMapper;
import com.contracthub.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/contract-folders")
public class ContractFolderController {
    
    private final ContractFolderMapper folderMapper;
    
    public ContractFolderController(ContractFolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }
    
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> getFolderTree() {
        List<ContractFolder> folders = folderMapper.selectList(
            new QueryWrapper<ContractFolder>().orderByAsc("sort_order", "id")
        );
        
        List<Map<String, Object>> tree = buildTree(folders, null);
        return ApiResponse.success(tree);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<ContractFolder>> getAllFolders() {
        List<ContractFolder> folders = folderMapper.selectList(
            new QueryWrapper<ContractFolder>().orderByAsc("sort_order", "id")
        );
        return ApiResponse.success(folders);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<ContractFolder> getFolder(@PathVariable Long id) {
        ContractFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            return ApiResponse.error("文件夹不存在", "error.folder.notFound");
        }
        return ApiResponse.success(folder);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('FOLDER_MANAGE')")
    public ApiResponse<ContractFolder> createFolder(@RequestBody Map<String, Object> data) {
        ContractFolder folder = new ContractFolder();
        folder.setName((String) data.get("name"));
        folder.setParentId(data.get("parentId") != null ? ((Number) data.get("parentId")).longValue() : null);
        folder.setDescription((String) data.get("description"));
        folder.setColor((String) data.getOrDefault("color", "#667eea"));
        folder.setSortOrder(data.get("sortOrder") != null ? ((Number) data.get("sortOrder")).intValue() : 0);
        folder.setCreatedBy(SecurityUtils.getCurrentUserId());
        folder.setCreatedByName(SecurityUtils.getCurrentUserName());
        
        folderMapper.insert(folder);
        return ApiResponse.success(folder);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FOLDER_MANAGE')")
    public ApiResponse<ContractFolder> updateFolder(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        ContractFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            return ApiResponse.error("文件夹不存在", "error.folder.notFound");
        }
        
        if (data.containsKey("name")) {
            folder.setName((String) data.get("name"));
        }
        if (data.containsKey("parentId")) {
            Object parentIdObj = data.get("parentId");
            if (parentIdObj == null) {
                folder.setParentId(null);
            } else if (parentIdObj instanceof Number) {
                folder.setParentId(((Number) parentIdObj).longValue());
            }
        }
        if (data.containsKey("description")) {
            folder.setDescription((String) data.get("description"));
        }
        if (data.containsKey("color")) {
            folder.setColor((String) data.get("color"));
        }
        if (data.containsKey("sortOrder")) {
            folder.setSortOrder(((Number) data.get("sortOrder")).intValue());
        }
        
        folderMapper.updateById(folder);
        return ApiResponse.success(folder);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FOLDER_MANAGE')")
    public ApiResponse<Void> deleteFolder(@PathVariable Long id) {
        ContractFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            return ApiResponse.error("文件夹不存在", "error.folder.notFound");
        }
        
        // 检查是否有子文件夹
        long childCount = folderMapper.selectCount(
            new QueryWrapper<ContractFolder>().eq("parent_id", id)
        );
        if (childCount > 0) {
            return ApiResponse.error("请先删除子文件夹", "error.folder.hasChildren");
        }
        
        folderMapper.deleteById(id);
        return ApiResponse.success(null);
    }
    
    private List<Map<String, Object>> buildTree(List<ContractFolder> folders, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (ContractFolder folder : folders) {
            if ((parentId == null && folder.getParentId() == null) ||
                (parentId != null && parentId.equals(folder.getParentId()))) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", folder.getId());
                node.put("name", folder.getName());
                node.put("parentId", folder.getParentId());
                node.put("description", folder.getDescription());
                node.put("color", folder.getColor());
                node.put("sortOrder", folder.getSortOrder());
                node.put("createdBy", folder.getCreatedByName());
                
                List<Map<String, Object>> children = buildTree(folders, folder.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                tree.add(node);
            }
        }
        return tree;
    }
}
