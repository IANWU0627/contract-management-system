package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractVersion;
import com.contracthub.service.ContractVersionService;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/contracts/{contractId}/versions")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
public class ContractVersionController {

    private final ContractVersionService contractVersionService;

    public ContractVersionController(ContractVersionService contractVersionService) {
        this.contractVersionService = contractVersionService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getVersionHistory(@PathVariable Long contractId) {
        List<ContractVersion> versionList = contractVersionService.getVersionHistory(contractId);
        List<Map<String, Object>> versions = new ArrayList<>();
        
        for (ContractVersion version : versionList) {
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
        ContractVersion version = contractVersionService.getVersionDetail(contractId, versionId);
        if (version == null) {
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
        String content = (String) versionData.get("content");
        String changeDesc = (String) versionData.getOrDefault("changeDesc", "");
        ContractVersion newVersion = contractVersionService.createVersion(
                contractId,
                content,
                changeDesc,
                SecurityUtils.getCurrentUserId(),
                SecurityUtils.getCurrentUserName()
        );
        
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
        try {
            Map<String, Object> result = contractVersionService.restoreVersion(
                    contractId,
                    versionId,
                    SecurityUtils.getCurrentUserId(),
                    SecurityUtils.getCurrentUserName()
            );
            result.put("message", "版本恢复成功");
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/compare")
    public ApiResponse<Map<String, Object>> compareVersions(
            @PathVariable Long contractId,
            @RequestParam Long versionId1,
            @RequestParam Long versionId2) {
        try {
            return ApiResponse.success(contractVersionService.compareVersions(contractId, versionId1, versionId2));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
