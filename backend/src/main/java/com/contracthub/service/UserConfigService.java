package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.entity.SystemConfig;
import com.contracthub.mapper.SystemConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserConfigService {

    private final SystemConfigMapper systemConfigMapper;

    public UserConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    public Map<String, String> getUserConfigValues(Long userId) {
        Map<String, String> result = new HashMap<>();
        if (userId == null) {
            return result;
        }

        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getUserId, userId);

        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    public String getStringConfig(Long userId, String key, String defaultValue) {
        return getStringConfig(getUserConfigValues(userId), key, defaultValue);
    }

    public String getStringConfig(Map<String, String> configs, String key, String defaultValue) {
        String value = configs.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public boolean getBooleanConfig(Long userId, String key, boolean defaultValue) {
        return getBooleanConfig(getUserConfigValues(userId), key, defaultValue);
    }

    public boolean getBooleanConfig(Map<String, String> configs, String key, boolean defaultValue) {
        String value = configs.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    public int getIntConfig(Long userId, String key, int defaultValue) {
        return getIntConfig(getUserConfigValues(userId), key, defaultValue);
    }

    public int getIntConfig(Map<String, String> configs, String key, int defaultValue) {
        String value = configs.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDoubleConfig(Long userId, String key, double defaultValue) {
        return getDoubleConfig(getUserConfigValues(userId), key, defaultValue);
    }

    public double getDoubleConfig(Map<String, String> configs, String key, double defaultValue) {
        String value = configs.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
