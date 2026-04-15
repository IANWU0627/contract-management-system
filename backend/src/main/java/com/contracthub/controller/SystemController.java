package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.SystemConfig;
import com.contracthub.entity.UserSession;
import com.contracthub.mapper.SystemConfigMapper;
import com.contracthub.mapper.UserSessionMapper;
import com.contracthub.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Properties;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
    
    private final SystemConfigMapper systemConfigMapper;
    private final UserSessionMapper userSessionMapper;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    
    public SystemController(SystemConfigMapper systemConfigMapper,
                           UserSessionMapper userSessionMapper,
                           RestTemplate restTemplate,
                           JdbcTemplate jdbcTemplate) {
        this.systemConfigMapper = systemConfigMapper;
        this.userSessionMapper = userSessionMapper;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
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
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        int cpuUsage = getCpuUsage();
        int memoryUsage = getMemoryUsage(memoryBean);
        int diskUsage = getDiskUsage();
        
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

    private int getDiskUsage() {
        try {
            File root = new File(".");
            long total = root.getTotalSpace();
            long usable = root.getUsableSpace();
            if (total > 0) {
                return (int) (((total - usable) * 100) / total);
            }
        } catch (Exception e) {
            log.error("获取磁盘使用率失败", e);
        }
        return 0;
    }
    
    // ==================== 操作日志 ====================
    
    @GetMapping("/operation-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getOperationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            int safePage = Math.max(page, 1);
            int safePageSize = Math.max(1, Math.min(pageSize, 100));
            Set<String> columns = getOperationLogColumns();
            String operationColumn = pickColumn(columns, "operation", "action");
            String userColumn = pickColumn(columns, "operator_name", "username", "user_name");
            String descriptionColumn = pickColumn(columns, "description", "content");
            String moduleColumn = pickColumn(columns, "module");
            String ipColumn = pickColumn(columns, "ip", "ip_address");
            String timeColumn = pickColumn(columns, "created_at", "create_time");

            List<Object> whereParams = new ArrayList<>();
            StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");

            if (keyword != null && !keyword.isBlank()) {
                whereSql.append(" AND (");
                boolean hasCondition = false;
                if (descriptionColumn != null) {
                    whereSql.append(descriptionColumn).append(" LIKE ? ");
                    whereParams.add("%" + keyword.trim() + "%");
                    hasCondition = true;
                }
                if (userColumn != null) {
                    if (hasCondition) {
                        whereSql.append(" OR ");
                    }
                    whereSql.append(userColumn).append(" LIKE ? ");
                    whereParams.add("%" + keyword.trim() + "%");
                    hasCondition = true;
                }
                if (!hasCondition) {
                    whereSql.append(" 1=0 ");
                }
                whereSql.append(") ");
            }

            if (module != null && !module.isBlank() && moduleColumn != null) {
                whereSql.append(" AND ").append(moduleColumn).append(" = ? ");
                whereParams.add(module.trim());
            }

            String normalizedLevel = normalizeLevel(level);
            String levelCaseExpr = buildLevelCaseExpression(operationColumn, descriptionColumn);
            if (normalizedLevel != null) {
                whereSql.append(" AND ").append(levelCaseExpr).append(" = ? ");
                whereParams.add(normalizedLevel);
            }

            String countSql = "SELECT COUNT(1) FROM operation_log" + whereSql;
            Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, whereParams.toArray());
            int total = totalCount == null ? 0 : totalCount;

            String orderBySql = timeColumn != null ? (" ORDER BY " + timeColumn + " DESC ") : " ORDER BY id DESC ";
            String querySql = "SELECT * FROM operation_log" + whereSql + orderBySql + " LIMIT ? OFFSET ? ";
            List<Object> queryParams = new ArrayList<>(whereParams);
            queryParams.add(safePageSize);
            queryParams.add((safePage - 1) * safePageSize);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql, queryParams.toArray());

            List<Map<String, Object>> normalizedRows = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String action = readAsString(row, operationColumn);
                String userName = readAsString(row, userColumn);
                String content = readAsString(row, descriptionColumn);
                String ipValue = readAsString(row, ipColumn);
                String moduleValue = readAsString(row, moduleColumn);
                Object timeValue = readObject(row, timeColumn);
                String resolvedLevel = resolveLogLevel(action, content);
                Map<String, Object> item = new HashMap<>();
                item.put("id", row.get("id"));
                item.put("time", timeValue);
                item.put("level", resolvedLevel);
                item.put("module", moduleValue);
                item.put("action", action != null ? action : "读取");
                item.put("user", userName);
                item.put("content", content);
                item.put("ip", ipValue);
                normalizedRows.add(item);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", normalizedRows);
            data.put("total", total);
            
            return ApiResponse.success(data);
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            return ApiResponse.error("获取操作日志失败");
        }
    }

    private String resolveLogLevel(String operation, String description) {
        operation = Optional.ofNullable(operation).orElse("").toLowerCase();
        description = Optional.ofNullable(description).orElse("").toLowerCase();
        String text = operation + " " + description;
        if (text.contains("fail") || text.contains("error") || text.contains("失败") || text.contains("异常")) {
            return "error";
        }
        if (text.contains("warn") || text.contains("warning") || text.contains("拒绝") || text.contains("过期")) {
            return "warning";
        }
        return "info";
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        String normalized = level.trim().toLowerCase();
        if (!Set.of("info", "warning", "error").contains(normalized)) {
            return null;
        }
        return normalized;
    }

    private String buildLevelCaseExpression(String operationColumn, String descriptionColumn) {
        String operationExpr = operationColumn != null
                ? "LOWER(COALESCE(CAST(" + operationColumn + " AS CHAR), ''))"
                : "''";
        String descriptionExpr = descriptionColumn != null
                ? "LOWER(COALESCE(CAST(" + descriptionColumn + " AS CHAR), ''))"
                : "''";
        String textExpr = "CONCAT(" + operationExpr + ", ' ', " + descriptionExpr + ")";
        return "CASE " +
                "WHEN (" + textExpr + " LIKE '%fail%' OR " + textExpr + " LIKE '%error%' OR " + textExpr + " LIKE '%失败%' OR " + textExpr + " LIKE '%异常%') THEN 'error' " +
                "WHEN (" + textExpr + " LIKE '%warn%' OR " + textExpr + " LIKE '%warning%' OR " + textExpr + " LIKE '%拒绝%' OR " + textExpr + " LIKE '%过期%') THEN 'warning' " +
                "ELSE 'info' END";
    }

    private Set<String> getOperationLogColumns() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW COLUMNS FROM operation_log");
        Set<String> columns = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Object field = row.get("Field");
            if (field != null) {
                columns.add(field.toString().toLowerCase());
            }
        }
        return columns;
    }

    private String pickColumn(Set<String> columns, String... candidates) {
        for (String candidate : candidates) {
            if (columns.contains(candidate.toLowerCase())) {
                return candidate;
            }
        }
        return null;
    }

    private String readAsString(Map<String, Object> row, String key) {
        Object value = readObject(row, key);
        return value != null ? String.valueOf(value) : null;
    }

    private Object readObject(Map<String, Object> row, String key) {
        if (key == null) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
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
    public ApiResponse<List<Map<String, Object>>> getActiveSessions(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String authHeader = request.getHeader("Authorization");
        String currentToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            currentToken = authHeader.substring(7);
        }
        
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
            item.put("browserVersion", extractBrowserVersion(session.getUserAgent()));
            item.put("current", currentToken != null && currentToken.equals(session.getToken()));
            result.add(item);
        }
        
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> terminateSession(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getId, id);
        wrapper.eq(UserSession::getUserId, userId);

        UserSession session = userSessionMapper.selectOne(wrapper);
        if (session == null) {
            return ApiResponse.error("会话不存在或无权限操作");
        }
        String authHeader = request.getHeader("Authorization");
        String currentToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            currentToken = authHeader.substring(7);
        }
        if (currentToken != null && currentToken.equals(session.getToken())) {
            return ApiResponse.error("当前会话不能被终止");
        }

        userSessionMapper.deleteById(id);
        return ApiResponse.success(null);
    }
    
    @DeleteMapping("/sessions")
    public ApiResponse<Void> terminateAllSessions(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String authHeader = request.getHeader("Authorization");
        String currentToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            currentToken = authHeader.substring(7);
        }
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        if (currentToken != null && !currentToken.isEmpty()) {
            wrapper.ne(UserSession::getToken, currentToken);
        }
        
        userSessionMapper.delete(wrapper);
        return ApiResponse.success(null);
    }

    private String extractBrowserVersion(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "未知";
        }
        String[] tokens = {"Edg/", "Chrome/", "Firefox/", "Version/", "Safari/"};
        for (String token : tokens) {
            int start = userAgent.indexOf(token);
            if (start >= 0) {
                int versionStart = start + token.length();
                int versionEnd = versionStart;
                while (versionEnd < userAgent.length()) {
                    char c = userAgent.charAt(versionEnd);
                    if (!(Character.isDigit(c) || c == '.')) {
                        break;
                    }
                    versionEnd++;
                }
                if (versionEnd > versionStart) {
                    return token.replace("/", "") + " " + userAgent.substring(versionStart, versionEnd);
                }
            }
        }
        return "未知";
    }
    
    // ==================== 邮件通知 ====================
    
    @PostMapping("/email/test")
    public ApiResponse<Void> sendTestEmail(@RequestBody Map<String, String> config) {
        String toEmail = config.get("toEmail");
        if (toEmail == null || toEmail.isBlank()) {
            return ApiResponse.error("测试邮箱不能为空");
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Map<String, String> userConfigs = getUserConfigMap(userId);
            String host = userConfigs.get("email_smtp_host");
            String from = userConfigs.get("email_smtp_from");
            String username = userConfigs.get("email_smtp_username");
            String password = userConfigs.get("email_smtp_password");
            boolean useSsl = Boolean.parseBoolean(userConfigs.getOrDefault("email_smtp_use_ssl", "true"));
            int port = parseInt(userConfigs.get("email_smtp_port"), useSsl ? 465 : 25);

            if (isBlank(host) || isBlank(from) || isBlank(username) || isBlank(password)) {
                return ApiResponse.error("请先在系统设置中完整配置SMTP参数后再测试");
            }

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            mailSender.setDefaultEncoding("UTF-8");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", String.valueOf(useSsl));
            props.put("mail.smtp.starttls.enable", String.valueOf(!useSsl));
            props.put("mail.debug", "false");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("合同管理系统 - 测试邮件");
            message.setText("这是一封测试邮件，说明当前SMTP配置可用。");
            mailSender.send(message);

            log.info("测试邮件发送成功: {}", toEmail);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("发送测试邮件失败", e);
            return ApiResponse.error("测试邮件发送失败: " + e.getMessage());
        }
    }
    
    // ==================== 短信通知 ====================
    
    @PostMapping("/sms/test")
    public ApiResponse<Void> sendTestSms(@RequestBody Map<String, String> config) {
        String phone = config.get("phone");
        if (phone == null || phone.isBlank()) {
            return ApiResponse.error("测试手机号不能为空");
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Map<String, String> userConfigs = getUserConfigMap(userId);

            String provider = userConfigs.getOrDefault("sms_provider", "");
            String apiUrl = userConfigs.getOrDefault("sms_api_url", "");
            String accessKey = userConfigs.getOrDefault("sms_access_key", "");
            String accessSecret = userConfigs.getOrDefault("sms_access_secret", "");
            String signName = userConfigs.getOrDefault("sms_sign_name", "");
            String templateCode = userConfigs.getOrDefault("sms_template_code", "");

            if (isBlank(provider)) {
                return ApiResponse.error("请先配置短信服务商");
            }
            if (isBlank(apiUrl)) {
                return ApiResponse.error("请先配置短信网关API地址（sms_api_url）");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("phone", phone);
            payload.put("signName", signName);
            payload.put("templateCode", templateCode);
            payload.put("accessKey", accessKey);
            payload.put("accessSecret", accessSecret);
            payload.put("message", "【合同管理系统】测试短信，当前短信配置可用。");
            payload.put("provider", provider);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(Objects.requireNonNull(apiUrl), request, String.class);

            log.info("测试短信请求已发送: phone={}, provider={}", phone, provider);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("发送测试短信失败", e);
            return ApiResponse.error("测试短信发送失败: " + e.getMessage());
        }
    }

    private Map<String, String> getUserConfigMap(Long userId) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getUserId, userId);
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        Map<String, String> result = new HashMap<>();
        for (SystemConfig item : configs) {
            result.put(item.getConfigKey(), item.getConfigValue());
        }
        return result;
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
