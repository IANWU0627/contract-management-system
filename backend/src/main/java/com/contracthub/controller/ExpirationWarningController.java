package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.service.ContractDataScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/contracts")
@PreAuthorize("hasAuthority('REPORT_VIEW')")
public class ExpirationWarningController {
    
    private final ContractMapper contractMapper;
    private final ContractDataScopeService contractDataScopeService;
    
    public ExpirationWarningController(ContractMapper contractMapper, ContractDataScopeService contractDataScopeService) {
        this.contractMapper = contractMapper;
        this.contractDataScopeService = contractDataScopeService;
    }
    
    @GetMapping("/expiring")
    public ApiResponse<Map<String, Object>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date")
              .ge("end_date", today)
              .le("end_date", endDate)
              .orderByAsc("end_date");
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        applyRoleIsolation(wrapper);
        
        List<Contract> contracts = contractMapper.selectList(wrapper);
        
        List<Map<String, Object>> expiringContracts = new ArrayList<>();
        Map<String, Integer> typeStats = new HashMap<>();
        
        for (Contract contract : contracts) {
            LocalDate end = contract.getEndDate();
            long daysRemaining = ChronoUnit.DAYS.between(today, end);
            
            Map<String, Object> item = new HashMap<>();
            item.put("id", contract.getId());
            item.put("contractNo", contract.getContractNo());
            item.put("title", contract.getTitle());
            item.put("type", contract.getType());
            item.put("status", contract.getStatus());
            item.put("amount", contract.getAmount());
            item.put("startDate", contract.getStartDate());
            item.put("endDate", contract.getEndDate());
            item.put("daysRemaining", daysRemaining);
            item.put("counterparty", resolveCounterpartyText(contract));
            item.put("creatorId", contract.getCreatorId());
            
            String expireStatus;
            String expireStatusText;
            if (daysRemaining == 0) {
                expireStatus = "today";
                expireStatusText = "今天到期";
            } else if (daysRemaining <= 7) {
                expireStatus = "urgent";
                expireStatusText = "剩余 " + daysRemaining + " 天";
            } else {
                expireStatus = "warning";
                expireStatusText = "剩余 " + daysRemaining + " 天";
            }
            item.put("expireStatus", expireStatus);
            item.put("expireStatusText", expireStatusText);
            
            expiringContracts.add(item);
            
            String contractType = contract.getType() != null ? contract.getType() : "OTHER";
            typeStats.put(contractType, typeStats.getOrDefault(contractType, 0) + 1);
        }
        
        QueryWrapper<Contract> expiredWrapper = new QueryWrapper<>();
        expiredWrapper.isNotNull("end_date").lt("end_date", today);
        applyRoleIsolation(expiredWrapper);
        long expiredCount = contractMapper.selectCount(expiredWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", expiringContracts);
        result.put("total", expiringContracts.size());
        result.put("stats", Map.of(
            "expired", expiredCount,
            "today", expiringContracts.stream().filter(c -> "today".equals(c.get("expireStatus"))).count(),
            "urgent", expiringContracts.stream().filter(c -> "urgent".equals(c.get("expireStatus"))).count(),
            "warning", expiringContracts.stream().filter(c -> "warning".equals(c.get("expireStatus"))).count()
        ));
        result.put("byType", typeStats);
        
        return ApiResponse.success(result);
    }
    
    @GetMapping("/statistics/expiration")
    public ApiResponse<Map<String, Object>> getExpirationStats() {
        LocalDate today = LocalDate.now();
        
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        applyRoleIsolation(wrapper);
        
        long totalContracts = contractMapper.selectCount(wrapper);
        
        wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date").lt("end_date", today);
        applyRoleIsolation(wrapper);
        long alreadyExpired = contractMapper.selectCount(wrapper);
        
        wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date")
              .ge("end_date", today)
              .le("end_date", today.plusDays(7));
        applyRoleIsolation(wrapper);
        long expiringThisWeek = contractMapper.selectCount(wrapper);
        
        wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date")
              .ge("end_date", today)
              .le("end_date", today.plusDays(30));
        applyRoleIsolation(wrapper);
        long expiringThisMonth = contractMapper.selectCount(wrapper);
        
        wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date")
              .ge("end_date", today)
              .le("end_date", today.plusDays(90));
        applyRoleIsolation(wrapper);
        long expiringThisQuarter = contractMapper.selectCount(wrapper);
        
        QueryWrapper<Contract> amountWrapper = new QueryWrapper<>();
        amountWrapper.select("coalesce(sum(amount), 0) as total_amount");
        applyRoleIsolation(amountWrapper);
        Map<String, Object> totalResult = contractMapper.selectMaps(amountWrapper).stream().findFirst().orElse(new HashMap<>());
        double totalAmount = totalResult.get("total_amount") != null ? ((Number) totalResult.get("total_amount")).doubleValue() : 0;
        
        amountWrapper = new QueryWrapper<>();
        amountWrapper.isNotNull("end_date")
                    .le("end_date", today.plusDays(30))
                    .select("coalesce(sum(amount), 0) as total_amount");
        applyRoleIsolation(amountWrapper);
        Map<String, Object> expiringResult = contractMapper.selectMaps(amountWrapper).stream().findFirst().orElse(new HashMap<>());
        double expiringAmount = expiringResult.get("total_amount") != null ? ((Number) expiringResult.get("total_amount")).doubleValue() : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalContracts", totalContracts);
        result.put("expiringThisWeek", expiringThisWeek);
        result.put("expiringThisMonth", expiringThisMonth);
        result.put("expiringThisQuarter", expiringThisQuarter);
        result.put("alreadyExpired", alreadyExpired);
        result.put("totalAmount", totalAmount);
        result.put("expiringAmount", expiringAmount);
        result.put("expiringPercent", totalAmount > 0 ? (expiringAmount / totalAmount * 100) : 0);
        
        return ApiResponse.success(result);
    }

    @GetMapping("/workbench/expiring-summary")
    public ApiResponse<Map<String, Object>> getExpiringWorkbenchSummary() {
        LocalDate today = LocalDate.now();
        LocalDate d1 = today.plusDays(1);
        LocalDate d7 = today.plusDays(7);
        LocalDate d30 = today.plusDays(30);

        List<Contract> contracts = queryContractsWithin(today, d30);
        List<Map<String, Object>> bucket1d = new ArrayList<>();
        List<Map<String, Object>> bucket7d = new ArrayList<>();
        List<Map<String, Object>> bucket30d = new ArrayList<>();

        for (Contract contract : contracts) {
            LocalDate endDate = contract.getEndDate();
            if (endDate == null) {
                continue;
            }
            long daysRemaining = ChronoUnit.DAYS.between(today, endDate);
            Map<String, Object> item = toWorkbenchItem(contract, daysRemaining);
            if (!endDate.isAfter(d1)) {
                bucket1d.add(item);
            }
            if (!endDate.isAfter(d7)) {
                bucket7d.add(item);
            }
            bucket30d.add(item);
        }

        Map<String, Object> overview = new HashMap<>();
        overview.put("today", bucket1d.size());
        overview.put("within7Days", bucket7d.size());
        overview.put("within30Days", bucket30d.size());
        overview.put("totalAmount", bucket30d.stream()
                .map(i -> i.get("amount"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToDouble(Number::doubleValue)
                .sum());

        Map<String, Object> result = new HashMap<>();
        result.put("overview", overview);
        result.put("oneDay", bucket1d);
        result.put("sevenDays", bucket7d);
        result.put("thirtyDays", bucket30d);
        return ApiResponse.success(result);
    }

    private List<Contract> queryContractsWithin(LocalDate start, LocalDate end) {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("end_date")
                .ge("end_date", start)
                .le("end_date", end)
                .orderByAsc("end_date");
        applyRoleIsolation(wrapper);
        return contractMapper.selectList(wrapper);
    }

    private Map<String, Object> toWorkbenchItem(Contract contract, long daysRemaining) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", contract.getId());
        item.put("contractNo", contract.getContractNo());
        item.put("title", contract.getTitle());
        item.put("type", contract.getType());
        item.put("status", contract.getStatus());
        item.put("amount", contract.getAmount());
        item.put("endDate", contract.getEndDate());
        item.put("daysRemaining", daysRemaining);
        item.put("counterparty", resolveCounterpartyText(contract));
        item.put("creatorId", contract.getCreatorId());
        item.put("currentApproverName", contract.getCurrentApproverName());

        String recommendedAction;
        if ("SIGNED".equals(contract.getStatus()) || "APPROVED".equals(contract.getStatus())) {
            recommendedAction = "startRenewal";
        } else if ("DRAFT".equals(contract.getStatus())) {
            recommendedAction = "submit";
        } else {
            recommendedAction = "view";
        }
        item.put("recommendedAction", recommendedAction);
        return item;
    }

    private void applyRoleIsolation(QueryWrapper<Contract> wrapper) {
        contractDataScopeService.applyContractVisibilityFilter(wrapper);
    }

    private String resolveCounterpartyText(Contract contract) {
        String counterpartiesJson = contract.getCounterparties();
        if (counterpartiesJson == null || counterpartiesJson.isBlank()) {
            return "";
        }
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cps = new com.fasterxml.jackson.databind.ObjectMapper().readValue(counterpartiesJson, List.class);
            return cps.stream()
                .map(cp -> cp.get("name"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .reduce((a, b) -> a + " / " + b)
                .orElse("");
        } catch (Exception ignored) {
            return "";
        }
    }
}
