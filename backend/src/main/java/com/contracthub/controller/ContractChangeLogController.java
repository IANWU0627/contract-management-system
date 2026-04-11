package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractChangeLog;
import com.contracthub.mapper.ContractChangeLogMapper;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/contracts/{contractId}/change-logs")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
public class ContractChangeLogController {

    private final ContractChangeLogMapper changeLogMapper;

    public ContractChangeLogController(ContractChangeLogMapper changeLogMapper) {
        this.changeLogMapper = changeLogMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getChangeLogs(@PathVariable Long contractId) {
        QueryWrapper<ContractChangeLog> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at");
        List<ContractChangeLog> logs = changeLogMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", logs.size());
        
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<ContractChangeLog> createChangeLog(
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> logData) {
        
        ContractChangeLog log = new ContractChangeLog();
        log.setContractId(contractId);
        log.setChangeType((String) logData.get("changeType"));
        log.setFieldName((String) logData.get("fieldName"));
        log.setOldValue((String) logData.get("oldValue"));
        log.setNewValue((String) logData.get("newValue"));
        log.setRemark((String) logData.get("remark"));
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUserName());
        
        changeLogMapper.insert(log);
        
        return ApiResponse.success(log);
    }
}

@RestController
@RequestMapping("/api/change-logs")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
class ChangeLogListController {
    
    private final ContractChangeLogMapper changeLogMapper;
    
    public ChangeLogListController(ContractChangeLogMapper changeLogMapper) {
        this.changeLogMapper = changeLogMapper;
    }
    
    @GetMapping("/recent")
    public ApiResponse<Map<String, Object>> getRecentLogs(@RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> logs = changeLogMapper.selectRecent(limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", logs.size());
        
        return ApiResponse.success(result);
    }
    
    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyLogs(@RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> logs = changeLogMapper.selectByOperator(userId, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", logs.size());
        
        return ApiResponse.success(result);
    }
}
