package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractReminder;
import com.contracthub.mapper.ContractReminderMapper;
import com.contracthub.service.ReminderSchedulerService;
import com.contracthub.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private static final Logger log = LoggerFactory.getLogger(ReminderController.class);
    
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
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error("用户未登录", "error.auth.notLogin");
            }
            String normalizedKeyword = keyword != null ? keyword.trim() : null;
            if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
                normalizedKeyword = null;
            }
            Integer statusValue = null;
            if (status != null && !status.isBlank()) {
                statusValue = Integer.parseInt(status);
            }
            int safePage = Math.max(page, 1);
            int safePageSize = Math.max(pageSize, 1);
            int offset = (safePage - 1) * safePageSize;

            List<ContractReminder> pageRecords = contractReminderMapper.selectMyDeduplicatedPage(
                    currentUserId,
                    normalizedKeyword,
                    statusValue,
                    safePageSize,
                    offset
            );
            long total = contractReminderMapper.countMyDeduplicated(currentUserId, normalizedKeyword, statusValue);

            List<Map<String, Object>> myReminders = new ArrayList<>();

            for (ContractReminder reminder : pageRecords) {
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
            
            long totalUnread = contractReminderMapper.countMyUnreadDeduplicated(currentUserId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", myReminders);
            result.put("total", total);
            result.put("page", safePage);
            result.put("pageSize", safePageSize);
            result.put("unreadCount", totalUnread);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取我的提醒列表失败", e);
            return ApiResponse.error("获取提醒列表失败", "error.reminder.listFailed");
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<Map<String, Object>> getAllReminders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            int safePage = Math.max(page, 1);
            int safePageSize = Math.max(pageSize, 1);
            int offset = (safePage - 1) * safePageSize;
            List<ContractReminder> pageRecords = contractReminderMapper.selectAllDeduplicatedPage(safePageSize, offset);
            long total = contractReminderMapper.countAllDeduplicated();

            List<Map<String, Object>> reminders = new ArrayList<>();
            for (ContractReminder reminder : pageRecords) {
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
            result.put("total", total);
            result.put("page", safePage);
            result.put("pageSize", safePageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取提醒列表失败", e);
            return ApiResponse.error("获取提醒列表失败", "error.reminder.listFailed");
        }
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<String> markRead(@PathVariable Long id) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error("用户未登录", "error.auth.notLogin");
            }
            ContractReminder reminder = contractReminderMapper.selectById(id);
            if (reminder == null) {
                return ApiResponse.error("提醒不存在", "error.reminder.notFound");
            }
            if (reminder.getRecipientUserId() != null && !reminder.getRecipientUserId().equals(currentUserId)) {
                return ApiResponse.error("无权限操作该提醒", "error.reminder.forbidden");
            }
            reminder.setIsRead(true);
            reminder.setStatus(1);
            contractReminderMapper.updateById(reminder);
            return ApiResponse.success("已标记为已读");
        } catch (Exception e) {
            log.error("标记提醒已读失败: id={}", id, e);
            return ApiResponse.error("标记已读失败", "error.reminder.markReadFailed");
        }
    }

    @PutMapping("/read-batch")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> markBatchRead(@RequestBody Map<String, Object> request) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error("用户未登录", "error.auth.notLogin");
            }
            List<Object> idsObj = (List<Object>) request.get("ids");
            if (idsObj == null || idsObj.isEmpty()) {
                return ApiResponse.error("请选择需要标记的提醒", "error.reminder.idsRequired");
            }
            List<Long> ids = idsObj.stream()
                    .filter(Number.class::isInstance)
                    .map(id -> ((Number) id).longValue())
                    .toList();
            int successCount = 0;
            for (Long id : ids) {
                ContractReminder reminder = contractReminderMapper.selectById(id);
                if (reminder == null) {
                    continue;
                }
                if (reminder.getRecipientUserId() != null && !reminder.getRecipientUserId().equals(currentUserId)) {
                    continue;
                }
                reminder.setIsRead(true);
                reminder.setStatus(1);
                contractReminderMapper.updateById(reminder);
                successCount++;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("totalCount", ids.size());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("批量标记已读失败", e);
            return ApiResponse.error("批量标记已读失败", "error.reminder.batchMarkReadFailed");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            int result = contractReminderMapper.deleteById(id);
            if (result == 0) {
                return ApiResponse.error("提醒不存在", "error.reminder.notFound");
            }
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            log.warn("删除提醒失败: {}", e.getMessage());
            return ApiResponse.error("删除提醒失败", "error.reminder.deleteFailed");
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('REMINDER_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> reminderMap) {
        try {
            ContractReminder reminder = new ContractReminder();
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (reminderMap.get("recipientUserId") instanceof Number recipientIdObj) {
                reminder.setRecipientUserId(recipientIdObj.longValue());
            } else {
                reminder.setRecipientUserId(currentUserId);
            }
            if (reminderMap.get("contractId") instanceof Number contractIdObj) {
                reminder.setContractId(contractIdObj.longValue());
            } else {
                return ApiResponse.error("合同ID不能为空", "error.reminder.contractIdRequired");
            }
            reminder.setContractNo((String) reminderMap.get("contractNo"));
            reminder.setContractTitle((String) reminderMap.get("contractTitle"));
            
            String expireDateStr = (String) reminderMap.get("expireDate");
            if (expireDateStr == null || expireDateStr.isBlank()) {
                return ApiResponse.error("到期日期不能为空", "error.reminder.expireDateRequired");
            }
            LocalDateTime expireDate = LocalDateTime.parse(expireDateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            reminder.setExpireDate(expireDate);
            
            reminder.setRemindDays(toInt(reminderMap.getOrDefault("remindDays", 0), 0));
            reminder.setReminderType(toInt(reminderMap.getOrDefault("reminderType", 0), 0));
            reminder.setStatus(toInt(reminderMap.getOrDefault("status", 0), 0));

            // 幂等保护：同合同+同接收人存在未处理提醒时复用已有记录，避免重复插入
            QueryWrapper<ContractReminder> existingWrapper = new QueryWrapper<>();
            existingWrapper.eq("contract_id", reminder.getContractId())
                    .eq("recipient_user_id", reminder.getRecipientUserId())
                    .and(w -> w.eq("is_read", false).or().isNull("is_read"))
                    .orderByDesc("id")
                    .last("LIMIT 1");
            List<ContractReminder> existingList = contractReminderMapper.selectList(existingWrapper);
            if (!existingList.isEmpty()) {
                Map<String, Object> result = buildReminderResponse(existingList.get(0));
                result.put("existed", true);
                return ApiResponse.success(result);
            }

            contractReminderMapper.insert(reminder);

            Map<String, Object> result = buildReminderResponse(reminder);
            result.put("existed", false);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("创建提醒失败", e);
            return ApiResponse.error("创建提醒失败", "error.reminder.createFailed");
        }
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    private Map<String, Object> buildReminderResponse(ContractReminder reminder) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", reminder.getId());
        result.put("contractNo", reminder.getContractNo());
        result.put("contractTitle", reminder.getContractTitle());
        result.put("expireDate", reminder.getExpireDate() != null ? reminder.getExpireDate().format(formatter) : "");
        result.put("remindDays", reminder.getRemindDays());
        result.put("status", reminder.getStatus());
        result.put("contractId", reminder.getContractId());
        result.put("isRead", reminder.getIsRead() != null && reminder.getIsRead());
        result.put("createdAt", reminder.getCreatedAt() != null ? reminder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        return result;
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
            log.error("触发提醒检查失败", e);
            return ApiResponse.error("检查提醒失败", "error.reminder.checkFailed");
        }
    }
}
