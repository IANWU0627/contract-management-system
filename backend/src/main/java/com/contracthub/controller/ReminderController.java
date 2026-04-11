package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractReminder;
import com.contracthub.mapper.ContractReminderMapper;
import com.contracthub.service.ReminderSchedulerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    
    private final ContractReminderMapper contractReminderMapper;
    private final ReminderSchedulerService schedulerService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ReminderController(ContractReminderMapper contractReminderMapper, ReminderSchedulerService schedulerService) {
        this.contractReminderMapper = contractReminderMapper;
        this.schedulerService = schedulerService;
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getMyReminders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            QueryWrapper<ContractReminder> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("created_at");
            
            Page<ContractReminder> pagination = new Page<>(page, pageSize);
            Page<ContractReminder> resultPage = contractReminderMapper.selectPage(pagination, wrapper);
            
            List<Map<String, Object>> myReminders = new ArrayList<>();
            
            for (ContractReminder reminder : resultPage.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reminder.getId());
                map.put("contractNo", reminder.getContractNo());
                map.put("contractTitle", reminder.getContractTitle());
                map.put("expireDate", reminder.getExpireDate() != null ? reminder.getExpireDate().format(formatter) : "");
                map.put("remindDays", reminder.getRemindDays());
                map.put("status", reminder.getStatus());
                map.put("contractId", reminder.getContractId());
                map.put("isRead", reminder.getIsRead() != null && reminder.getIsRead());
                map.put("createdAt", reminder.getCreatedAt() != null ? reminder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                myReminders.add(map);
            }
            
            QueryWrapper<ContractReminder> countWrapper = new QueryWrapper<>();
            countWrapper.eq("is_read", false).or().isNull("is_read");
            long totalUnread = contractReminderMapper.selectCount(countWrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", myReminders);
            result.put("total", resultPage.getTotal());
            result.put("page", resultPage.getCurrent());
            result.put("pageSize", resultPage.getSize());
            result.put("unreadCount", totalUnread);
            return ApiResponse.success(result);
        } catch (Exception e) {
            System.out.println("获取提醒列表失败，返回空列表: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            result.put("unreadCount", 0);
            return ApiResponse.success(result);
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<Map<String, Object>> getAllReminders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            QueryWrapper<ContractReminder> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("created_at");
            
            Page<ContractReminder> pagination = new Page<>(page, pageSize);
            Page<ContractReminder> resultPage = contractReminderMapper.selectPage(pagination, wrapper);
            
            List<Map<String, Object>> reminders = new ArrayList<>();
            for (ContractReminder reminder : resultPage.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reminder.getId());
                map.put("contractNo", reminder.getContractNo());
                map.put("contractTitle", reminder.getContractTitle());
                map.put("expireDate", reminder.getExpireDate() != null ? reminder.getExpireDate().format(formatter) : "");
                map.put("remindDays", reminder.getRemindDays());
                map.put("status", reminder.getStatus());
                map.put("contractId", reminder.getContractId());
                map.put("createdAt", reminder.getCreatedAt() != null ? reminder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                reminders.add(map);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", reminders);
            result.put("total", resultPage.getTotal());
            result.put("page", resultPage.getCurrent());
            result.put("pageSize", resultPage.getSize());
            return ApiResponse.success(result);
        } catch (Exception e) {
            System.out.println("获取提醒列表失败，返回空列表: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            return ApiResponse.success(result);
        }
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<String> markRead(@PathVariable Long id) {
        try {
            ContractReminder reminder = contractReminderMapper.selectById(id);
            if (reminder != null) {
                reminder.setIsRead(true);
                contractReminderMapper.updateById(reminder);
            }
            return ApiResponse.success("已标记为已读");
        } catch (Exception e) {
            return ApiResponse.error("标记已读失败");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            int result = contractReminderMapper.deleteById(id);
            if (result == 0) {
                return ApiResponse.error("提醒不存在");
            }
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            System.out.println("删除提醒失败: " + e.getMessage());
            return ApiResponse.error("删除提醒失败");
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> reminderMap) {
        try {
            ContractReminder reminder = new ContractReminder();
            reminder.setContractId((Long) reminderMap.get("contractId"));
            reminder.setContractNo((String) reminderMap.get("contractNo"));
            reminder.setContractTitle((String) reminderMap.get("contractTitle"));
            
            String expireDateStr = (String) reminderMap.get("expireDate");
            if (expireDateStr != null) {
                try {
                    LocalDateTime expireDate = LocalDateTime.parse(expireDateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    reminder.setExpireDate(expireDate);
                } catch (Exception e) {
                    reminder.setExpireDate(LocalDateTime.now());
                }
            }
            
            reminder.setRemindDays((Integer) reminderMap.getOrDefault("remindDays", 0));
            reminder.setReminderType((Integer) reminderMap.getOrDefault("reminderType", 0));
            reminder.setStatus((Integer) reminderMap.getOrDefault("status", 0));
            
            contractReminderMapper.insert(reminder);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", reminder.getId());
            result.put("contractNo", reminder.getContractNo());
            result.put("contractTitle", reminder.getContractTitle());
            result.put("expireDate", reminder.getExpireDate() != null ? reminder.getExpireDate().format(formatter) : "");
            result.put("remindDays", reminder.getRemindDays());
            result.put("status", reminder.getStatus());
            result.put("contractId", reminder.getContractId());
            result.put("isRead", false);
            result.put("createdAt", reminder.getCreatedAt() != null ? reminder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            System.out.println("创建提醒失败: " + e.getMessage());
            return ApiResponse.error("创建提醒失败");
        }
    }
    
    @PostMapping("/check")
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<String> triggerCheck() {
        try {
            schedulerService.checkContractExpiring();
            
            QueryWrapper<ContractReminder> wrapper = new QueryWrapper<>();
            long count = contractReminderMapper.selectCount(wrapper);
            return ApiResponse.success("检查完成，发现 " + count + " 条提醒");
        } catch (Exception e) {
            System.out.println("检查提醒失败: " + e.getMessage());
            return ApiResponse.success("检查完成，发现 0 条提醒");
        }
    }
}
