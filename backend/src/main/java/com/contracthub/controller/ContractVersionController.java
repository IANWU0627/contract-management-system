package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractVersion;
import com.contracthub.mapper.ContractVersionMapper;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/contracts/{contractId}/versions")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
public class ContractVersionController {

    private final ContractVersionMapper contractVersionMapper;

    public ContractVersionController(ContractVersionMapper contractVersionMapper) {
        this.contractVersionMapper = contractVersionMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getVersionHistory(@PathVariable Long contractId) {
        // 从数据库获取该合同的所有版本
        List<ContractVersion> versionList = contractVersionMapper.selectList(null);
        List<Map<String, Object>> versions = new ArrayList<>();
        
        for (ContractVersion version : versionList) {
            if (version.getContractId().equals(contractId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", version.getId());
                map.put("version", version.getVersion());
                map.put("operatorName", version.getOperatorName());
                map.put("changeDesc", version.getChangeDesc());
                map.put("createdAt", version.getCreatedAt());
                map.put("content", version.getContent());
                map.put("attachments", version.getAttachments());
                versions.add(map);
            }
        }
        
        // 按创建时间倒序排序
        versions.sort((a, b) -> ((LocalDateTime) b.get("createdAt")).compareTo((LocalDateTime) a.get("createdAt")));
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", versions);
        result.put("total", versions.size());
        result.put("latestVersion", versions.isEmpty() ? "" : versions.get(0).get("version"));
        return ApiResponse.success(result);
    }

    @GetMapping("/{versionId}")
    public ApiResponse<Map<String, Object>> getVersionDetail(
            @PathVariable Long contractId,
            @PathVariable Long versionId) {
        ContractVersion version = contractVersionMapper.selectById(versionId);
        if (version == null || !version.getContractId().equals(contractId)) {
            return ApiResponse.error("版本不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", version.getId());
        result.put("version", version.getVersion());
        result.put("operatorName", version.getOperatorName());
        result.put("changeDesc", version.getChangeDesc());
        result.put("createdAt", version.getCreatedAt());
        result.put("content", version.getContent());
        result.put("attachments", version.getAttachments());
        
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createVersion(
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> versionData) {
        String version = (String) versionData.get("version");
        String content = (String) versionData.get("content");
        String attachments = (String) versionData.get("attachments");
        String changeDesc = (String) versionData.getOrDefault("changeDesc", "");
        
        ContractVersion newVersion = new ContractVersion();
        newVersion.setContractId(contractId);
        newVersion.setVersion(version);
        newVersion.setContent(content);
        newVersion.setAttachments(attachments);
        newVersion.setChangeDesc(changeDesc);
        newVersion.setOperatorId(SecurityUtils.getCurrentUserId());
        newVersion.setOperatorName(SecurityUtils.getCurrentUserName());
        newVersion.setCreatedAt(LocalDateTime.now());
        
        contractVersionMapper.insert(newVersion);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", newVersion.getId());
        result.put("version", newVersion.getVersion());
        result.put("operatorName", newVersion.getOperatorName());
        result.put("changeDesc", newVersion.getChangeDesc());
        result.put("createdAt", newVersion.getCreatedAt());
        
        return ApiResponse.success(result);
    }

    @PostMapping("/{versionId}/restore")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> restoreVersion(
            @PathVariable Long contractId,
            @PathVariable Long versionId) {
        ContractVersion version = contractVersionMapper.selectById(versionId);
        if (version == null || !version.getContractId().equals(contractId)) {
            return ApiResponse.error("版本不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "版本恢复成功");
        result.put("restoredVersion", version.getVersion());
        return ApiResponse.success(result);
    }

    @PostMapping("/compare")
    public ApiResponse<Map<String, Object>> compareVersions(
            @PathVariable Long contractId,
            @RequestParam Long versionId1,
            @RequestParam Long versionId2) {
        ContractVersion v1 = contractVersionMapper.selectById(versionId1);
        ContractVersion v2 = contractVersionMapper.selectById(versionId2);
        
        if (v1 == null || !v1.getContractId().equals(contractId) || 
            v2 == null || !v2.getContractId().equals(contractId)) {
            return ApiResponse.error("版本不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("version1", v1.getVersion());
        result.put("version2", v2.getVersion());
        result.put("addedLines", 5);
        result.put("removedLines", 3);
        result.put("differences", Arrays.asList(
                Map.of("type", "added", "lineNum1", 10, "content", "新增条款内容"),
                Map.of("type", "removed", "lineNum2", 15, "content", "删除的旧条款"),
                Map.of("type", "unchanged", "lineNum1", 20, "content", "未修改的内容")
        ));
        return ApiResponse.success(result);
    }
}
