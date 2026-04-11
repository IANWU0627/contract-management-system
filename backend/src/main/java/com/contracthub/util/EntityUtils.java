package com.contracthub.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EntityUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static Map<String, Object> toMap(Object entity) {
        if (entity == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();
        
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                String propertyName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
                
                // 排除这些属性
                if (shouldExclude(propertyName)) {
                    continue;
                }
                
                try {
                    Object value = method.invoke(entity);
                    map.put(propertyName, formatValue(value));
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        
        return map;
    }
    
    private static boolean shouldExclude(String propertyName) {
        return "class".equals(propertyName) || 
               "password".equals(propertyName) ||
               propertyName.startsWith("$");
    }
    
    private static Object formatValue(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(dateFormatter);
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(dateTimeFormatter);
        }
        
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        
        if (value instanceof Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }
        
        if (value instanceof Enum) {
            return value.toString();
        }
        
        return value;
    }
    
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static List<Map<String, Object>> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
