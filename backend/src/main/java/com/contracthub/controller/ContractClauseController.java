package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractClause;
import com.contracthub.entity.ContractTemplate;
import com.contracthub.mapper.ContractClauseMapper;
import com.contracthub.mapper.ContractTemplateMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clauses")
@PreAuthorize("hasAuthority('TEMPLATE_MANAGE')")
public class ContractClauseController {

    private final ContractClauseMapper contractClauseMapper;
    private final ContractTemplateMapper contractTemplateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractClauseController(ContractClauseMapper contractClauseMapper,
                                    ContractTemplateMapper contractTemplateMapper) {
        this.contractClauseMapper = contractClauseMapper;
        this.contractTemplateMapper = contractTemplateMapper;
    }

    @GetMapping
    public ApiResponse<List<ContractClause>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status
    ) {
        String normalizedKeyword = keyword != null ? keyword.trim() : null;
        String normalizedCategory = category != null ? category.trim() : null;
        LambdaQueryWrapper<ContractClause> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .and(normalizedKeyword != null && !normalizedKeyword.isEmpty(), wrapper -> wrapper
                        .like(ContractClause::getName, normalizedKeyword)
                        .or()
                        .like(ContractClause::getCode, normalizedKeyword)
                        .or()
                        .like(ContractClause::getNameEn, normalizedKeyword))
                .eq(normalizedCategory != null && !normalizedCategory.isEmpty(), ContractClause::getCategory, normalizedCategory)
                .eq(status != null, ContractClause::getStatus, status)
                .orderByAsc(ContractClause::getSortOrder)
                .orderByDesc(ContractClause::getUpdatedAt);
        return ApiResponse.success(contractClauseMapper.selectList(queryWrapper));
    }

    @GetMapping("/references")
    public ApiResponse<Map<Long, Map<String, Object>>> getClauseReferences() {
        List<ContractClause> clauses = contractClauseMapper.selectList(
                new LambdaQueryWrapper<ContractClause>().select(ContractClause::getId, ContractClause::getCode)
        );
        List<ContractTemplate> templates = contractTemplateMapper.selectList(
                new LambdaQueryWrapper<ContractTemplate>().select(
                        ContractTemplate::getId,
                        ContractTemplate::getName,
                        ContractTemplate::getCategory,
                        ContractTemplate::getVariables,
                        ContractTemplate::getUpdatedAt
                )
        );

        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        Map<String, Long> clauseCodeToId = new HashMap<>();
        for (ContractClause clause : clauses) {
            clauseCodeToId.put(clause.getCode(), clause.getId());
            Map<String, Object> initial = new HashMap<>();
            initial.put("count", 0);
            initial.put("templates", new ArrayList<Map<String, Object>>());
            result.put(clause.getId(), initial);
        }

        for (ContractTemplate template : templates) {
            Map<String, Object> variables = parseVariables(template.getVariables());
            List<String> sourceCodes = extractSourceClauseCodes(variables);
            for (String code : sourceCodes) {
                Long clauseId = clauseCodeToId.get(code);
                if (clauseId == null) {
                    continue;
                }

                Map<String, Object> item = result.get(clauseId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> referencedTemplates = (List<Map<String, Object>>) item.get("templates");
                Map<String, Object> templateMap = new HashMap<>();
                templateMap.put("id", template.getId());
                templateMap.put("name", template.getName());
                templateMap.put("category", template.getCategory());
                templateMap.put("updatedAt", template.getUpdatedAt());
                referencedTemplates.add(templateMap);
                item.put("count", referencedTemplates.size());
            }
        }

        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<ContractClause> getById(@PathVariable Long id) {
        return ApiResponse.success(contractClauseMapper.selectById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ContractClause clause) {
        normalizeClause(clause);
        String duplicateReason = validateDuplicate(clause, null);
        if (duplicateReason != null) {
            return ApiResponse.error(duplicateReason);
        }
        if (clause.getStatus() == null) {
            clause.setStatus(1);
        }
        if (clause.getSortOrder() == null) {
            clause.setSortOrder(0);
        }
        try {
            contractClauseMapper.insert(clause);
        } catch (DuplicateKeyException e) {
            return ApiResponse.error("条款编码已存在");
        }
        return ApiResponse.success(clause.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody ContractClause clause) {
        clause.setId(id);
        normalizeClause(clause);
        String duplicateReason = validateDuplicate(clause, id);
        if (duplicateReason != null) {
            return ApiResponse.error(duplicateReason);
        }
        return ApiResponse.success(contractClauseMapper.updateById(clause) > 0);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.success(contractClauseMapper.deleteById(id) > 0);
    }

    private Map<String, Object> parseVariables(String variablesJson) {
        if (variablesJson == null || variablesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(variablesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<String> extractSourceClauseCodes(Map<String, Object> variables) {
        List<String> codes = new ArrayList<>();
        Object singleCode = variables.get("sourceClauseCode");
        if (singleCode instanceof String code && !code.isBlank()) {
            codes.add(code.trim());
        }

        Object multipleCodes = variables.get("sourceClauseCodes");
        if (multipleCodes instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String code && !code.isBlank() && !codes.contains(code.trim())) {
                    codes.add(code.trim());
                }
            }
        }
        return codes;
    }

    private void normalizeClause(ContractClause clause) {
        if (clause == null) {
            return;
        }
        clause.setCode(trimToNull(clause.getCode()));
        clause.setName(trimToNull(clause.getName()));
        clause.setCategory(trimToNull(clause.getCategory()));
        clause.setContent(trimToNull(clause.getContent()));
        clause.setDescription(trimToNull(clause.getDescription()));
        String nameEn = trimToNull(clause.getNameEn());
        if (nameEn == null) {
            if (clause.getCode() != null && !clause.getCode().isBlank()) {
                nameEn = clause.getCode();
            } else if (clause.getName() != null && !clause.getName().isBlank()) {
                nameEn = clause.getName();
            }
        }
        clause.setNameEn(nameEn);
    }

    private String validateDuplicate(ContractClause clause, Long excludeId) {
        if (clause == null) {
            return null;
        }
        if (clause.getCode() != null) {
            LambdaQueryWrapper<ContractClause> codeWrapper = new LambdaQueryWrapper<ContractClause>()
                    .eq(ContractClause::getCode, clause.getCode())
                    .ne(excludeId != null, ContractClause::getId, excludeId)
                    .last("LIMIT 1");
            if (contractClauseMapper.selectOne(codeWrapper) != null) {
                return "条款编码已存在";
            }
        }

        if (clause.getName() != null) {
            LambdaQueryWrapper<ContractClause> nameWrapper = new LambdaQueryWrapper<ContractClause>()
                    .eq(ContractClause::getName, clause.getName())
                    .eq(clause.getCategory() != null, ContractClause::getCategory, clause.getCategory())
                    .ne(excludeId != null, ContractClause::getId, excludeId)
                    .last("LIMIT 1");
            if (contractClauseMapper.selectOne(nameWrapper) != null) {
                return "同分类下条款名称已存在";
            }
        }

        if (clause.getContent() != null) {
            LambdaQueryWrapper<ContractClause> contentWrapper = new LambdaQueryWrapper<ContractClause>()
                    .eq(ContractClause::getContent, clause.getContent())
                    .ne(excludeId != null, ContractClause::getId, excludeId)
                    .last("LIMIT 1");
            if (contractClauseMapper.selectOne(contentWrapper) != null) {
                return "条款内容已存在，请勿重复保存";
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
