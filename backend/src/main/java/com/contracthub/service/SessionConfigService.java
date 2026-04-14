package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.entity.SystemConfig;
import com.contracthub.mapper.SystemConfigMapper;
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
        Map<String, String> config = new HashMap<>();
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SystemConfig::getUserId);
        
        try {
            for (SystemConfig item : systemConfigMapper.selectList(wrapper)) {
                config.put(item.getConfigKey(), item.getConfigValue());
            }
        } catch (Exception e) {
            System.err.println("获取配置失败: " + e.getMessage());
        }
        
        return config;
    }
    
    public int getTokenExpiry() {
        String value = getAllConfig().get("session_token_expiry");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 24; // 默认24小时
    }
    
    public int getRefreshTokenExpiry() {
        String value = getAllConfig().get("session_refresh_token_expiry");
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
    
    public int getSessionTimeout() {
        String value = getAllConfig().get("session_timeout");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 30; // 默认30分钟
    }
}
