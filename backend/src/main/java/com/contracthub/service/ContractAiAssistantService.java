package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 合同 AI 助手：统一提示词、模型 JSON 解析与结果增强（供 /api/contracts/{id}/analyze 与 /api/ai/analyze/{id} 共用）。
 */
@Service
public class ContractAiAssistantService {

    private static final Set<String> SUPPORTED_AI_ACTIONS = Set.of(
            "submit",
            "approve",
            "reject",
            "sign",
            "terminate",
            "startRenewal",
            "completeRenewal",
            "declineRenewal"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildUserPrompt(Contract contract) {
        return buildAssistantPrompt(contract, summarizeCounterparties(contract));
    }

    /**
     * 无 AI 配置时的演示数据，经 enrich 后与线上一致。
     */
    public Map<String, Object> buildOfflineDemoResult(Contract contract) {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", "本合同为常见的商务合同结构。助手模式会给出谈判要点、条款覆盖检查与可定位的风险提示；配置真实 AI 后可获得更贴合正文的结果。");
        result.put("score", 78);
        result.put("negotiationPoints", Arrays.asList(
                "核对付款节点、发票类型与逾期责任是否与报价/订单一致",
                "确认争议解决方式（诉讼/仲裁）与管辖地是否符合公司政策",
                "如涉交付/验收，明确验收标准与异议期"
        ));
        List<Map<String, Object>> demoChecks = new ArrayList<>();
        demoChecks.add(createMissingCheck("争议解决", false, "medium", "未明确仲裁或诉讼及管辖地时需补充"));
        demoChecks.add(createMissingCheck("不可抗力", false, "low", "建议约定通知义务与免责范围"));
        demoChecks.add(createMissingCheck("保密义务", true, "low", "正文已出现保密相关表述"));
        result.put("missingClauseChecks", demoChecks);
        result.put("risks", Arrays.asList(
                Map.of("level", "medium", "content", "付款周期偏长，可能影响现金流，建议与财务确认可接受账期。", "anchor", ""),
                Map.of("level", "low", "content", "违约责任量化方式尚可，建议关注上限是否与合同金额匹配。", "anchor", "")
        ));
        result.put("suggestions", Arrays.asList("签署前建议由法务复核违约金与付款条款；重大合同建议保留版本留痕。"));
        result.put("actionSuggestions", new ArrayList<>());
        result.put("clauseSuggestions", new ArrayList<>());
        result.put("keyInfo", new HashMap<String, Object>());
        enrichResult(contract, result);
        return result;
    }

    public Map<String, Object> parseModelMessageContent(String content) {
        return parseAssistantResponse(content);
    }

    public void enrichResult(Contract contract, Map<String, Object> result) {
        enrichAiResult(contract, result);
    }

    private static Map<String, Object> createMissingCheck(String topic, boolean present, String severity, String note) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("topic", topic);
        m.put("present", present);
        m.put("severity", severity);
        m.put("note", note);
        return m;
    }

    private String buildAssistantPrompt(Contract contract, String counterpartySummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「合同助手」：在理解合同的基础上，输出可执行的结构化结果，帮助法务/业务快速复核与谈判。\n\n");
        sb.append("合同名称：").append(contract.getTitle()).append("\n");
        sb.append("合同编号：").append(contract.getContractNo()).append("\n");
        sb.append("合同类型：").append(contract.getType()).append("\n");
        sb.append("相对方：").append(counterpartySummary).append("\n");
        sb.append("金额：").append(contract.getAmount()).append("\n");
        sb.append("开始日期：").append(contract.getStartDate()).append("\n");
        sb.append("结束日期：").append(contract.getEndDate()).append("\n\n");

        if (contract.getContent() != null && !contract.getContent().isEmpty()) {
            String plainContent = contract.getContent()
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            sb.append("合同内容：\n").append(plainContent.substring(0, Math.min(6000, plainContent.length())));
        }

        sb.append("\n\n请只返回一个 JSON 对象（不要 Markdown、不要解释），字段如下：\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"合同概述（100字以内）\",\n");
        sb.append("  \"score\": 0-100 的整数（越高表示履约与条款越稳妥）,\n");
        sb.append("  \"negotiationPoints\": [\"与对方协商时可重点确认的要点1\", \"要点2\"],\n");
        sb.append("  \"missingClauseChecks\": [\n");
        sb.append("    {\n");
        sb.append("      \"topic\": \"争议解决 / 保密 / 知识产权 / 不可抗力 / 违约责任 / 付款与发票 等主题之一\",\n");
        sb.append("      \"present\": true或false（根据正文是否已覆盖该主题做判断）,\n");
        sb.append("      \"severity\": \"high或medium或low（若缺失则标严重程度）\",\n");
        sb.append("      \"note\": \"一句话说明依据或建议\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"risks\": [\n");
        sb.append("    {\n");
        sb.append("      \"level\": \"high或medium或low\",\n");
        sb.append("      \"content\": \"风险说明\",\n");
        sb.append("      \"anchor\": \"可选：从合同正文中摘录的短句（不超过120字），用于定位；若无法摘录可填空字符串\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"suggestions\": [\"条款或流程层面的优化建议1\", \"建议2\"],\n");
        sb.append("  \"actionSuggestions\": [\n");
        sb.append("    {\n");
        sb.append("      \"action\": \"submit\",\n");
        sb.append("      \"title\": \"建议提交审批\",\n");
        sb.append("      \"description\": \"当前合同条款较完整，可进入审批流程。\",\n");
        sb.append("      \"reason\": \"可选\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"clauseSuggestions\": [\n");
        sb.append("    {\n");
        sb.append("      \"code\": \"FORCE_MAJEURE\",\n");
        sb.append("      \"name\": \"不可抗力条款\",\n");
        sb.append("      \"category\": \"风险控制\",\n");
        sb.append("      \"reason\": \"说明为什么建议补充该条款\",\n");
        sb.append("      \"snippet\": \"建议补充的示例条款片段\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"keyInfo\": {\n");
        sb.append("    \"partyA\": \"甲方\",\n");
        sb.append("    \"partyB\": \"乙方\",\n");
        sb.append("    \"amount\": \"金额\",\n");
        sb.append("    \"duration\": \"有效期\",\n");
        sb.append("    \"keyClauses\": [\"关键条款1\", \"关键条款2\", \"关键条款3\"]\n");
        sb.append("  }\n");
        sb.append("}");

        return sb.toString();
    }

    private String summarizeCounterparties(Contract contract) {
        if (contract == null || contract.getCounterparties() == null || contract.getCounterparties().isBlank()) {
            return "";
        }
        try {
            List<Map<String, Object>> counterparties = objectMapper.readValue(
                    contract.getCounterparties(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            return counterparties.stream()
                    .map(item -> item.get("name"))
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(" / "));
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, Object> parseAssistantResponse(String content) {
        Map<String, Object> result = new HashMap<>();

        try {
            String jsonStr = extractJson(content);
            if (jsonStr != null) {
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                result.put("summary", parsed.getOrDefault("summary", "分析完成"));
                result.put("score", parsed.getOrDefault("score", 75));
                result.put("negotiationPoints", normalizeNegotiationPoints(parsed.get("negotiationPoints")));
                result.put("missingClauseChecks", normalizeMissingClauseChecks(parsed.get("missingClauseChecks")));
                result.put("risks", normalizeRiskItems(parsed.get("risks")));
                result.put("suggestions", parsed.getOrDefault("suggestions", Arrays.asList("建议仔细阅读合同条款")));
                result.put("actionSuggestions", normalizeActionSuggestions(parsed.get("actionSuggestions")));
                result.put("clauseSuggestions", normalizeClauseSuggestions(parsed.get("clauseSuggestions")));
                Object keyInfo = parsed.get("keyInfo");
                result.put("keyInfo", keyInfo instanceof Map ? keyInfo : new HashMap<String, Object>());
                return result;
            }
        } catch (Exception ignored) {
            // fall through
        }

        result.put("summary", "AI 分析完成");
        result.put("score", 75);
        result.put("negotiationPoints", new ArrayList<String>());
        result.put("missingClauseChecks", new ArrayList<Map<String, Object>>());
        result.put("risks", Arrays.asList(Map.of("level", "low", "content", "请人工审核合同条款", "anchor", "")));
        result.put("suggestions", Arrays.asList("建议与法务团队确认关键条款"));
        result.put("actionSuggestions", new ArrayList<>());
        result.put("clauseSuggestions", new ArrayList<>());
        result.put("keyInfo", new HashMap<String, Object>());
        return result;
    }

    private List<String> normalizeNegotiationPoints(Object obj) {
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

    private List<Map<String, Object>> normalizeMissingClauseChecks(Object obj) {
        if (!(obj instanceof List<?> raw)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : raw) {
            if (!(o instanceof Map<?, ?> m)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topic", m.get("topic") != null ? String.valueOf(m.get("topic")).trim() : "");
            row.put("present", parseBooleanLoose(m.get("present")));
            String sev = m.get("severity") != null ? String.valueOf(m.get("severity")).trim().toLowerCase(Locale.ROOT) : "medium";
            if (!Set.of("high", "medium", "low").contains(sev)) {
                sev = "medium";
            }
            row.put("severity", sev);
            row.put("note", m.get("note") != null ? String.valueOf(m.get("note")) : "");
            if (!row.get("topic").toString().isEmpty()) {
                out.add(row);
            }
        }
        return out;
    }

    private static boolean parseBooleanLoose(Object v) {
        if (v instanceof Boolean b) {
            return b;
        }
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
        return "true".equals(s) || "yes".equals(s) || "是".equals(s) || "1".equals(s) || "y".equals(s);
    }

    private List<Map<String, Object>> normalizeRiskItems(Object risksObj) {
        if (!(risksObj instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof String s) {
                if (!s.isBlank()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("level", "medium");
                    row.put("content", s.trim());
                    row.put("anchor", "");
                    out.add(row);
                }
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
                Object anchor = m.get("anchor");
                if (anchor != null) {
                    String a = String.valueOf(anchor).trim();
                    if (a.length() > 200) {
                        a = a.substring(0, 200) + "…";
                    }
                    row.put("anchor", a);
                } else {
                    row.put("anchor", "");
                }
                out.add(row);
            }
        }
        if (out.isEmpty()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("level", "low");
            row.put("content", "未发现明显风险");
            row.put("anchor", "");
            out.add(row);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private void enrichAiResult(Contract contract, Map<String, Object> result) {
        Map<String, Object> keyInfo;
        Object keyInfoObj = result.get("keyInfo");
        if (keyInfoObj instanceof Map<?, ?>) {
            keyInfo = (Map<String, Object>) keyInfoObj;
        } else {
            keyInfo = new HashMap<>();
        }

        String counterparty = summarizeCounterparties(contract);
        String[] parties = counterparty.split("\\s*/\\s*");
        keyInfo.putIfAbsent("partyA", parties.length > 0 ? parties[0] : "");
        keyInfo.putIfAbsent("partyB", parties.length > 1 ? parties[1] : "");
        keyInfo.putIfAbsent("amount", contract.getAmount() != null ? contract.getAmount().toString() : "");
        keyInfo.putIfAbsent("duration", String.format("%s ~ %s",
                contract.getStartDate() != null ? contract.getStartDate() : "",
                contract.getEndDate() != null ? contract.getEndDate() : ""));

        Object clausesObj = keyInfo.get("keyClauses");
        if (!(clausesObj instanceof List<?>)) {
            Object topLevelClauses = result.get("keyClauses");
            if (topLevelClauses instanceof List<?>) {
                keyInfo.put("keyClauses", topLevelClauses);
            } else {
                keyInfo.put("keyClauses", new ArrayList<>());
            }
        }
        result.put("keyInfo", keyInfo);
        result.put("actionSuggestions", buildExecutableActionSuggestions(contract, result.get("actionSuggestions"), result));
        result.put("clauseSuggestions", buildClauseSuggestions(contract, result.get("clauseSuggestions"), result));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeActionSuggestions(Object actionSuggestionsObj) {
        if (!(actionSuggestionsObj instanceof List<?> rawList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object item : rawList) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Object actionObj = map.get("action");
            if (actionObj == null) {
                continue;
            }
            String action = String.valueOf(actionObj).trim();
            if (!SUPPORTED_AI_ACTIONS.contains(action)) {
                continue;
            }
            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("action", action);
            Object title = map.get("title");
            Object description = map.get("description");
            suggestion.put("title", title != null ? String.valueOf(title) : defaultActionTitle(action));
            suggestion.put("description", description != null ? String.valueOf(description) : defaultActionDescription(action));
            Object reason = map.get("reason");
            if (reason != null) {
                suggestion.put("reason", String.valueOf(reason));
            }
            normalized.add(suggestion);
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildExecutableActionSuggestions(Contract contract, Object actionSuggestionsObj, Map<String, Object> analysisResult) {
        List<Map<String, Object>> normalized = actionSuggestionsObj instanceof List<?>
                ? (List<Map<String, Object>>) actionSuggestionsObj
                : new ArrayList<>();
        List<Map<String, Object>> executable = new ArrayList<>();
        Set<String> existingActions = new LinkedHashSet<>();

        for (Map<String, Object> suggestion : normalized) {
            String action = String.valueOf(suggestion.getOrDefault("action", ""));
            if (!isActionAllowed(contract, action) || existingActions.contains(action)) {
                continue;
            }
            executable.add(suggestion);
            existingActions.add(action);
        }

        if (!executable.isEmpty()) {
            return executable;
        }

        Object scoreObj = analysisResult.get("score");
        int score = scoreObj instanceof Number ? ((Number) scoreObj).intValue() : 75;
        String status = contract.getStatus() != null ? contract.getStatus() : "";

        switch (status) {
            case "DRAFT":
                executable.add(createActionSuggestion("submit", score >= 75 ? "建议提交审批" : "建议完善后提交审批",
                        score >= 75 ? "当前内容较完整，可推进到审批阶段。" : "建议先处理风险项后，再进入审批流程。", null));
                break;
            case "PENDING":
            case "APPROVING":
                if (score >= 75) {
                    executable.add(createActionSuggestion("approve", "建议审批通过", "整体风险可控，可进入下一步。", null));
                } else {
                    executable.add(createActionSuggestion("reject", "建议退回修改", "当前风险较高，建议补充或修订关键条款。", "请根据 AI 风险建议完善后重新提交。"));
                }
                break;
            case "APPROVED":
                executable.add(createActionSuggestion("sign", "建议推进签署", "审批已完成，可安排签署落地。", null));
                break;
            case "SIGNED":
                if (shouldSuggestRenewal(contract)) {
                    executable.add(createActionSuggestion("startRenewal", "建议发起续签", "合同临近到期，可提前启动续签流程。", "合同临近到期，建议提前发起续签。"));
                }
                break;
            case "RENEWING":
                executable.add(createActionSuggestion("completeRenewal", "建议确认续签完成", "如双方已达成一致，可记录续签完成。", "双方已确认续签条款并完成流程。"));
                executable.add(createActionSuggestion("declineRenewal", "建议标记为不续签", "若业务不再继续，可结束本次续签流程。", "经评估，本合同不再继续续签。"));
                break;
            default:
                break;
        }
        return executable;
    }

    private List<Map<String, Object>> normalizeClauseSuggestions(Object clauseSuggestionsObj) {
        if (!(clauseSuggestionsObj instanceof List<?> rawList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object item : rawList) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Object code = map.get("code");
            Object name = map.get("name");
            if (code == null && name == null) {
                continue;
            }
            Map<String, Object> clause = new LinkedHashMap<>();
            clause.put("code", code != null ? String.valueOf(code) : "");
            clause.put("name", name != null ? String.valueOf(name) : "");
            clause.put("category", map.get("category") != null ? String.valueOf(map.get("category")) : "通用");
            clause.put("reason", map.get("reason") != null ? String.valueOf(map.get("reason")) : "");
            clause.put("snippet", map.get("snippet") != null ? String.valueOf(map.get("snippet")) : "");
            normalized.add(clause);
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildClauseSuggestions(Contract contract, Object clauseSuggestionsObj, Map<String, Object> analysisResult) {
        List<Map<String, Object>> normalized = clauseSuggestionsObj instanceof List<?>
                ? (List<Map<String, Object>>) clauseSuggestionsObj
                : new ArrayList<>();
        List<Map<String, Object>> merged = new ArrayList<>();
        Set<String> existingCodes = new LinkedHashSet<>();

        for (Map<String, Object> suggestion : normalized) {
            String code = String.valueOf(suggestion.getOrDefault("code", "")).trim();
            if (!code.isEmpty()) {
                existingCodes.add(code);
            }
            merged.add(suggestion);
        }

        String content = contract.getContent() == null ? "" : contract.getContent().replaceAll("<[^>]+>", " ").toLowerCase(Locale.ROOT);

        addClauseSuggestionIfMissing(merged, existingCodes, content, "FORCE_MAJEURE", "不可抗力条款", "风险控制",
                "当前合同文本中未明显覆盖不可抗力场景，建议补充免责和通知机制。",
                "因地震、台风、洪水、战争、政府管制等不可抗力事件导致任一方不能履行合同义务的，受影响方在合理期限内通知对方后，可部分或全部免责。");

        addClauseSuggestionIfMissing(merged, existingCodes, content, "CONFIDENTIALITY", "保密条款", "信息安全",
                "合同涉及商业合作内容，建议明确保密范围、保密期限与违约责任。",
                "双方对在履行本合同过程中获悉的对方商业秘密、技术资料及其他未公开信息承担保密义务，未经对方书面同意不得向第三方披露。");

        if (isPaymentSensitiveContract(contract)) {
            addClauseSuggestionIfMissing(merged, existingCodes, content, "PAYMENT_AND_INVOICE", "付款与发票条款", "财务结算",
                    "建议补充付款节点、发票类型与逾期付款责任，降低结算争议。",
                    "甲方应在乙方开具合法有效发票后【X】个工作日内完成付款；逾期付款的，应按未付款项的一定比例承担违约责任。");
        }

        Object scoreObj = analysisResult.get("score");
        int score = scoreObj instanceof Number ? ((Number) scoreObj).intValue() : 75;
        if (score < 70) {
            addClauseSuggestionIfMissing(merged, existingCodes, content, "DEFAULT_AND_TERMINATION", "违约与解除条款", "违约处置",
                    "AI 风险评分较低，建议进一步明确违约责任、补救期与合同解除条件。",
                    "任一方严重违约且在收到书面催告后【X】日内仍未整改的，守约方有权解除合同并要求违约方承担相应损失。");
        }

        return merged;
    }

    private void addClauseSuggestionIfMissing(List<Map<String, Object>> merged, Set<String> existingCodes, String content,
                                              String code, String name, String category, String reason, String snippet) {
        if (existingCodes.contains(code)) {
            return;
        }
        String normalizedContent = content == null ? "" : content;
        if (normalizedContent.contains(name.replace("条款", "").toLowerCase(Locale.ROOT))) {
            return;
        }
        Map<String, Object> clause = new LinkedHashMap<>();
        clause.put("code", code);
        clause.put("name", name);
        clause.put("category", category);
        clause.put("reason", reason);
        clause.put("snippet", snippet);
        merged.add(clause);
        existingCodes.add(code);
    }

    private boolean isPaymentSensitiveContract(Contract contract) {
        if (contract.getAmount() != null && contract.getAmount().doubleValue() > 0) {
            return true;
        }
        String type = contract.getType();
        return "PURCHASE".equalsIgnoreCase(type) || "SALES".equalsIgnoreCase(type) || "SERVICE".equalsIgnoreCase(type);
    }

    private boolean isActionAllowed(Contract contract, String action) {
        String status = contract.getStatus();
        if (status == null) {
            return false;
        }
        switch (action) {
            case "submit":
                return "DRAFT".equals(status);
            case "approve":
            case "reject":
                return "PENDING".equals(status) || "APPROVING".equals(status);
            case "sign":
                return "APPROVED".equals(status);
            case "terminate":
                return "SIGNED".equals(status) || "APPROVED".equals(status) || "RENEWING".equals(status);
            case "startRenewal":
                return "SIGNED".equals(status);
            case "completeRenewal":
            case "declineRenewal":
                return "RENEWING".equals(status);
            default:
                return false;
        }
    }

    private boolean shouldSuggestRenewal(Contract contract) {
        if (contract.getEndDate() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);
        return !contract.getEndDate().isBefore(today) && !contract.getEndDate().isAfter(threshold);
    }

    private Map<String, Object> createActionSuggestion(String action, String title, String description, String reason) {
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("action", action);
        suggestion.put("title", title);
        suggestion.put("description", description);
        if (reason != null && !reason.isBlank()) {
            suggestion.put("reason", reason);
        }
        return suggestion;
    }

    private String defaultActionTitle(String action) {
        switch (action) {
            case "submit":
                return "建议提交审批";
            case "approve":
                return "建议审批通过";
            case "reject":
                return "建议退回修改";
            case "sign":
                return "建议推进签署";
            case "terminate":
                return "建议终止合同";
            case "startRenewal":
                return "建议发起续签";
            case "completeRenewal":
                return "建议确认续签完成";
            case "declineRenewal":
                return "建议标记为不续签";
            default:
                return "建议执行动作";
        }
    }

    private String defaultActionDescription(String action) {
        switch (action) {
            case "submit":
                return "当前合同条款较完整，可提交进入审批流程。";
            case "approve":
                return "AI 判断整体风险可控，可进入下一步。";
            case "reject":
                return "建议先修订高风险条款，再重新处理。";
            case "sign":
                return "审批完成后可继续推进签署。";
            case "terminate":
                return "若合同继续履约风险过高，可考虑终止。";
            case "startRenewal":
                return "合同临近到期，可提前启动续签流程。";
            case "completeRenewal":
                return "若续签内容已确认，可完成续签。";
            case "declineRenewal":
                return "若不再继续合作，可结束续签流程。";
            default:
                return "AI 建议执行该动作。";
        }
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
