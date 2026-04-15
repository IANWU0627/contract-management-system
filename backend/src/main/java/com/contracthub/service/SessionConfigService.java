package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.entity.SystemConfig;
import com.contracthub.mapper.SystemConfigMapper;
import com.contracthub.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class SessionConfigService {
    
    private final SystemConfigMapper systemConfigMapper;
    
    public SessionConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }
    
    public Map<String, String> getAllConfig() {
        Long userId = SecurityUtils.getCurrentUserId();
        return getAllConfig(userId);
    }

    public Map<String, String> getAllConfig(Long userId) {
        Map<String, String> config = new HashMap<>();

        try {
            if (userId != null) {
                LambdaQueryWrapper<SystemConfig> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(SystemConfig::getUserId, userId);
                for (SystemConfig item : systemConfigMapper.selectList(userWrapper)) {
                    config.put(item.getConfigKey(), item.getConfigValue());
                }
            }

            // Fallback to global defaults for missing keys.
            LambdaQueryWrapper<SystemConfig> globalWrapper = new LambdaQueryWrapper<>();
            globalWrapper.isNull(SystemConfig::getUserId);
            for (SystemConfig item : systemConfigMapper.selectList(globalWrapper)) {
                config.putIfAbsent(item.getConfigKey(), item.getConfigValue());
            }
        } catch (Exception e) {
            System.err.println("获取配置失败: " + e.getMessage());
        }
        
        return config;
    }
    
    public int getTokenExpiry() {
        return getTokenExpiry(SecurityUtils.getCurrentUserId());
    }

    public int getTokenExpiry(Long userId) {
        String value = getAllConfig(userId).get("session_token_expiry");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 24; // 默认24小时
    }
    
    public int getRefreshTokenExpiry() {
        return getRefreshTokenExpiry(SecurityUtils.getCurrentUserId());
    }

    public int getRefreshTokenExpiry(Long userId) {
        String value = getAllConfig(userId).get("session_refresh_token_expiry");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 168; // 默认168小时(7天)
    }
    
    public boolean isSingleSession() {
        String value = getAllConfig().get("session_single_session");
        return "true".equals(value) || Boolean.TRUE.toString().equals(value);
    }

    public boolean isSingleSession(Long userId) {
        String value = getAllConfig(userId).get("session_single_session");
        return "true".equals(value) || Boolean.TRUE.toString().equals(value);
    }
    
    public int getSessionTimeout() {
        return getSessionTimeout(SecurityUtils.getCurrentUserId());
    }

    public int getSessionTimeout(Long userId) {
        String value = getAllConfig(userId).get("session_timeout");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 30; // 默认30分钟
    }
}
