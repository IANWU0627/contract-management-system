package com.contracthub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.annotation.RequirePermission;
import com.contracthub.entity.ContractVersion;
import com.contracthub.mapper.ContractVersionMapper;
import com.contracthub.service.ContractVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContractVersionServiceImpl implements ContractVersionService {

    @Autowired
    private ContractVersionMapper versionMapper;

    @Override
    @Transactional
    public ContractVersion createVersion(Long contractId, String content, String changeDesc,
                                          Long operatorId, String operatorName) {
        String nextVersion = generateNextVersion(contractId);
        
        ContractVersion version = new ContractVersion();
        version.setContractId(contractId);
        version.setVersion(nextVersion);
        version.setContent(content);
        version.setChangeDesc(changeDesc);
        version.setOperatorId(operatorId);
        version.setOperatorName(operatorName);
        version.setCreatedAt(LocalDateTime.now());
        
        versionMapper.insert(version);
        return version;
    }

    @Override
    public List<ContractVersion> getVersionHistory(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at");
        return versionMapper.selectList(wrapper);
    }

    @Override
    public ContractVersion getVersionDetail(Long contractId, Long versionId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("id", versionId)
               .eq("contract_id", contractId);
        return versionMapper.selectOne(wrapper);
    }

    @Override
    public String getLatestVersion(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        ContractVersion latest = versionMapper.selectOne(wrapper);
        return latest != null ? latest.getVersion() : "v1.0";
    }

    @Override
    @Transactional
    @RequirePermission(value = "version:restore", description = "恢复历史版本")
    public Map<String, Object> restoreVersion(Long contractId, Long versionId,
                                               Long operatorId, String operatorName) {
        ContractVersion targetVersion = getVersionDetail(contractId, versionId);
        if (targetVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        String nextVersion = generateNextVersion(contractId);
        
        ContractVersion newVersion = new ContractVersion();
        newVersion.setContractId(contractId);
        newVersion.setVersion(nextVersion);
        newVersion.setContent(targetVersion.getContent());
        newVersion.setChangeDesc("从版本 " + targetVersion.getVersion() + " 恢复");
        newVersion.setOperatorId(operatorId);
        newVersion.setOperatorName(operatorName);
        newVersion.setCreatedAt(LocalDateTime.now());
        
        versionMapper.insert(newVersion);

        Map<String, Object> result = new HashMap<>();
        result.put("restoredVersion", targetVersion.getVersion());
        result.put("newVersion", nextVersion);
        result.put("content", targetVersion.getContent());
        return result;
    }

    @Override
    public Map<String, Object> compareVersions(Long contractId, Long versionId1, Long versionId2) {
        ContractVersion v1 = getVersionDetail(contractId, versionId1);
        ContractVersion v2 = getVersionDetail(contractId, versionId2);
        
        if (v1 == null || v2 == null) {
            throw new RuntimeException("版本不存在");
        }

        String content1 = v1.getContent() != null ? v1.getContent() : "";
        String content2 = v2.getContent() != null ? v2.getContent() : "";
        
        List<Map<String, Object>> differences = calculateDiff(content1, content2);
        
        Map<String, Object> result = new HashMap<>();
        result.put("version1", v1.getVersion());
        result.put("version2", v2.getVersion());
        result.put("differences", differences);
        result.put("addedLines", countAddedLines(differences));
        result.put("removedLines", countRemovedLines(differences));
        return result;
    }

    @Override
    public String generateNextVersion(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        ContractVersion latest = versionMapper.selectOne(wrapper);
        
        if (latest == null) {
            return "v1.0";
        }
        
        String currentVersion = latest.getVersion();
        try {
            String versionNum = currentVersion.replace("v", "");
            String[] parts = versionNum.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            
            minor++;
            if (minor >= 10) {
                major++;
                minor = 0;
            }
            return "v" + major + "." + minor;
        } catch (Exception e) {
            return "v1.0";
        }
    }

    private List<Map<String, Object>> calculateDiff(String content1, String content2) {
        List<Map<String, Object>> diff = new ArrayList<>();
        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");
        
        int i = 0, j = 0;
        while (i < lines1.length || j < lines2.length) {
            if (i < lines1.length && j < lines2.length && lines1[i].equals(lines2[j])) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "unchanged");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                item.put("lineNum2", j + 1);
                diff.add(item);
                i++;
                j++;
            } else if (j < lines2.length && (i >= lines1.length || !contains(lines1, lines2[j], i))) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "added");
                item.put("content", lines2[j]);
                item.put("lineNum2", j + 1);
                diff.add(item);
                j++;
            } else if (i < lines1.length) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "removed");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                diff.add(item);
                i++;
            }
        }
        return diff;
    }

    private boolean contains(String[] array, String target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i].equals(target)) return true;
        }
        return false;
    }

    private int countAddedLines(List<Map<String, Object>> differences) {
        return (int) differences.stream()
                .filter(d -> "added".equals(d.get("type")))
                .count();
    }

    private int countRemovedLines(List<Map<String, Object>> differences) {
        return (int) differences.stream()
                .filter(d -> "removed".equals(d.get("type")))
                .count();
    }
}
