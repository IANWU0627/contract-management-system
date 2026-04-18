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
        String name = ruleData.get("name") != null ? String.valueOf(ruleData.get("name")).trim() : "";
        if (name.isEmpty()) {
            return ApiResponse.error(
                    400,
                    "规则名称不能为空",
                    "error.reminderRule.fieldRequired",
                    Map.of("field", "name"));
        }

        ReminderRule rule = new ReminderRule();
        rule.setName(name);
        
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
            Object minAmountObj = ruleData.get("minAmount");
            if (minAmountObj != null) {
                try {
                    rule.setMinAmount(new BigDecimal(minAmountObj.toString()));
                } catch (NumberFormatException ex) {
                    return ApiResponse.error(
                            400,
                            "最小金额格式错误",
                            "error.reminderRule.fieldInvalid",
                            Map.of("field", "minAmount", "value", String.valueOf(minAmountObj)));
                }
            } else {
                rule.setMinAmount(null);
            }
        }
        if (ruleData.containsKey("maxAmount")) {
            Object maxAmountObj = ruleData.get("maxAmount");
            if (maxAmountObj != null) {
                try {
                    rule.setMaxAmount(new BigDecimal(maxAmountObj.toString()));
                } catch (NumberFormatException ex) {
                    return ApiResponse.error(
                            400,
                            "最大金额格式错误",
                            "error.reminderRule.fieldInvalid",
                            Map.of("field", "maxAmount", "value", String.valueOf(maxAmountObj)));
                }
            } else {
                rule.setMaxAmount(null);
            }
        }
        
        rule.setRemindDays((Integer) ruleData.getOrDefault("remindDays", 30));
        if (rule.getRemindDays() == null || rule.getRemindDays() < 1 || rule.getRemindDays() > 365) {
            return ApiResponse.error(
                    400,
                    "提醒天数超出范围",
                    "error.reminderRule.fieldOutOfRange",
                    Map.of("field", "remindDays", "value", String.valueOf(rule.getRemindDays()), "min", 1, "max", 365));
        }
        if (rule.getMinAmount() != null && rule.getMaxAmount() != null
                && rule.getMinAmount().compareTo(rule.getMaxAmount()) > 0) {
            return ApiResponse.error(
                    400,
                    "金额区间不合法",
                    "error.reminderRule.amountRangeInvalid",
                    Map.of("minValue", rule.getMinAmount().toPlainString(), "maxValue", rule.getMaxAmount().toPlainString()));
        }
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
    public ApiResponse<?> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> ruleData) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在", "error.reminderRule.notFound");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = rule.getIsPublic() != null && rule.getIsPublic();
        
        if (!isPublic && rule.getCreatorId() != null && !rule.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限修改此规则", "error.reminderRule.noEditPermission");
        }
        
        if (ruleData.containsKey("name")) {
            String name = ruleData.get("name") != null ? String.valueOf(ruleData.get("name")).trim() : "";
            if (name.isEmpty()) {
                return ApiResponse.error(
                        400,
                        "规则名称不能为空",
                        "error.reminderRule.fieldRequired",
                        Map.of("field", "name"));
            }
            rule.setName(name);
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
            Object minAmountObj = ruleData.get("minAmount");
            if (minAmountObj != null && !minAmountObj.toString().isEmpty()) {
                try {
                    rule.setMinAmount(new BigDecimal(minAmountObj.toString()));
                } catch (NumberFormatException ex) {
                    return ApiResponse.error(
                            400,
                            "最小金额格式错误",
                            "error.reminderRule.fieldInvalid",
                            Map.of("field", "minAmount", "value", String.valueOf(minAmountObj)));
                }
            } else {
                rule.setMinAmount(null);
            }
        }
        if (ruleData.containsKey("maxAmount")) {
            Object maxAmountObj = ruleData.get("maxAmount");
            if (maxAmountObj != null && !maxAmountObj.toString().isEmpty()) {
                try {
                    rule.setMaxAmount(new BigDecimal(maxAmountObj.toString()));
                } catch (NumberFormatException ex) {
                    return ApiResponse.error(
                            400,
                            "最大金额格式错误",
                            "error.reminderRule.fieldInvalid",
                            Map.of("field", "maxAmount", "value", String.valueOf(maxAmountObj)));
                }
            } else {
                rule.setMaxAmount(null);
            }
        }
        if (ruleData.containsKey("remindDays")) {
            rule.setRemindDays((Integer) ruleData.get("remindDays"));
            if (rule.getRemindDays() == null || rule.getRemindDays() < 1 || rule.getRemindDays() > 365) {
                return ApiResponse.error(
                        400,
                        "提醒天数超出范围",
                        "error.reminderRule.fieldOutOfRange",
                        Map.of("field", "remindDays", "value", String.valueOf(rule.getRemindDays()), "min", 1, "max", 365));
            }
        }
        if (ruleData.containsKey("isEnabled")) {
            rule.setIsEnabled((Boolean) ruleData.get("isEnabled"));
        }
        
        if (rule.getMinAmount() != null && rule.getMaxAmount() != null
                && rule.getMinAmount().compareTo(rule.getMaxAmount()) > 0) {
            return ApiResponse.error(
                    400,
                    "金额区间不合法",
                    "error.reminderRule.amountRangeInvalid",
                    Map.of("minValue", rule.getMinAmount().toPlainString(), "maxValue", rule.getMaxAmount().toPlainString()));
        }

        ruleMapper.updateById(rule);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRule(@PathVariable Long id) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在", "error.reminderRule.notFound");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = rule.getIsPublic() != null && rule.getIsPublic();
        
        if (!isPublic && rule.getCreatorId() != null && !rule.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限删除此规则", "error.reminderRule.noDeletePermission");
        }
        
        ruleMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<?> toggleRule(@PathVariable Long id) {
        ReminderRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return ApiResponse.error("规则不存在", "error.reminderRule.notFound");
        }
        
        rule.setIsEnabled(!rule.getIsEnabled());
        ruleMapper.updateById(rule);
        
        return ApiResponse.success(null);
    }
}
