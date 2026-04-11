package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.mapper.ContractMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
public class AiController {
    
    private final ContractMapper contractMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AiController(ContractMapper contractMapper) {
        this.contractMapper = contractMapper;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @PostMapping("/test")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody Map<String, String> config) {
        try {
            String apiUrl = config.get("apiUrl");
            String apiKey = config.get("apiKey");
            String model = config.get("model");
            if (apiUrl == null || apiUrl.isBlank()) {
                return ApiResponse.error("apiUrl 不能为空");
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
                return ApiResponse.error("Connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return ApiResponse.error("Connection failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/analyze/{id}")
    public ApiResponse<Map<String, Object>> analyze(@PathVariable Long id, @RequestBody Map<String, String> config) {
        try {
            Contract contract = contractMapper.selectById(id);
            if (contract == null) {
                return ApiResponse.error("Contract not found");
            }
            
            String apiUrl = config.get("apiUrl");
            String apiKey = config.getOrDefault("apiKey", "");
            String model = config.getOrDefault("model", "gpt-3.5-turbo");
            if (apiUrl == null || apiUrl.isBlank()) {
                return ApiResponse.error("apiUrl 不能为空");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            
            String prompt = buildAnalysisPrompt(contract);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "You are a professional contract analyst. Analyze contracts and provide structured feedback."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(responseBody, Map.class);
                
                // 解析 AI 响应
                List<?> choices = (List<?>) parsed.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    String aiContent = (String) message.get("content");
                    
                    Map<String, Object> result = parseAiResponse(aiContent);
                    return ApiResponse.success(result);
                }
            }
            
            return ApiResponse.error("AI analysis failed");
        } catch (Exception e) {
            return ApiResponse.error("AI analysis failed: " + e.getMessage());
        }
    }
    
    private String buildAnalysisPrompt(Contract contract) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下合同并提供结构化的风险评估报告：\n\n");
        sb.append("合同名称：").append(contract.getTitle()).append("\n");
        sb.append("合同编号：").append(contract.getContractNo()).append("\n");
        sb.append("合同类型：").append(contract.getType()).append("\n");
        sb.append("相对方：").append(contract.getCounterparty()).append("\n");
        sb.append("金额：").append(contract.getAmount()).append("\n");
        sb.append("开始日期：").append(contract.getStartDate()).append("\n");
        sb.append("结束日期：").append(contract.getEndDate()).append("\n\n");
        
        if (contract.getContent() != null && !contract.getContent().isEmpty()) {
            sb.append("合同内容：\n").append(contract.getContent().substring(0, Math.min(2000, contract.getContent().length())));
        }
        
        sb.append("\n\n请按照以下JSON格式返回分析结果（只返回JSON，不要其他内容）：\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"合同概述（100字以内）\",\n");
        sb.append("  \"score\": 风险评分（0-100的整数，越高越安全）,\n");
        sb.append("  \"risks\": [\n");
        sb.append("    { \"level\": \"high/medium/low\", \"content\": \"风险描述\" },\n");
        sb.append("    { \"level\": \"high/medium/low\", \"content\": \"风险描述\" }\n");
        sb.append("  ],\n");
        sb.append("  \"suggestions\": [\"建议1\", \"建议2\"],\n");
        sb.append("  \"keyInfo\": {\n");
        sb.append("    \"partyA\": \"甲方\",\n");
        sb.append("    \"partyB\": \"乙方\",\n");
        sb.append("    \"amount\": \"金额\",\n");
        sb.append("    \"duration\": \"有效期\",\n");
        sb.append("    \"keyClauses\": [\"关键条款1\", \"关键条款2\"]\n");
        sb.append("  }\n");
        sb.append("}");
        
        return sb.toString();
    }
    
    private Map<String, Object> parseAiResponse(String content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试解析 JSON
            String jsonStr = extractJson(content);
            if (jsonStr != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);
                result.put("summary", parsed.getOrDefault("summary", "分析完成"));
                result.put("score", parsed.getOrDefault("score", 75));
                
                // 解析风险列表，支持两种格式：简单字符串数组或带level的对象数组
                Object risksObj = parsed.get("risks");
                if (risksObj instanceof List) {
                    List<?> risksList = (List<?>) risksObj;
                    if (!risksList.isEmpty() && risksList.get(0) instanceof Map) {
                        // 已经是带level的对象数组，直接使用
                        result.put("risks", risksList);
                    } else {
                        // 简单字符串数组，转换为带level的对象
                        List<Map<String, String>> formattedRisks = new ArrayList<>();
                        for (Object risk : risksList) {
                            Map<String, String> riskObj = new HashMap<>();
                            riskObj.put("level", "medium");
                            riskObj.put("content", risk != null ? risk.toString() : "");
                            formattedRisks.add(riskObj);
                        }
                        result.put("risks", formattedRisks);
                    }
                } else {
                    // 默认风险
                    result.put("risks", Arrays.asList(Map.of("level", "low", "content", "未发现明显风险")));
                }
                
                result.put("suggestions", parsed.getOrDefault("suggestions", Arrays.asList("建议仔细阅读合同条款")));
                
                // 解析关键信息
                Object keyInfoObj = parsed.get("keyInfo");
                if (keyInfoObj instanceof Map) {
                    result.put("keyInfo", keyInfoObj);
                } else {
                    Map<String, Object> defaultKeyInfo = new HashMap<>();
                    defaultKeyInfo.put("partyA", "");
                    defaultKeyInfo.put("partyB", "");
                    defaultKeyInfo.put("amount", "");
                    defaultKeyInfo.put("duration", "");
                    defaultKeyInfo.put("keyClauses", Arrays.asList());
                    result.put("keyInfo", defaultKeyInfo);
                }
                
                return result;
            }
        } catch (Exception e) {
            // 解析失败，使用默认格式
        }
        
        // 默认返回
        result.put("summary", "AI 分析完成");
        result.put("score", 75);
        result.put("risks", Arrays.asList(Map.of("level", "low", "content", "请人工审核合同条款")));
        result.put("suggestions", Arrays.asList("建议与法务团队确认关键条款"));
        Map<String, Object> defaultKeyInfo = new HashMap<>();
        defaultKeyInfo.put("partyA", "");
        defaultKeyInfo.put("partyB", "");
        defaultKeyInfo.put("amount", "");
        defaultKeyInfo.put("duration", "");
        defaultKeyInfo.put("keyClauses", Arrays.asList());
        result.put("keyInfo", defaultKeyInfo);
        return result;
    }
    
    private String extractJson(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return null;
    }
}
