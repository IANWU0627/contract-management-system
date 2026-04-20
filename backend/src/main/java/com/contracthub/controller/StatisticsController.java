package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.service.ContractDataScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasAuthority('REPORT_VIEW')")
public class StatisticsController {
    
    private final ContractMapper contractMapper;
    private final ContractDataScopeService contractDataScopeService;
    
    public StatisticsController(ContractMapper contractMapper, ContractDataScopeService contractDataScopeService) {
        this.contractMapper = contractMapper;
        this.contractDataScopeService = contractDataScopeService;
    }
    
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        try {
            QueryWrapper<Contract> wrapper = new QueryWrapper<>();
            wrapper.select("amount", "status", "end_date", "create_time", "update_time");
            applyRoleIsolation(wrapper);
            List<Map<String, Object>> contractList = contractMapper.selectMaps(wrapper);
            Map<String, Object> stats = new HashMap<>();
            
            int totalContracts = contractList.size();
            stats.put("totalContracts", totalContracts);
            
            BigDecimal totalAmount = contractList.stream()
                .map(item -> (BigDecimal) item.get("amount"))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalAmount", totalAmount.doubleValue());
            
            long pendingApproval = contractList.stream()
                .filter(c -> "PENDING".equals(String.valueOf(c.get("status"))))
                .count();
            stats.put("pendingApproval", pendingApproval);
            
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysLater = today.plusDays(30);
            long expiringSoon = contractList.stream()
                .filter(c -> {
                    LocalDate endDate = toLocalDate(c.get("end_date"));
                    return endDate != null && !endDate.isBefore(today) && !endDate.isAfter(thirtyDaysLater);
                })
                .count();
            stats.put("expiringSoon", expiringSoon);
            
            String thisMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String previousMonth = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            long monthlyNew = contractList.stream()
                .filter(c -> {
                    LocalDateTime createTime = toLocalDateTime(c.get("create_time"));
                    if (createTime == null) return false;
                    return createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(thisMonth);
                })
                .count();
            stats.put("monthlyNew", monthlyNew);
            
            long signedThisMonth = contractList.stream()
                .filter(c -> {
                    if (!"SIGNED".equals(String.valueOf(c.get("status")))) return false;
                    LocalDateTime updateTime = toLocalDateTime(c.get("update_time"));
                    if (updateTime == null) return false;
                    return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(thisMonth);
                })
                .count();
            stats.put("signedThisMonth", signedThisMonth);

            BigDecimal thisMonthAmount = contractList.stream()
                    .filter(c -> {
                        LocalDateTime createTime = toLocalDateTime(c.get("create_time"));
                        return createTime != null
                                && createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(thisMonth);
                    })
                    .map(item -> (BigDecimal) item.get("amount"))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal previousMonthAmount = contractList.stream()
                    .filter(c -> {
                        LocalDateTime createTime = toLocalDateTime(c.get("create_time"));
                        return createTime != null
                                && createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(previousMonth);
                    })
                    .map(item -> (BigDecimal) item.get("amount"))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long previousMonthlyNew = contractList.stream()
                    .filter(c -> {
                        LocalDateTime createTime = toLocalDateTime(c.get("create_time"));
                        return createTime != null
                                && createTime.format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(previousMonth);
                    })
                    .count();

            double amountGrowth = calculateGrowthRate(previousMonthAmount.doubleValue(), thisMonthAmount.doubleValue());
            double contractGrowth = calculateGrowthRate(previousMonthlyNew, monthlyNew);
            stats.put("amountGrowth", amountGrowth);
            stats.put("contractGrowth", contractGrowth);
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("统计数据获取失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/status-distribution")
    public ApiResponse<List<Map<String, Object>>> statusDistribution() {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.select("status");
        applyRoleIsolation(wrapper);
        List<Map<String, Object>> contracts = contractMapper.selectMaps(wrapper);
        
        Map<String, Long> statusCount = new HashMap<>();
        for (Map<String, Object> contract : contracts) {
            String status = contract.get("status") != null ? String.valueOf(contract.get("status")) : "OTHER";
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
        }
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", entry.getKey());
            item.put("value", entry.getValue());
            data.add(item);
        }
        
        return ApiResponse.success(data);
    }
    
    @GetMapping("/type-distribution")
    public ApiResponse<List<Map<String, Object>>> typeDistribution() {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.select("type");
        applyRoleIsolation(wrapper);
        List<Map<String, Object>> contracts = contractMapper.selectMaps(wrapper);
        
        Map<String, Long> typeCount = new HashMap<>();
        for (Map<String, Object> contract : contracts) {
            String type = contract.get("type") != null ? String.valueOf(contract.get("type")).toUpperCase() : "OTHER";
            typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
        }
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", entry.getKey());
            item.put("value", entry.getValue());
            data.add(item);
        }
        
        return ApiResponse.success(data);
    }
    
    @GetMapping("/monthly-trend")
    public ApiResponse<List<Map<String, Object>>> monthlyTrend() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            String monthLabel = month.format(DateTimeFormatter.ofPattern("M月"));
            LocalDate startOfMonth = month.withDayOfMonth(1);
            LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
            
            QueryWrapper<Contract> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", startOfMonth.atStartOfDay())
                  .lt("create_time", endOfMonth.plusDays(1).atStartOfDay())
                  .select("count(*) as count, coalesce(sum(amount), 0) as total_amount");
            applyRoleIsolation(wrapper);
            
            Map<String, Object> result = contractMapper.selectMaps(wrapper).stream().findFirst().orElse(new HashMap<>());
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthLabel);
            monthData.put("count", result.get("count") != null ? result.get("count") : 0);
            monthData.put("amount", result.get("total_amount") != null ? result.get("total_amount") : 0);
            monthlyData.add(monthData);
        }
        
        return ApiResponse.success(monthlyData);
    }
    
    @GetMapping("/top-counterparties")
    public ApiResponse<List<Map<String, Object>>> topCounterparties() {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.select("id", "counterparties", "amount");
        applyRoleIsolation(wrapper);
        List<Contract> contracts = contractMapper.selectList(wrapper);

        Map<String, Map<String, Object>> agg = new HashMap<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (Contract c : contracts) {
            List<String> names = new ArrayList<>();
            if (c.getCounterparties() != null && !c.getCounterparties().isBlank()) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> cps = mapper.readValue(c.getCounterparties(), List.class);
                    for (Map<String, Object> cp : cps) {
                        Object nameObj = cp.get("name");
                        if (nameObj != null && !nameObj.toString().isBlank()) {
                            names.add(nameObj.toString().trim());
                        }
                    }
                } catch (Exception ignored) {
                    // ignore malformed historical json
                }
            }
            if (names.isEmpty()) {
                continue;
            }
            double amount = c.getAmount() != null ? c.getAmount().doubleValue() : 0d;
            for (String name : names.stream().distinct().toList()) {
                Map<String, Object> item = agg.computeIfAbsent(name, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", k);
                    m.put("count", 0L);
                    m.put("amount", 0d);
                    return m;
                });
                item.put("count", ((Long) item.get("count")) + 1L);
                item.put("amount", ((Double) item.get("amount")) + amount);
            }
        }

        List<Map<String, Object>> data = new ArrayList<>(agg.values());
        data.sort((a, b) -> Double.compare((Double) b.get("amount"), (Double) a.get("amount")));
        if (data.size() > 5) {
            data = data.subList(0, 5);
        }
        return ApiResponse.success(data);
    }
    
    @GetMapping("/user-activity")
    public ApiResponse<List<Map<String, Object>>> userActivity() {
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.select("creator_id", 
                      "count(*) as total",
                      "sum(case when status = 'DRAFT' then 1 else 0 end) as draft_count",
                      "sum(case when status in ('PENDING', 'APPROVING') then 1 else 0 end) as pending_count",
                      "sum(case when status in ('SIGNED', 'APPROVED') then 1 else 0 end) as signed_count",
                      "coalesce(sum(amount), 0) as total_amount")
              .isNotNull("creator_id")
              .groupBy("creator_id")
              .orderByDesc("total");
        applyRoleIsolation(wrapper);
        
        List<Map<String, Object>> rawResult = contractMapper.selectMaps(wrapper);
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rawResult) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", row.get("creator_id"));
            item.put("totalContracts", row.get("total"));
            item.put("draftContracts", row.get("draft_count"));
            item.put("pendingContracts", row.get("pending_count"));
            item.put("signedContracts", row.get("signed_count"));
            item.put("totalAmount", row.get("total_amount"));
            data.add(item);
        }
        
        return ApiResponse.success(data);
    }

    private void applyRoleIsolation(QueryWrapper<Contract> wrapper) {
        contractDataScopeService.applyContractVisibilityFilter(wrapper);
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof CharSequence cs) {
            try {
                return LocalDate.parse(cs.toString());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }
        if (value instanceof java.util.Date utilDate) {
            return LocalDateTime.ofInstant(utilDate.toInstant(), java.time.ZoneId.systemDefault());
        }
        if (value instanceof CharSequence cs) {
            try {
                return LocalDateTime.parse(cs.toString());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private double calculateGrowthRate(double previousValue, double currentValue) {
        if (previousValue == 0d) {
            return currentValue == 0d ? 0d : 100d;
        }
        return Math.round(((currentValue - previousValue) / previousValue) * 1000d) / 10d;
    }
}
