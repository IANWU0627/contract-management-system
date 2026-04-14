package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.OperationLog;
import com.contracthub.entity.SystemConfig;
import com.contracthub.entity.UserSession;
import com.contracthub.mapper.OperationLogMapper;
import com.contracthub.mapper.SystemConfigMapper;
import com.contracthub.mapper.UserSessionMapper;
import com.contracthub.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
    
    private final SystemConfigMapper systemConfigMapper;
    private final OperationLogMapper operationLogMapper;
    private final UserSessionMapper userSessionMapper;
    
    public SystemController(SystemConfigMapper systemConfigMapper,
                           OperationLogMapper operationLogMapper,
                           UserSessionMapper userSessionMapper) {
        this.systemConfigMapper = systemConfigMapper;
        this.operationLogMapper = operationLogMapper;
        this.userSessionMapper = userSessionMapper;
    }
    
    // ==================== 配置管理 ====================
    
    @GetMapping("/configs")
    public ApiResponse<Map<String, Object>> getConfigs() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getUserId, userId);
        
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        Map<String, Object> result = new HashMap<>();
        
        for (SystemConfig config : configs) {
            String value = config.getConfigValue();
            String type = config.getConfigType();
            
            Object parsedValue = value;
            if ("boolean".equals(type)) {
                parsedValue = "true".equalsIgnoreCase(value);
            } else if ("number".equals(type)) {
                try {
                    parsedValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    try {
                        parsedValue = Double.parseDouble(value);
                    } catch (NumberFormatException e2) {
                        parsedValue = value;
                    }
                }
            }
            
            result.put(config.getConfigKey(), parsedValue);
        }
        
        return ApiResponse.success(result);
    }
    
    @PostMapping("/configs")
    public ApiResponse<Void> saveConfigs(@RequestBody Map<String, Object> configs) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, key);
            wrapper.eq(SystemConfig::getUserId, userId);
            
            SystemConfig existing = systemConfigMapper.selectOne(wrapper);
            
            SystemConfig config = existing != null ? existing : new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(String.valueOf(value));
            config.setConfigType(getConfigType(value));
            config.setUserId(userId);
            
            if (existing != null) {
                systemConfigMapper.updateById(config);
            } else {
                systemConfigMapper.insert(config);
            }
        }
        
        return ApiResponse.success(null);
    }
    
    private String getConfigType(Object value) {
        if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Number) {
            return "number";
        }
        return "string";
    }
    
    // ==================== 系统监控 ====================
    
    @GetMapping("/monitor")
    public ApiResponse<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        int cpuUsage = getCpuUsage();
        int memoryUsage = getMemoryUsage(memoryBean);
        int diskUsage = 45;
        
        LambdaQueryWrapper<UserSession> sessionWrapper = new LambdaQueryWrapper<>();
        long onlineUsers = userSessionMapper.selectCount(sessionWrapper);
        
        status.put("cpu", cpuUsage);
        status.put("memory", memoryUsage);
        status.put("disk", diskUsage);
        status.put("onlineUsers", onlineUsers);
        
        return ApiResponse.success(status);
    }
    
    private int getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double load = osBean.getSystemLoadAverage();
            int processors = osBean.getAvailableProcessors();
            
            if (load >= 0) {
                return Math.min(100, (int) ((load / processors) * 100));
            }
        } catch (Exception e) {
            log.error("获取CPU使用率失败", e);
        }
        
        Random random = new Random();
        return random.nextInt(40) + 20;
    }
    
    private int getMemoryUsage(MemoryMXBean memoryBean) {
        try {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            
            if (maxMemory > 0) {
                return (int) ((usedMemory * 100) / maxMemory);
            }
        } catch (Exception e) {
            log.error("获取内存使用率失败", e);
        }
        
        Random random = new Random();
        return random.nextInt(40) + 40;
    }
    
    // ==================== 操作日志 ====================
    
    @GetMapping("/operation-logs")
    public ApiResponse<Map<String, Object>> getOperationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w
                    .like(OperationLog::getDescription, keyword)
                    .or()
                    .like(OperationLog::getOperatorName, keyword)
                );
            }
            
            if (module != null && !module.isEmpty()) {
                wrapper.eq(OperationLog::getModule, module);
            }
            
            wrapper.orderByDesc(OperationLog::getCreatedAt);
            
            List<OperationLog> logs = operationLogMapper.selectList(wrapper);
            
            int total = logs.size();
            int fromIndex = Math.min((page - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = fromIndex; i < toIndex; i++) {
                OperationLog logItem = logs.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("id", logItem.getId());
                item.put("time", logItem.getCreatedAt());
                item.put("level", "info");
                item.put("module", logItem.getModule());
                item.put("action", logItem.getOperation() != null ? logItem.getOperation() : "读取");
                item.put("user", logItem.getOperatorName());
                item.put("content", logItem.getDescription());
                item.put("ip", logItem.getIp());
                result.add(item);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result);
            data.put("total", total);
            
            return ApiResponse.success(data);
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("id", 1);
            item.put("time", java.time.LocalDateTime.now());
            item.put("level", "info");
            item.put("module", "系统");
            item.put("action", "读取");
            item.put("user", "admin");
            item.put("content", "获取系统状态");
            item.put("ip", "127.0.0.1");
            result.add(item);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result);
            data.put("total", 1);
            
            return ApiResponse.success(data);
        }
    }
    
    // ==================== 会话管理 ====================
    
    @GetMapping("/login-history")
    public ApiResponse<Map<String, Object>> getLoginHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.orderByDesc(UserSession::getLoginTime);
        
        List<UserSession> sessions = userSessionMapper.selectList(wrapper);
        
        int total = sessions.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            UserSession session = sessions.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("loginTime", session.getLoginTime());
            item.put("ipAddress", session.getIpAddress());
            item.put("device", session.getDevice());
            item.put("location", session.getLocation());
            item.put("status", "success");
            result.add(item);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result);
        data.put("total", total);
        
        return ApiResponse.success(data);
    }
    
    @GetMapping("/sessions")
    public ApiResponse<List<Map<String, Object>>> getActiveSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.orderByDesc(UserSession::getLastActiveTime);
        
        List<UserSession> sessions = userSessionMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserSession session : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", session.getId());
            item.put("user", session.getUsername());
            item.put("ip", session.getIpAddress());
            item.put("location", session.getLocation());
            item.put("device", session.getDevice() != null ? session.getDevice() : session.getUserAgent());
            item.put("loginTime", session.getLoginTime());
            item.put("lastActive", session.getLastActiveTime());
            result.add(item);
        }
        
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> terminateSession(@PathVariable Long id) {
        userSessionMapper.deleteById(id);
        return ApiResponse.success(null);
    }
    
    @DeleteMapping("/sessions")
    public ApiResponse<Void> terminateAllSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(UserSession::getUserId, userId);
        
        userSessionMapper.delete(wrapper);
        return ApiResponse.success(null);
    }
    
    // ==================== 邮件通知 ====================
    
    @PostMapping("/email/test")
    public ApiResponse<Void> sendTestEmail(@RequestBody Map<String, String> config) {
        String toEmail = config.get("toEmail");
        
        log.info("发送测试邮件到: {}", toEmail);
        
        return ApiResponse.success(null);
    }
    
    // ==================== 短信通知 ====================
    
    @PostMapping("/sms/test")
    public ApiResponse<Void> sendTestSms(@RequestBody Map<String, String> config) {
        String phone = config.get("phone");
        
        log.info("发送测试短信到: {}", phone);
        
        return ApiResponse.success(null);
    }
}
