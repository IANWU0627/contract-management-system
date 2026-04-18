package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.mapper.ContractRenewalMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.entity.ContractRenewal;
import com.contracthub.entity.Contract;
import com.contracthub.service.NotificationService;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/contracts/{contractId}/renewals")
@PreAuthorize("hasAuthority('RENEWAL_MANAGE')")
public class RenewalController {

    private final ContractRenewalMapper renewalMapper;
    private final ContractMapper contractMapper;
    private final NotificationService notificationService;

    public RenewalController(ContractRenewalMapper renewalMapper, ContractMapper contractMapper, 
                            NotificationService notificationService) {
        this.renewalMapper = renewalMapper;
        this.contractMapper = contractMapper;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getRenewals(@PathVariable Long contractId) {
        List<Map<String, Object>> renewals = renewalMapper.selectRenewalsByContractId(contractId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", renewals);
        result.put("total", renewals.size());
        
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createRenewal(
            @PathVariable Long contractId,
            @RequestBody Map<String, Object> renewalData) {
        
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }
        
        ContractRenewal renewal = new ContractRenewal();
        renewal.setContractId(contractId);
        renewal.setOldEndDate(contract.getEndDate());
        
        String newEndDateStr = (String) renewalData.get("newEndDate");
        renewal.setNewEndDate(LocalDate.parse(newEndDateStr));
        
        renewal.setRenewalType((String) renewalData.getOrDefault("renewalType", "EXTEND"));
        renewal.setStatus("PENDING");
        renewal.setRemark((String) renewalData.get("remark"));
        renewal.setOperatorId(SecurityUtils.getCurrentUserId());
        renewal.setOperatorName(SecurityUtils.getCurrentUserName());
        
        renewalMapper.insert(renewal);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", renewal.getId());
        result.put("contractId", contractId);
        result.put("oldEndDate", renewal.getOldEndDate());
        result.put("newEndDate", renewal.getNewEndDate());
        result.put("status", renewal.getStatus());
        
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','LEGAL')")
    public ApiResponse<Void> approveRenewal(@PathVariable Long contractId, @PathVariable Long id) {
        ContractRenewal renewal = renewalMapper.selectById(id);
        if (renewal == null || !renewal.getContractId().equals(contractId)) {
            return ApiResponse.error("续约记录不存在", "error.renewal.notFound");
        }
        
        Contract contract = contractMapper.selectById(contractId);
        if (contract != null) {
            contract.setEndDate(renewal.getNewEndDate());
            contractMapper.updateById(contract);
        }
        
        renewal.setStatus("APPROVED");
        renewalMapper.updateById(renewal);
        
        // 通知续约申请人
        if (renewal.getOperatorId() != null) {
            notificationService.sendRenewalResult(renewal.getOperatorId(), contractId, 
                contract != null ? contract.getContractNo() : "", "APPROVED");
        }
        
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','LEGAL')")
    public ApiResponse<Void> rejectRenewal(@PathVariable Long contractId, @PathVariable Long id, @RequestBody Map<String, Object> data) {
        ContractRenewal renewal = renewalMapper.selectById(id);
        if (renewal == null || !renewal.getContractId().equals(contractId)) {
            return ApiResponse.error("续约记录不存在", "error.renewal.notFound");
        }
        
        renewal.setStatus("REJECTED");
        if (data.containsKey("remark")) {
            renewal.setRemark((String) data.get("remark"));
        }
        renewalMapper.updateById(renewal);
        
        // 通知续约申请人
        Contract contract = contractMapper.selectById(contractId);
        if (renewal.getOperatorId() != null) {
            notificationService.sendRenewalResult(renewal.getOperatorId(), contractId, 
                contract != null ? contract.getContractNo() : "", "REJECTED");
        }
        
        return ApiResponse.success(null);
    }
}

@RestController
@RequestMapping("/api/renewals")
@PreAuthorize("hasAuthority('RENEWAL_MANAGE')")
class RenewalListController {
    
    private final ContractRenewalMapper renewalMapper;
    
    public RenewalListController(ContractRenewalMapper renewalMapper) {
        this.renewalMapper = renewalMapper;
    }
    
    @GetMapping
    public ApiResponse<Map<String, Object>> getAllRenewals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Map<String, Object>> renewals;
        if (status != null && !status.isEmpty()) {
            renewals = renewalMapper.selectRenewalsByStatus(status);
        } else {
            renewals = renewalMapper.selectRenewalsByStatus(null);
        }
        
        // 分页处理
        int total = renewals.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> paginatedRenewals = new ArrayList<>();
        if (start < total) {
            paginatedRenewals = renewals.subList(start, end);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", paginatedRenewals);
        result.put("total", total);
        
        return ApiResponse.success(result);
    }
}
