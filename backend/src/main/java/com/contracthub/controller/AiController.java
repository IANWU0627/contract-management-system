package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.service.ContractAiAssistantService;
import com.contracthub.service.ContractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI 相关接口（连接测试、Ollama 模型列表、兼容路径下的合同分析）。
 * <p>合同内容分析请优先使用 {@code POST /api/contracts/{id}/analyze}；{@code POST /api/ai/analyze/{id}} 为兼容保留，
 * 与合同接口共用 {@link com.contracthub.service.ContractAiAssistantService}。
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String SYSTEM_MESSAGE =
            "You are a bilingual contract assistant. Always respond with a single JSON object only, following the user's schema. Use Chinese for natural-language fields when the user writes in Chinese.";

    private final ContractService contractService;
    private final ContractAiAssistantService contractAiAssistantService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiController(ContractService contractService,
                        ContractAiAssistantService contractAiAssistantService) {
        this.contractService = contractService;
        this.contractAiAssistantService = contractAiAssistantService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/test")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody Map<String, String> config) {
        try {
            String apiUrl = config.get("apiUrl");
            String apiKey = config.get("apiKey");
            String model = config.get("model");
            if (apiUrl == null || apiUrl.isBlank()) {
                return ApiResponse.error("apiUrl 不能为空", "error.ai.apiUrlRequired");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "user", "content", "Hello, just testing connection.")
            ));
            requestBody.put("max_tokens", 10);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ApiResponse.success(Map.of("success", true, "message", "Connection successful"));
            } else {
                return ApiResponse.error("Connection failed: " + response.getStatusCode(), "error.ai.connectionFailed");
            }
        } catch (Exception e) {
            return ApiResponse.error("Connection failed: " + e.getMessage(), "error.ai.connectionFailed");
        }
    }

    @GetMapping("/ollama/models")
    public ApiResponse<List<Map<String, String>>> getOllamaModels(@RequestParam(defaultValue = "http://localhost:11434") String baseUrl) {
        try {
            String modelsUrl = baseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(modelsUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> models = (List<Map<String, Object>>) result.get("models");

                List<Map<String, String>> formattedModels = new ArrayList<>();
                if (models != null) {
                    for (Map<String, Object> model : models) {
                        Map<String, String> formatted = new HashMap<>();
                        String name = (String) model.get("name");
                        formatted.put("value", name);
                        formatted.put("label", name);
                        formattedModels.add(formatted);
                    }
                }
                return ApiResponse.success(formattedModels);
            } else {
                return ApiResponse.error("Failed to fetch models: " + response.getStatusCode(), "error.ai.modelsFetchFailed");
            }
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch models: " + e.getMessage(), "error.ai.modelsFetchFailed");
        }
    }

    @PostMapping("/analyze/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> analyze(@PathVariable Long id, @RequestBody(required = false) Map<String, String> config) {
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) {
                return ApiResponse.error("合同不存在", "error.ai.contractNotFound");
            }

            Map<String, String> cfg = config != null ? config : Map.of();
            String apiUrl = cfg.get("apiUrl");
            String apiKey = cfg.getOrDefault("apiKey", "");
            String model = cfg.getOrDefault("model", "gpt-3.5-turbo");
            double temperature = parseDouble(cfg.get("temperature"), 0.7);
            int maxTokens = parseInt(cfg.get("maxTokens"), 1000);

            if (apiUrl == null || apiUrl.isBlank()) {
                return ApiResponse.success(contractAiAssistantService.buildOfflineDemoResult(contract));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            String prompt = contractAiAssistantService.buildUserPrompt(contract);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", SYSTEM_MESSAGE),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(response.getBody(), Map.class);

                List<?> choices = (List<?>) parsed.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    String aiContent = (String) message.get("content");

                    Map<String, Object> result = contractAiAssistantService.parseModelMessageContent(aiContent);
                    contractAiAssistantService.enrichResult(contract, result);
                    return ApiResponse.success(result);
                }
            }

            return ApiResponse.error("AI 分析失败", "error.ai.analysisFailed");
        } catch (Exception e) {
            return ApiResponse.error("AI 分析失败: " + e.getMessage(), "error.ai.analysisFailed");
        }
    }

    private static double parseDouble(String raw, double defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
