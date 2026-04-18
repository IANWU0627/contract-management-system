package com.contracthub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 合同模板编辑场景的 AI 分析：提示词、JSON 解析与变量一致性校验（供 POST /api/templates/analyze）。
 */
@Service
public class TemplateAiAssistantService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[\\[([^\\]]+)]]");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildUserPrompt(String name, String category, String description, String contentHtml, String variablesText) {
        String plain = stripHtml(contentHtml);
        String truncated = plain.length() > 8000 ? plain.substring(0, 8000) : plain;
        String varsSnippet = variablesText != null && variablesText.length() > 4000
                ? variablesText.substring(0, 4000)
                : (variablesText != null ? variablesText : "");

        StringBuilder sb = new StringBuilder();
        sb.append("你是「合同模板编辑助手」：从可维护性、条款完整性、变量占位符规范等角度，帮助用户发现模板草稿中的问题。\n");
        sb.append("模板名称：").append(nullToEmpty(name)).append("\n");
        sb.append("分类：").append(nullToEmpty(category)).append("\n");
        sb.append("描述：").append(nullToEmpty(description)).append("\n\n");
        sb.append("变量 JSON（键=占位符名，值=展示名称，可能不完整）：\n").append(varsSnippet.isEmpty() ? "（空）" : varsSnippet).append("\n\n");
        sb.append("模板正文（已去 HTML 标签，占位符形如 [[varName]]）：\n").append(truncated.isEmpty() ? "（空）" : truncated).append("\n\n");
        sb.append("请只返回一个 JSON 对象（不要 Markdown、不要解释），字段如下：\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"模板质量概述（120字以内）\",\n");
        sb.append("  \"score\": 0-100 的整数（越高表示结构越清晰、越利于复用）,\n");
        sb.append("  \"risks\": [\n");
        sb.append("    {\n");
        sb.append("      \"level\": \"high或medium或low\",\n");
        sb.append("      \"content\": \"具体问题说明（如条款缺失、表述歧义、变量命名问题等）\",\n");
        sb.append("      \"anchor\": \"可选：从正文中摘录的短片段用于定位，不超过120字\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"suggestions\": [\"可执行的修改建议1\", \"建议2\"],\n");
        sb.append("  \"keyInfo\": {\n");
        sb.append("    \"structureNotes\": \"对章节/条款结构的简要评价\",\n");
        sb.append("    \"variableHints\": \"对 [[变量]] 使用与 JSON 定义是否一致等的提示\"\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    public Map<String, Object> parseModelMessageContent(String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            String jsonStr = extractJson(content);
            if (jsonStr != null) {
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                result.put("summary", parsed.getOrDefault("summary", "分析完成"));
                result.put("score", toIntScore(parsed.get("score")));
                result.put("risks", normalizeRisks(parsed.get("risks")));
                result.put("suggestions", normalizeStringList(parsed.get("suggestions")));
                Object keyInfo = parsed.get("keyInfo");
                result.put("keyInfo", keyInfo instanceof Map ? keyInfo : new HashMap<String, Object>());
                return result;
            }
        } catch (Exception ignored) {
            // fall through
        }
        result.put("summary", "AI 分析完成");
        result.put("score", 75);
        result.put("risks", List.of(Map.of("level", "low", "content", "请结合人工复核模板条款与变量定义", "anchor", "")));
        result.put("suggestions", List.of("建议检查占位符 [[变量名]] 与下方 JSON 键是否一致"));
        result.put("keyInfo", new HashMap<String, Object>());
        return result;
    }

    public void mergeConsistencyAndEnrich(String name, String category, String contentHtml, String variablesText, Map<String, Object> result) {
        @SuppressWarnings("unchecked")
        Map<String, Object> keyInfo = result.get("keyInfo") instanceof Map
                ? new LinkedHashMap<>((Map<String, Object>) result.get("keyInfo"))
                : new LinkedHashMap<>();
        keyInfo.put("templateName", nullToEmpty(name));
        keyInfo.put("category", nullToEmpty(category));

        List<String> warnings = computeConsistencyWarnings(contentHtml, variablesText);
        keyInfo.put("consistencyWarnings", warnings);
        result.put("keyInfo", keyInfo);
    }

    public Map<String, Object> buildOfflineDemoResult(String name, String category, String description, String contentHtml, String variablesText) {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", "当前为离线演示分析：会结合正文片段与变量 JSON 做一致性检查；配置真实 AI 接口后可获得更细化的条款与表述建议。");
        result.put("score", 72);
        result.put("suggestions", Arrays.asList(
                "保存前建议核对争议解决、付款与交付等关键条款是否齐全",
                "占位符请统一使用 [[变量名]]，并在变量 JSON 中为每个键填写展示名称"
        ));
        List<Map<String, Object>> demoRisks = new ArrayList<>();
        demoRisks.add(Map.of(
                "level", "low",
                "content", "模板越长越建议拆分章节标题，便于业务侧快速定位修改。",
                "anchor", ""
        ));
        String plain = stripHtml(contentHtml);
        if (plain.length() < 80) {
            demoRisks.add(0, Map.of("level", "medium", "content", "正文过短，可能尚未覆盖主要商务条款。", "anchor", ""));
        }
        result.put("risks", demoRisks);
        Map<String, Object> keyInfo = new LinkedHashMap<>();
        keyInfo.put("structureNotes", description != null && !description.isBlank()
                ? "已填写描述，便于他人理解模板用途。"
                : "可考虑补充模板描述，说明适用场景与注意事项。");
        keyInfo.put("variableHints", "将下方「变量 JSON」中的键与正文 [[占位符]] 对齐，可减少签约时漏填。");
        result.put("keyInfo", keyInfo);
        mergeConsistencyAndEnrich(name, category, contentHtml, variablesText, result);
        return result;
    }

    private List<String> computeConsistencyWarnings(String contentHtml, String variablesText) {
        List<String> out = new ArrayList<>();
        Set<String> inContent = extractPlaceholderCodes(contentHtml);
        Set<String> inJson = new LinkedHashSet<>();
        try {
            if (variablesText != null && !variablesText.isBlank()) {
                Map<String, Object> map = objectMapper.readValue(variablesText, new TypeReference<Map<String, Object>>() {});
                inJson.addAll(map.keySet());
            }
        } catch (Exception e) {
            out.add("变量 JSON 无法解析，请检查是否为合法 JSON 对象。");
            return out;
        }
        for (String code : inContent) {
            if (!inJson.contains(code)) {
                out.add("正文中出现 [[" + code + "]]，但变量 JSON 中未定义该键。");
            }
        }
        for (String key : inJson) {
            if (!inContent.contains(key)) {
                out.add("变量 JSON 中存在键 \"" + key + "\"，但正文中未找到 [[" + key + "]]。");
            }
        }
        return out;
    }

    private Set<String> extractPlaceholderCodes(String html) {
        Set<String> set = new LinkedHashSet<>();
        if (html == null || html.isEmpty()) {
            return set;
        }
        Matcher m = PLACEHOLDER_PATTERN.matcher(html);
        while (m.find()) {
            String code = m.group(1).trim();
            if (!code.isEmpty()) {
                set.add(code);
            }
        }
        return set;
    }

    private static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        String s = html.replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return s;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static int toIntScore(Object scoreObj) {
        if (scoreObj instanceof Number n) {
            int v = n.intValue();
            return Math.max(0, Math.min(100, v));
        }
        try {
            int v = Integer.parseInt(String.valueOf(scoreObj).trim());
            return Math.max(0, Math.min(100, v));
        } catch (Exception e) {
            return 75;
        }
    }

    private List<Map<String, Object>> normalizeRisks(Object risksObj) {
        if (!(risksObj instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof String s && !s.isBlank()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("level", "medium");
                row.put("content", s.trim());
                row.put("anchor", "");
                out.add(row);
                continue;
            }
            if (o instanceof Map<?, ?> m) {
                Map<String, Object> row = new LinkedHashMap<>();
                String level = m.get("level") != null ? String.valueOf(m.get("level")).trim().toLowerCase(Locale.ROOT) : "medium";
                if (!Set.of("high", "medium", "low").contains(level)) {
                    level = "medium";
                }
                row.put("level", level);
                row.put("content", m.get("content") != null ? String.valueOf(m.get("content")) : "");
                row.put("anchor", m.get("anchor") != null ? String.valueOf(m.get("anchor")) : "");
                if (!String.valueOf(row.get("content")).isBlank()) {
                    out.add(row);
                }
            }
        }
        return out;
    }

    private List<String> normalizeStringList(Object obj) {
        if (!(obj instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o != null && !String.valueOf(o).isBlank()) {
                out.add(String.valueOf(o).trim());
            }
        }
        return out;
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
