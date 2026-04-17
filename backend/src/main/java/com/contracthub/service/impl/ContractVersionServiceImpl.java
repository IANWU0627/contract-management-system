package com.contracthub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.annotation.RequirePermission;
import com.contracthub.entity.ContractVersion;
import com.contracthub.entity.ContractVersionDiffAnalysis;
import com.contracthub.mapper.ContractVersionMapper;
import com.contracthub.mapper.ContractVersionDiffAnalysisMapper;
import com.contracthub.service.ContractVersionService;
import com.contracthub.util.ClauseCompareUtils;
import com.contracthub.util.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContractVersionServiceImpl implements ContractVersionService {

    @Autowired
    private ContractVersionMapper versionMapper;

    @Autowired
    private ContractVersionDiffAnalysisMapper diffAnalysisMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ContractVersion createVersion(Long contractId, String content, String changeDesc,
                                          Long operatorId, String operatorName) {
        String nextVersion = generateNextVersion(contractId);
        
        ContractVersion version = new ContractVersion();
        version.setContractId(contractId);
        version.setVersion(nextVersion);
        version.setContent(content);
        version.setChangeDesc(changeDesc);
        version.setOperatorId(operatorId);
        version.setOperatorName(operatorName);
        version.setCreatedAt(LocalDateTime.now());
        
        versionMapper.insert(version);
        return version;
    }

    @Override
    public List<ContractVersion> getVersionHistory(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at");
        return versionMapper.selectList(wrapper);
    }

    @Override
    public ContractVersion getVersionDetail(Long contractId, Long versionId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("id", versionId)
               .eq("contract_id", contractId);
        return versionMapper.selectOne(wrapper);
    }

    @Override
    public String getLatestVersion(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        ContractVersion latest = versionMapper.selectOne(wrapper);
        return latest != null ? latest.getVersion() : "v1.0";
    }

    @Override
    @Transactional
    @RequirePermission(value = "version:restore", description = "恢复历史版本")
    public Map<String, Object> restoreVersion(Long contractId, Long versionId,
                                               Long operatorId, String operatorName) {
        ContractVersion targetVersion = getVersionDetail(contractId, versionId);
        if (targetVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        String nextVersion = generateNextVersion(contractId);
        
        ContractVersion newVersion = new ContractVersion();
        newVersion.setContractId(contractId);
        newVersion.setVersion(nextVersion);
        newVersion.setContent(targetVersion.getContent());
        newVersion.setChangeDesc("从版本 " + targetVersion.getVersion() + " 恢复");
        newVersion.setOperatorId(operatorId);
        newVersion.setOperatorName(operatorName);
        newVersion.setCreatedAt(LocalDateTime.now());
        
        versionMapper.insert(newVersion);

        Map<String, Object> result = new HashMap<>();
        result.put("restoredVersion", targetVersion.getVersion());
        result.put("newVersion", nextVersion);
        result.put("content", targetVersion.getContent());
        return result;
    }

    @Override
    public Map<String, Object> compareVersions(Long contractId, Long versionId1, Long versionId2) {
        ContractVersion v1 = getVersionDetail(contractId, versionId1);
        ContractVersion v2 = getVersionDetail(contractId, versionId2);
        
        if (v1 == null || v2 == null) {
            throw new RuntimeException("版本不存在");
        }

        String content1 = v1.getContent() != null ? v1.getContent() : "";
        String content2 = v2.getContent() != null ? v2.getContent() : "";

        QueryWrapper<ContractVersionDiffAnalysis> query = new QueryWrapper<>();
        query.eq("contract_id", contractId)
                .eq("base_version_id", versionId1)
                .eq("target_version_id", versionId2)
                .orderByDesc("updated_at")
                .last("LIMIT 1");
        ContractVersionDiffAnalysis cached = diffAnalysisMapper.selectOne(query);
        if (cached != null && cached.getDiffJson() != null && !cached.getDiffJson().isBlank()) {
            return buildCompareResponse(v1, v2, cached);
        }

        List<Map<String, Object>> differences = calculateDiff(content1, content2);
        List<Map<String, Object>> clauseChanges = analyzeClauseChanges(content1, content2);
        int addedLines = countAddedLines(differences);
        int removedLines = countRemovedLines(differences);
        Map<String, Object> riskPayload = analyzeDiffRisk(differences, clauseChanges);
        String overallRisk = String.valueOf(riskPayload.getOrDefault("overallRisk", "LOW"));

        ContractVersionDiffAnalysis analysis = cached != null ? cached : new ContractVersionDiffAnalysis();
        analysis.setContractId(contractId);
        analysis.setBaseVersionId(versionId1);
        analysis.setTargetVersionId(versionId2);
        analysis.setOverallRisk(overallRisk);
        analysis.setModelName("rule-engine");
        analysis.setPromptVersion("v1");
        analysis.setCreatedBy(SecurityUtils.getCurrentUserId());
        analysis.setUpdatedAt(LocalDateTime.now());
        if (analysis.getCreatedAt() == null) {
            analysis.setCreatedAt(LocalDateTime.now());
        }
        try {
            Map<String, Object> diffPayload = new LinkedHashMap<>();
            diffPayload.put("differences", differences);
            diffPayload.put("clauseChanges", clauseChanges);
            diffPayload.put("addedLines", addedLines);
            diffPayload.put("removedLines", removedLines);
            analysis.setDiffJson(objectMapper.writeValueAsString(diffPayload));
            analysis.setRiskJson(objectMapper.writeValueAsString(riskPayload));
        } catch (Exception e) {
            analysis.setDiffJson("{}");
            analysis.setRiskJson("{}");
        }
        if (analysis.getId() == null) {
            diffAnalysisMapper.insert(analysis);
        } else {
            diffAnalysisMapper.updateById(analysis);
        }
        return buildCompareResponse(v1, v2, analysis);
    }

    @Override
    public String generateNextVersion(Long contractId) {
        QueryWrapper<ContractVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        ContractVersion latest = versionMapper.selectOne(wrapper);
        
        if (latest == null) {
            return "v1.0";
        }
        
        String currentVersion = latest.getVersion();
        try {
            String versionNum = currentVersion.replace("v", "");
            String[] parts = versionNum.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            
            minor++;
            if (minor >= 10) {
                major++;
                minor = 0;
            }
            return "v" + major + "." + minor;
        } catch (Exception e) {
            return "v1.0";
        }
    }

    private List<Map<String, Object>> calculateDiff(String content1, String content2) {
        List<Map<String, Object>> diff = new ArrayList<>();
        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");
        
        int i = 0, j = 0;
        while (i < lines1.length || j < lines2.length) {
            if (i < lines1.length && j < lines2.length && lines1[i].equals(lines2[j])) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "unchanged");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                item.put("lineNum2", j + 1);
                diff.add(item);
                i++;
                j++;
            } else if (j < lines2.length && (i >= lines1.length || !contains(lines1, lines2[j], i))) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "added");
                item.put("content", lines2[j]);
                item.put("lineNum2", j + 1);
                diff.add(item);
                j++;
            } else if (i < lines1.length) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "removed");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                diff.add(item);
                i++;
            }
        }
        return diff;
    }

    private boolean contains(String[] array, String target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i].equals(target)) return true;
        }
        return false;
    }

    private int countAddedLines(List<Map<String, Object>> differences) {
        return (int) differences.stream()
                .filter(d -> "added".equals(d.get("type")))
                .count();
    }

    private int countRemovedLines(List<Map<String, Object>> differences) {
        return (int) differences.stream()
                .filter(d -> "removed".equals(d.get("type")))
                .count();
    }

    private Map<String, Object> buildCompareResponse(ContractVersion v1, ContractVersion v2, ContractVersionDiffAnalysis analysis) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version1", v1.getVersion());
        result.put("version2", v2.getVersion());
        result.put("baseVersionId", v1.getId());
        result.put("targetVersionId", v2.getId());
        result.put("overallRisk", analysis.getOverallRisk());
        result.put("modelName", analysis.getModelName());
        result.put("promptVersion", analysis.getPromptVersion());
        result.put("analyzedAt", analysis.getUpdatedAt());

        try {
            Map<String, Object> diffJson = objectMapper.readValue(
                    analysis.getDiffJson() == null ? "{}" : analysis.getDiffJson(),
                    new TypeReference<Map<String, Object>>() {}
            );
            result.put("differences", diffJson.getOrDefault("differences", new ArrayList<>()));
            result.put("clauseChanges", diffJson.getOrDefault("clauseChanges", new ArrayList<>()));
            result.put("addedLines", diffJson.getOrDefault("addedLines", 0));
            result.put("removedLines", diffJson.getOrDefault("removedLines", 0));
        } catch (Exception e) {
            result.put("differences", new ArrayList<>());
            result.put("clauseChanges", new ArrayList<>());
            result.put("addedLines", 0);
            result.put("removedLines", 0);
        }
        try {
            Map<String, Object> riskJson = objectMapper.readValue(
                    analysis.getRiskJson() == null ? "{}" : analysis.getRiskJson(),
                    new TypeReference<Map<String, Object>>() {}
            );
            result.put("riskSummary", riskJson.getOrDefault("riskSummary", new LinkedHashMap<>()));
            result.put("riskItems", riskJson.getOrDefault("riskItems", new ArrayList<>()));
            result.put("aiCommentary", riskJson.getOrDefault("aiCommentary", ""));
        } catch (Exception e) {
            result.put("riskSummary", new LinkedHashMap<>());
            result.put("riskItems", new ArrayList<>());
            result.put("aiCommentary", "");
        }
        return result;
    }

    private Map<String, Object> analyzeDiffRisk(List<Map<String, Object>> differences, List<Map<String, Object>> clauseChanges) {
        List<Map<String, Object>> riskItems = new ArrayList<>();
        int high = 0;
        int medium = 0;
        int low = 0;

        for (Map<String, Object> item : differences) {
            String type = String.valueOf(item.getOrDefault("type", ""));
            if (!"added".equals(type) && !"removed".equals(type)) {
                continue;
            }
            String content = normalizeContent(String.valueOf(item.getOrDefault("content", "")));
            if (content.isBlank()) {
                continue;
            }
            String level = detectRiskLevel(content);
            if ("LOW".equals(level)) {
                continue;
            }
            Map<String, Object> risk = new LinkedHashMap<>();
            risk.put("level", level.toLowerCase(Locale.ROOT));
            risk.put("changeType", type);
            risk.put("title", detectRiskTitle(content));
            risk.put("titleKey", detectRiskTitleKey(content));
            risk.put("reason", detectRiskReason(content));
            risk.put("reasonKey", detectRiskReasonKey(content));
            risk.put("evidence", shorten(content, 120));
            risk.put("suggestion", detectSuggestion(content));
            risk.put("suggestionKey", detectSuggestionKey(content));
            riskItems.add(risk);
            if ("HIGH".equals(level)) {
                high++;
            } else {
                medium++;
            }
        }

        for (Map<String, Object> clause : clauseChanges) {
            String level = String.valueOf(clause.getOrDefault("riskLevel", "low")).toUpperCase(Locale.ROOT);
            if ("LOW".equals(level)) {
                continue;
            }
            Map<String, Object> risk = new LinkedHashMap<>();
            risk.put("level", level.toLowerCase(Locale.ROOT));
            risk.put("changeType", String.valueOf(clause.getOrDefault("changeType", "modified")).toLowerCase(Locale.ROOT));
            risk.put("title", String.valueOf(clause.getOrDefault("title", "条款变更")));
            risk.put("reason", String.valueOf(clause.getOrDefault("riskReason", "关键条款发生变化")));
            risk.put("reasonKey", String.valueOf(clause.getOrDefault("riskReasonKey", "contract.versionsDetail.reason.generic")));
            risk.put("evidence", shorten(String.valueOf(clause.getOrDefault("after", clause.getOrDefault("before", ""))), 120));
            risk.put("suggestion", detectSuggestion(String.valueOf(clause.getOrDefault("after", ""))));
            risk.put("suggestionKey", detectSuggestionKey(String.valueOf(clause.getOrDefault("after", ""))));
            riskItems.add(risk);
            if ("HIGH".equals(level)) {
                high++;
            } else {
                medium++;
            }
        }

        if (riskItems.isEmpty()) {
            Map<String, Object> risk = new LinkedHashMap<>();
            risk.put("level", "low");
            risk.put("changeType", "unchanged");
            risk.put("title", "未识别到高风险改动");
            risk.put("titleKey", "contract.versionsDetail.riskTitleText.none");
            risk.put("reason", "本次修改未命中高风险条款关键词，建议继续人工复核关键商务条款。");
            risk.put("reasonKey", "contract.versionsDetail.reason.none");
            risk.put("evidence", "");
            risk.put("suggestion", "保持付款、违约责任、争议解决条款的一致性。");
            risk.put("suggestionKey", "contract.versionsDetail.suggestionText.none");
            riskItems.add(risk);
            low = 1;
        }

        String overallRisk = high > 0 ? "HIGH" : (medium > 0 ? "MEDIUM" : "LOW");
        Map<String, Object> riskSummary = new LinkedHashMap<>();
        riskSummary.put("overallRisk", overallRisk.toLowerCase(Locale.ROOT));
        riskSummary.put("highCount", high);
        riskSummary.put("mediumCount", medium);
        riskSummary.put("lowCount", low);
        riskSummary.put("totalRiskItems", riskItems.size());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("overallRisk", overallRisk);
        payload.put("riskSummary", riskSummary);
        payload.put("riskItems", riskItems);
        payload.put("aiCommentary", buildCommentary(overallRisk, high, medium));
        payload.put("aiCommentaryKey", buildCommentaryKey(overallRisk, high, medium));
        return payload;
    }

    private List<Map<String, Object>> analyzeClauseChanges(String content1, String content2) {
        Map<String, String> clauses1 = ClauseCompareUtils.splitToClauseMap(content1, this::normalizeContent);
        Map<String, String> clauses2 = ClauseCompareUtils.splitToClauseMap(content2, this::normalizeContent);
        List<Map<String, Object>> changes = new ArrayList<>();
        Set<String> matchedRightTitles = new LinkedHashSet<>();

        for (Map.Entry<String, String> left : clauses1.entrySet()) {
            String leftTitle = left.getKey();
            String leftContent = left.getValue();
            String matchedTitle = ClauseCompareUtils.findMatchingTitle(leftTitle, clauses2, matchedRightTitles);
            if (matchedTitle == null) {
                matchedTitle = ClauseCompareUtils.findBestContentMatch(
                    leftContent, clauses2, matchedRightTitles, this::normalizeContent);
            }
            if (matchedTitle == null) {
                changes.add(buildClauseChange(leftTitle, "REMOVED", leftContent, ""));
                continue;
            }
            matchedRightTitles.add(matchedTitle);
            String rightContent = clauses2.get(matchedTitle);
            if (!Objects.equals(normalizeContent(leftContent), normalizeContent(rightContent))) {
                if (ClauseCompareUtils.isMinorChange(
                    leftContent, rightContent, this::normalizeContent, this::normalizeForSemanticCompare)) {
                    changes.add(buildClauseChange(
                        ClauseCompareUtils.displayRenamedTitle(leftTitle, matchedTitle), "MINOR", leftContent, rightContent));
                    continue;
                }
                String displayTitle = ClauseCompareUtils.displayRenamedTitle(leftTitle, matchedTitle);
                changes.add(buildClauseChange(displayTitle, "MODIFIED", leftContent, rightContent));
            }
        }

        for (Map.Entry<String, String> right : clauses2.entrySet()) {
            if (!matchedRightTitles.contains(right.getKey())) {
                changes.add(buildClauseChange(right.getKey(), "ADDED", "", right.getValue()));
            }
        }

        return changes;
    }

    private Map<String, Object> buildClauseChange(String title, String changeType, String before, String after) {
        String anchor = (after != null && !after.isBlank()) ? after : before;
        String riskLevel = detectRiskLevel(anchor).toLowerCase(Locale.ROOT);
        if ("MINOR".equalsIgnoreCase(changeType)) {
            riskLevel = "low";
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("changeType", changeType);
        item.put("before", shorten(normalizeContent(before), 200));
        item.put("after", shorten(normalizeContent(after), 200));
        item.put("riskLevel", riskLevel);
        item.put("riskReason", "MINOR".equalsIgnoreCase(changeType)
                ? "仅检测到格式或标点级调整，未识别到实质条款变化。"
                : detectRiskReason(anchor));
        item.put("riskReasonKey", "MINOR".equalsIgnoreCase(changeType)
                ? "contract.versionsDetail.reason.minor"
                : detectRiskReasonKey(anchor));
        return item;
    }

    private String normalizeForSemanticCompare(String content) {
        String normalized = normalizeContent(content).toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[\\p{Punct}\\p{IsPunctuation}\\s]+", "");
    }

    private String detectRiskLevel(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "违约金", "赔偿", "无限责任", "indemn", "liability", "termination", "解除", "争议", "仲裁", "管辖"))) {
            return "HIGH";
        }
        if (containsAny(text, List.of("付款", "预付款", "发票", "账期", "payment", "invoice", "交付", "验收", "acceptance", "保密", "confidential"))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String detectRiskTitle(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "违约与责任条款变更";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "争议解决条款变更";
        }
        if (containsAny(text, List.of("付款", "预付款", "发票", "payment", "invoice"))) {
            return "付款与结算条款变更";
        }
        if (containsAny(text, List.of("交付", "验收", "delivery", "acceptance"))) {
            return "交付与验收条款变更";
        }
        return "关键条款发生变更";
    }

    private String detectRiskReason(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "责任边界发生变化，可能显著影响违约成本与索赔风险。";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "争议处理路径变化会直接影响诉讼/仲裁成本与执行效率。";
        }
        if (containsAny(text, List.of("付款", "预付款", "发票", "payment", "invoice"))) {
            return "结算节点或条件变化可能带来现金流与回款风险。";
        }
        return "条款措辞发生调整，建议结合业务背景进行人工复核。";
    }

    private String detectRiskReasonKey(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.reason.liability";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.reason.dispute";
        }
        if (containsAny(text, List.of("付款", "预付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.reason.payment";
        }
        return "contract.versionsDetail.reason.generic";
    }

    private String detectSuggestion(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "建议法务确认责任上限、违约金比例及免责边界。";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "建议统一争议解决方式与管辖地，避免跨法域执行风险。";
        }
        if (containsAny(text, List.of("付款", "发票", "payment", "invoice"))) {
            return "建议复核付款条件、发票类型与逾期责任是否与商务条款一致。";
        }
        return "建议将该改动纳入审批意见并要求提交方补充说明。";
    }

    private String detectSuggestionKey(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.suggestionText.liability";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.suggestionText.dispute";
        }
        if (containsAny(text, List.of("付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.suggestionText.payment";
        }
        return "contract.versionsDetail.suggestionText.generic";
    }

    private String detectRiskTitleKey(String content) {
        String text = content.toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.riskTopic.liability";
        }
        if (containsAny(text, List.of("争议", "仲裁", "管辖", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.riskTopic.dispute";
        }
        if (containsAny(text, List.of("付款", "预付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.riskTopic.payment";
        }
        if (containsAny(text, List.of("交付", "验收", "delivery", "acceptance"))) {
            return "contract.versionsDetail.riskTopic.delivery";
        }
        return "contract.versionsDetail.riskTopic.generic";
    }

    private String buildCommentary(String overallRisk, int high, int medium) {
        if ("HIGH".equals(overallRisk)) {
            return "本次版本修改包含高风险条款，请优先复核违约责任、争议解决与付款条件。";
        }
        if ("MEDIUM".equals(overallRisk)) {
            return "本次版本修改包含中风险变更，建议在审批意见中要求补充业务与法务说明。";
        }
        if (high == 0 && medium == 0) {
            return "本次版本未识别到明显高风险条款变更，但仍建议人工抽检关键条款。";
        }
        return "版本差异已完成分析，请结合业务背景进行最终判断。";
    }

    private String buildCommentaryKey(String overallRisk, int high, int medium) {
        if ("HIGH".equals(overallRisk)) {
            return "contract.versionsDetail.commentary.high";
        }
        if ("MEDIUM".equals(overallRisk)) {
            return "contract.versionsDetail.commentary.medium";
        }
        if (high == 0 && medium == 0) {
            return "contract.versionsDetail.commentary.low";
        }
        return "contract.versionsDetail.commentary.generic";
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        return content
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
