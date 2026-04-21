package com.contracthub.service;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.util.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractAiGatewayService {
    private final ContractService contractService;
    private final UserConfigService userConfigService;
    private final ContractAiAssistantService contractAiAssistantService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractAiGatewayService(
            ContractService contractService,
            UserConfigService userConfigService,
            ContractAiAssistantService contractAiAssistantService,
            RestTemplate restTemplate) {
        this.contractService = contractService;
        this.userConfigService = userConfigService;
        this.contractAiAssistantService = contractAiAssistantService;
        this.restTemplate = restTemplate;
    }

    public ApiResponse<Map<String, Object>> analyze(Long id, Map<String, Object> aiConfig) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, String> configs = userConfigService.getUserConfigValues(userId);
        String apiUrl = getConfigValue(aiConfig, "apiUrl", userConfigService.getStringConfig(configs, "ai_api_url", ""));
        String apiKey = getConfigValue(aiConfig, "apiKey", userConfigService.getStringConfig(configs, "ai_api_key", ""));
        String model = getConfigValue(aiConfig, "model", userConfigService.getStringConfig(configs, "ai_model", "gpt-3.5-turbo"));
        double temperature = getDoubleConfigValue(aiConfig, "temperature", userConfigService.getDoubleConfig(configs, "bd_temperature", 0.7));
        int maxTokens = getIntConfigValue(aiConfig, "maxTokens", userConfigService.getIntConfig(configs, "bd_max_tokens", 1000));

        if (!apiUrl.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (apiKey != null && !apiKey.isEmpty()) {
                    headers.set("Authorization", "Bearer " + apiKey);
                }

                String prompt = contractAiAssistantService.buildUserPrompt(contract);
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", Arrays.asList(
                        Map.of("role", "system", "content", "You are a bilingual contract assistant. Always respond with a single JSON object only, following the user's schema. Use Chinese for natural-language fields when the user writes in Chinese."),
                        Map.of("role", "user", "content", prompt)
                ));
                requestBody.put("temperature", temperature);
                requestBody.put("max_tokens", maxTokens);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    String responseBody = response.getBody();
                    Map<String, Object> parsed = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                    List<?> choices = (List<?>) parsed.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                        Map<?, ?> message = (Map<?, ?>) choice.get("message");
                        String aiContent = (String) message.get("content");
                        Map<String, Object> parsedResult = contractAiAssistantService.parseModelMessageContent(aiContent);
                        contractAiAssistantService.enrichResult(contract, parsedResult);
                        return ApiResponse.success(parsedResult);
                    }
                }
            } catch (Exception e) {
                return ApiResponse.error("AI 分析失败: " + e.getMessage());
            }
        }

        return ApiResponse.success(contractAiAssistantService.buildOfflineDemoResult(contract));
    }

    private String getConfigValue(Map<String, Object> configMap, String key, String defaultValue) {
        if (configMap == null) {
            return defaultValue;
        }
        Object value = configMap.get(key);
        if (value == null) {
            return defaultValue;
        }
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? defaultValue : result;
    }

    private double getDoubleConfigValue(Map<String, Object> configMap, String key, double defaultValue) {
        if (configMap == null || configMap.get(key) == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(configMap.get(key)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int getIntConfigValue(Map<String, Object> configMap, String key, int defaultValue) {
        if (configMap == null || configMap.get(key) == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(configMap.get(key)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
