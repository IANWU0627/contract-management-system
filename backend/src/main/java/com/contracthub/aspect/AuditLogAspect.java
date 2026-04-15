package com.contracthub.aspect;

import com.contracthub.entity.OperationLog;
import com.contracthub.mapper.OperationLogMapper;
import com.contracthub.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private final OperationLogMapper operationLogMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "confirmpassword",
            "token", "authorization", "accesstoken", "refreshtoken",
            "secret", "apikey", "api_key", "credential"
    );

    public AuditLogAspect(OperationLogMapper operationLogMapper, JdbcTemplate jdbcTemplate) {
        this.operationLogMapper = operationLogMapper;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Pointcut("execution(* com.contracthub.controller..*.*(..))")
    public void controllerMethods() {}
    
    @Around("controllerMethods() && !execution(* com.contracthub.controller..*.*me(..))")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();
        
        String module = extractModule(className);
        String action = extractAction(methodName);
        String description = buildDescription(className, methodName, joinPoint.getArgs());
        String detail = buildDetail(className, methodName, joinPoint.getArgs());
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUserName();
        String ip = SecurityUtils.getClientIp();
        
        Object result = null;
        String finalDescription = description;
        try {
            result = joinPoint.proceed();
            finalDescription = description + " - 操作成功";
        } catch (Exception e) {
            finalDescription = description + " - 操作失败";
            throw e;
        }
        
        final String finalDesc = finalDescription;
        final Long finalUserId = userId;
        final String finalUsername = username;
        final String finalIp = ip;
        final String finalModule = module;
        final String finalAction = action;
        final String finalDetail = detail;
        
        saveLogAsync(finalModule, finalAction, finalDesc, finalDetail, finalUserId, finalUsername, finalIp);
        
        return result;
    }
    
    @Async
    public void saveLogAsync(String module, String action, String description, String detail, Long userId, String username, String ip) {
        try {
            OperationLog log = new OperationLog();
            log.setModule(module);
            log.setOperation(action);
            log.setDescription(description);
            log.setDetail(detail);
            log.setOperatorId(userId);
            log.setOperatorName(username);
            log.setIp(ip);
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 兼容历史库结构（action/content/username/create_time），避免日志表结构差异导致“空日志”
            log.warn("标准操作日志写入失败，尝试兼容写入: {}", e.getMessage());
            try {
                insertCompatibleOperationLog(module, action, description, detail, userId, username, ip);
            } catch (Exception compatibleEx) {
                log.error("兼容写入操作日志失败", compatibleEx);
            }
        }
    }

    private void insertCompatibleOperationLog(String module, String action, String description, String detail,
                                              Long userId, String username, String ip) {
        Set<String> columns = getOperationLogColumns();

        String moduleColumn = pickColumn(columns, "module");
        String actionColumn = pickColumn(columns, "operation", "action");
        String descriptionColumn = pickColumn(columns, "description", "content");
        String detailColumn = pickColumn(columns, "detail");
        String operatorIdColumn = pickColumn(columns, "operator_id", "user_id");
        String operatorNameColumn = pickColumn(columns, "operator_name", "username", "user_name");
        String ipColumn = pickColumn(columns, "ip", "ip_address");
        String createdAtColumn = pickColumn(columns, "created_at", "create_time");

        Map<String, Object> fieldValues = new LinkedHashMap<>();
        putIfColumnExists(fieldValues, moduleColumn, module);
        putIfColumnExists(fieldValues, actionColumn, action);
        putIfColumnExists(fieldValues, descriptionColumn, description);
        putIfColumnExists(fieldValues, detailColumn, detail);
        putIfColumnExists(fieldValues, operatorIdColumn, userId);
        putIfColumnExists(fieldValues, operatorNameColumn, username);
        putIfColumnExists(fieldValues, ipColumn, ip);
        putIfColumnExists(fieldValues, createdAtColumn, LocalDateTime.now());

        if (fieldValues.isEmpty()) {
            throw new IllegalStateException("operation_log 无可用写入列");
        }

        List<String> insertColumns = new ArrayList<>(fieldValues.keySet());
        List<Object> values = new ArrayList<>(fieldValues.values());
        String placeholders = String.join(", ", insertColumns.stream().map(col -> "?").toList());
        String sql = "INSERT INTO operation_log (" + String.join(", ", insertColumns) + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(sql, values.toArray());
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

    private void putIfColumnExists(Map<String, Object> target, String column, Object value) {
        if (column != null) {
            target.put(column, value);
        }
    }
    
    private String extractModule(String className) {
        if (className.contains("Contract")) {
            return "合同管理";
        } else if (className.contains("User")) {
            return "用户管理";
        } else if (className.contains("Template")) {
            return "模板管理";
        } else if (className.contains("Reminder")) {
            return "提醒管理";
        } else if (className.contains("Statistics")) {
            return "统计报表";
        } else if (className.contains("Auth")) {
            return "认证授权";
        } else if (className.contains("Favorite")) {
            return "收藏管理";
        } else if (className.contains("Tag")) {
            return "标签管理";
        } else if (className.contains("Renewal")) {
            return "续约管理";
        }
        return "其他";
    }
    
    private String extractAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.equals("register")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("put")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("list") || methodName.startsWith("find") || methodName.startsWith("select")) {
            return "READ";
        } else if (methodName.startsWith("login") || methodName.startsWith("logout")) {
            return "LOGIN";
        } else if (methodName.startsWith("upload")) {
            return "UPLOAD";
        } else if (methodName.startsWith("download") || methodName.startsWith("export") || methodName.equals("exportContractsExcel") || methodName.equals("downloadContractWord")) {
            return "DOWNLOAD";
        } else if (methodName.contains("approve") || methodName.contains("Approve")) {
            return "APPROVE";
        } else if (methodName.contains("reject") || methodName.contains("Reject")) {
            return "REJECT";
        } else if (methodName.contains("sign") || methodName.contains("Sign")) {
            return "SIGN";
        } else if (methodName.contains("archive") || methodName.contains("Archive")) {
            return "ARCHIVE";
        } else if (methodName.contains("submit") || methodName.contains("Submit")) {
            return "SUBMIT";
        } else if (methodName.contains("star") || methodName.contains("favorite") || methodName.contains("Favorite")) {
            return "FAVORITE";
        } else if (methodName.contains("renewal") || methodName.contains("Renewal")) {
            return "RENEWAL";
        } else if (methodName.contains("copy") || methodName.contains("Copy")) {
            return "COPY";
        } else if (methodName.contains("batch") || methodName.contains("Batch")) {
            return "BATCH";
        } else if (methodName.contains("terminate") || methodName.contains("Terminate")) {
            return "TERMINATE";
        } else if (methodName.contains("analyze") || methodName.contains("Analyze")) {
            return "ANALYZE";
        }
        return "OTHER";
    }
    
    private String buildDescription(String className, String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(".").append(methodName);
        
        if (args != null && args.length > 0) {
            sb.append("(");
            for (int i = 0; i < Math.min(args.length, 3); i++) {
                if (i > 0) sb.append(", ");
                Object arg = args[i];
                if (arg != null) {
                    String argStr = arg.toString();
                    if (argStr.length() > 30) {
                        argStr = argStr.substring(0, 30) + "...";
                    }
                    sb.append(argStr);
                } else {
                    sb.append("null");
                }
            }
            if (args.length > 3) {
                sb.append("...");
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    private String buildDetail(String className, String methodName, Object[] args) {
        try {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("class", className);
            detailMap.put("method", methodName);
            detailMap.put("timestamp", LocalDateTime.now().toString());
            
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg != null) {
                        String argClass = arg.getClass().getSimpleName();
                        if (argClass.contains("Map")) {
                            detailMap.put("requestBody", sanitizeArgument(arg));
                        } else if (argClass.equals("Long") || argClass.equals("Integer") || argClass.equals("String")) {
                            if (methodName.contains("Id") || methodName.contains("id")) {
                                detailMap.put("targetId", arg);
                            }
                        }
                    }
                }
            }
            
            return objectMapper.writeValueAsString(detailMap);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Object sanitizeArgument(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey());
                Object entryValue = entry.getValue();
                if (isSensitiveKey(key)) {
                    sanitized.put(key, "***");
                } else {
                    sanitized.put(key, sanitizeArgument(entryValue));
                }
            }
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> sanitized = new ArrayList<>();
            for (Object item : collection) {
                sanitized.add(sanitizeArgument(item));
            }
            return sanitized;
        }
        String text = String.valueOf(value);
        if (text.length() > 500) {
            return text.substring(0, 500) + "...";
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase().replace("_", "");
        return SENSITIVE_KEYS.contains(normalized);
    }
}
