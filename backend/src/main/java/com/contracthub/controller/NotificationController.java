package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.NotificationMessage;
import com.contracthub.mapper.NotificationMessageMapper;
import com.contracthub.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationMessageMapper notificationMessageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationController(NotificationMessageMapper notificationMessageMapper) {
        this.notificationMessageMapper = notificationMessageMapper;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) Boolean importantOnly) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        QueryWrapper<NotificationMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUserId);
        if (type != null && !type.isBlank()) {
            wrapper.eq("type", type.trim());
        } else if (category != null && !category.isBlank()) {
            List<String> categoryTypes = resolveCategoryTypes(category.trim());
            if (!categoryTypes.isEmpty()) {
                wrapper.in("type", categoryTypes);
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like("title", kw).or().like("content", kw));
        }
        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.eq("is_read", false);
        }
        if (Boolean.TRUE.equals(importantOnly)) {
            wrapper.eq("is_important", true);
        }
        wrapper.orderByDesc("created_at");
        Page<NotificationMessage> resultPage = notificationMessageMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<Map<String, Object>> list = resultPage.getRecords().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("type", item.getType());
            map.put("title", item.getTitle());
            map.put("content", item.getContent());
            map.put("data", parseData(item.getData()));
            map.put("isRead", item.getIsRead());
            map.put("isImportant", item.getIsImportant());
            map.put("createdAt", item.getCreatedAt() == null ? "" : item.getCreatedAt().format(dateTimeFormatter));
            return map;
        }).toList();

        QueryWrapper<NotificationMessage> unreadWrapper = new QueryWrapper<>();
        unreadWrapper.eq("user_id", currentUserId).eq("is_read", false);
        long unreadCount = notificationMessageMapper.selectCount(unreadWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", resultPage.getTotal());
        data.put("page", resultPage.getCurrent());
        data.put("pageSize", resultPage.getSize());
        data.put("unreadCount", unreadCount);
        return ApiResponse.success(data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<String> deleteOne(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        NotificationMessage message = notificationMessageMapper.selectById(id);
        if (message == null) {
            return ApiResponse.error("通知不存在", "error.notification.notFound");
        }
        if (!Objects.equals(message.getUserId(), currentUserId)) {
            return ApiResponse.error("无权限操作该通知", "error.notification.forbidden");
        }
        notificationMessageMapper.deleteById(id);
        return ApiResponse.success("删除成功");
    }

    @DeleteMapping("/my")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> clearMyNotifications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        QueryWrapper<NotificationMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUserId);
        int deleted = notificationMessageMapper.delete(wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("deletedCount", deleted);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<String> markRead(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        NotificationMessage message = notificationMessageMapper.selectById(id);
        if (message == null) {
            return ApiResponse.error("通知不存在", "error.notification.notFound");
        }
        if (!Objects.equals(message.getUserId(), currentUserId)) {
            return ApiResponse.error("无权限操作该通知", "error.notification.forbidden");
        }
        message.setIsRead(true);
        notificationMessageMapper.updateById(message);
        return ApiResponse.success("已标记为已读");
    }

    @PutMapping("/{id}/important")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<String> markImportant(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        NotificationMessage message = notificationMessageMapper.selectById(id);
        if (message == null) {
            return ApiResponse.error("通知不存在", "error.notification.notFound");
        }
        if (!Objects.equals(message.getUserId(), currentUserId)) {
            return ApiResponse.error("无权限操作该通知", "error.notification.forbidden");
        }
        boolean important = payload != null && Boolean.TRUE.equals(payload.get("important"));
        message.setIsImportant(important);
        notificationMessageMapper.updateById(message);
        return ApiResponse.success("重要标记已更新");
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> markAllRead() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("用户未登录", "error.auth.notLogin");
        }
        QueryWrapper<NotificationMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUserId).eq("is_read", false);
        List<NotificationMessage> unreadList = notificationMessageMapper.selectList(wrapper);
        for (NotificationMessage message : unreadList) {
            message.setIsRead(true);
            notificationMessageMapper.updateById(message);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("updatedCount", unreadList.size());
        return ApiResponse.success(data);
    }

    private Object parseData(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, Map.class);
        } catch (Exception e) {
            return raw;
        }
    }

    private List<String> resolveCategoryTypes(String category) {
        return switch (category) {
            case "approval" -> List.of("approval_request", "approval_result", "renewal_request", "renewal_result");
            case "reminder" -> List.of("reminder");
            case "system" -> List.of("contract_comment", "contract_update", "security_alert");
            default -> List.of();
        };
    }
}
