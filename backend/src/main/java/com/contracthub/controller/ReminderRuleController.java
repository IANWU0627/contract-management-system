package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.mapper.ReminderRuleMapper;
import com.contracthub.entity.ReminderRule;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/reminder-rules")
@PreAuthorize("hasAuthority('REMINDER_RULE_MANAGE')")
public class ReminderRuleController {

    private final ReminderRuleMapper ruleMapper;

    public ReminderRuleController(ReminderRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getRules() {
        List<ReminderRule> rules = ruleMapper.selectAllRules();
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (ReminderRule rule : rules) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rule.getId());
            map.put("name", rule.getName());
            if (rule.getContractType() != null && !rule.getContractType().isEmpty()) {
                map.put("contractType", rule.getContractType().split(","));
            } else {
                map.put("contractType", new String[0]);
            }
            map.put("minAmount", rule.getMinAmount());
            map.put("maxAmount", rule.getMaxAmount());
            map.put("remindDays", rule.getRemindDays());
            map.put("isEnabled", rule.getIsEnabled());
            map.put("creatorId", rule.getCreatorId());
            map.put("isPublic", rule.getIsPublic());
            map.put("createdAt", rule.getCreatedAt());
            map.put("updatedAt", rule.getUpdatedAt());
            list.add(map);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.success(result);
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyRules() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ReminderRule> rules = ruleMapper.selectAllRules();
        
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (ReminderRule rule : rules) {
            Long creatorId = rule.getCreatorId();
            Boolean isPublic = rule.getIsPublic() != null && rule.getIsPublic();
            
            if (isPublic || (creatorId != null && creatorId.equals(userId))) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rule.getId());
                map.put("name", rule.getName());
                if (rule.getContractType() != null && !rule.getContractType().isEmpty()) {
                    map.put("contractType", rule.getContractType().split(","));
                } else {
                    map.put("contractType", new String[0]);
                }
                map.put("minAmount", rule.getMinAmount());
                map.put("maxAmount", rule.getMaxAmount());
                map.put("remindDays", rule.getRemindDays());
                map.put("isEnabled", rule.getIsEnabled());
                map.put("creatorId", rule.getCreatorId());
                map.put("isPublic", rule.getIsPublic());
                map.put("createdAt", rule.getCreatedAt());
                map.put("updatedAt", rule.getUpdatedAt());
                filteredList.add(map);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", filteredList);
        result.put("total", filteredList.size());
        
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createRule(@RequestBody Map<String, Object> ruleData) {
        ReminderRule rule = new ReminderRule();
        rule.setName((String) ruleData.get("name"));
        
        if (ruleData.containsKey("contractType")) {
            Object contractTypeObj = ruleData.get("contractType");
            if (contractTypeObj instanceof List) {
                List<?> typeList = (List<?>) contractTypeObj;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < typeList.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(typeList.get(i).toString());
                }
                rule.setContractType(sb.toString());
            } else if (contractTypeObj instanceof String) {
                rule.setContractType((String) contractTypeObj);
            }
        }
        
        if (ruleData.containsKey("minAmount")) {
            rule.setMinAmount(new BigDecimal(ruleData.get("minAmount").toString()));
        }
        if (ruleData.containsKey("maxAmount")) {
            rule.setMaxAmount(new BigDecimal(ruleData.get("maxAmount").toString()));
        }
        
        rule.setRemindDays((Integer) ruleData.getOrDefault("remindDays", 30));
        rule.setIsEnabled(true);
        rule.setCreatorId(SecurityUtils.getCurrentUserId());
        rule.setIsPublic(ruleData.containsKey("isPublic") ? (Boolean) ruleData.get("isPublic") : true);
        
        ruleMapper.insert(rule);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", rule.getId());
        result.put("name", rule.getName());
        result.put("remindDays", rule.getRemindDays());
        result.put("creatorId", rule.getCreatorId());
        result.put("isPublic", rule.getIsPublic());
        
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> ruleData) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = rule.getIsPublic() != null && rule.getIsPublic();
        
        if (!isPublic && rule.getCreatorId() != null && !rule.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限修改此规则");
        }
        
        if (ruleData.containsKey("name")) {
            rule.setName((String) ruleData.get("name"));
        }
        
        if (ruleData.containsKey("contractType")) {
            Object contractTypeObj = ruleData.get("contractType");
            if (contractTypeObj instanceof List) {
                List<?> typeList = (List<?>) contractTypeObj;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < typeList.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(typeList.get(i).toString());
                }
                rule.setContractType(sb.toString());
            } else if (contractTypeObj instanceof String) {
                rule.setContractType((String) contractTypeObj);
            }
        }
        
        if (ruleData.containsKey("minAmount")) {
            rule.setMinAmount(new BigDecimal(ruleData.get("minAmount").toString()));
        }
        if (ruleData.containsKey("maxAmount")) {
            rule.setMaxAmount(new BigDecimal(ruleData.get("maxAmount").toString()));
        }
        if (ruleData.containsKey("remindDays")) {
            rule.setRemindDays((Integer) ruleData.get("remindDays"));
        }
        if (ruleData.containsKey("isEnabled")) {
            rule.setIsEnabled((Boolean) ruleData.get("isEnabled"));
        }
        
        ruleMapper.updateById(rule);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = rule.getIsPublic() != null && rule.getIsPublic();
        
        if (!isPublic && rule.getCreatorId() != null && !rule.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限删除此规则");
        }
        
        ruleMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<Void> toggleRule(@PathVariable Long id) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在");
        }
        
        rule.setIsEnabled(!rule.getIsEnabled());
        ruleMapper.updateById(rule);
        
        return ApiResponse.success(null);
    }
}
