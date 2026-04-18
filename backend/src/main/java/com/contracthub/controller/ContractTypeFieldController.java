package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.entity.ContractCategory;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.contracthub.mapper.ContractCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.*;

/**
 * 合同类型字段配置 Controller
 */
@RestController
@RequestMapping("/api/contract-type-fields")
public class ContractTypeFieldController {

    private final ContractTypeFieldMapper fieldMapper;
    private final ContractCategoryMapper categoryMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ContractTypeFieldController(
            ContractTypeFieldMapper fieldMapper,
            ContractCategoryMapper categoryMapper,
            JdbcTemplate jdbcTemplate) {
        this.fieldMapper = fieldMapper;
        this.categoryMapper = categoryMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 轻量级接口：获取合同表单字段配置（仅返回字段列表）
     */
    @GetMapping("/form/{contractType}")
    @Cacheable(value = "contractTypeFields", key = "'form:' + #contractType")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<ContractTypeField>> getFormFields(@PathVariable String contractType) {
        List<ContractTypeField> fields = fieldMapper.selectByContractType(contractType);
        return ApiResponse.success(fields);
    }

    /**
     * 获取字段列表
     */
    @GetMapping
    @Cacheable(value = "contractTypeFields", key = "#contractType ?: 'all'")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getFields(@RequestParam(required = false) String contractType) {
        List<ContractTypeField> fields;
        if (contractType != null && !contractType.isEmpty()) {
            fields = fieldMapper.selectByContractType(contractType);
        } else {
            fields = fieldMapper.selectList(
                new QueryWrapper<ContractTypeField>().orderByAsc("contract_type", "field_order")
            );
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", fields);
        result.put("total", fields.size());
        
        return ApiResponse.success(result);
    }

    /**
     * 获取所有合同分类（带字段数量）
     */
    @GetMapping("/types")
    @Cacheable(value = "contractTypes")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> getContractTypes() {
        // 获取所有启用的分类
        QueryWrapper<ContractCategory> categoryWrapper = new QueryWrapper<>();
        categoryWrapper.eq("active", true);
        categoryWrapper.orderByAsc("sort_order");
        List<ContractCategory> categories = categoryMapper.selectList(categoryWrapper);
        
        // 获取每个分类的字段数量
        List<Map<String, Object>> rawCounts = fieldMapper.selectCountsByType();
        Map<String, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rawCounts) {
            String type = mapRowString(row, "contractType", "contract_type");
            if (type == null) {
                continue;
            }
            countMap.put(type, mapRowCount(row));
        }
        
        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContractCategory cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", cat.getCode());
            item.put("name", cat.getName());
            item.put("color", cat.getColor());
            item.put("fieldCount", countMap.getOrDefault(cat.getCode(), 0L));
            result.add(item);
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取每个分类的字段数量
     */
    @GetMapping("/counts")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Integer>> getFieldCounts() {
        List<Map<String, Object>> rawCounts = fieldMapper.selectCountsByType();
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : rawCounts) {
            String type = mapRowString(row, "contractType", "contract_type");
            if (type == null) {
                continue;
            }
            long cnt = mapRowCount(row);
            counts.put(type, (int) Math.min(cnt, Integer.MAX_VALUE));
        }
        return ApiResponse.success(counts);
    }

    /**
     * 按分类获取字段配置
     */
    @GetMapping("/config/{contractType}")
    @Cacheable(value = "contractTypeFields", key = "'config:' + #contractType")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getConfigByType(
            @PathVariable String contractType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        
        // 获取分类信息
        QueryWrapper<ContractCategory> catWrapper = new QueryWrapper<>();
        catWrapper.eq("code", contractType);
        ContractCategory category = categoryMapper.selectOne(catWrapper);
        
        // 获取字段
        List<ContractTypeField> allFields = fieldMapper.selectByContractType(contractType);
        int total = allFields.size();
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ContractTypeField> fields = fromIndex < total 
            ? allFields.subList(fromIndex, toIndex) 
            : Collections.emptyList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("contractType", contractType);
        if (category != null) {
            result.put("categoryName", category.getName());
            result.put("categoryColor", category.getColor());
        }
        result.put("fields", fields);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return ApiResponse.success(result);
    }

    @GetMapping("/draft/{contractType}")
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Map<String, Object>> getDraftByType(@PathVariable String contractType) {
        try {
            ensureDraftInitialized(contractType);
            Map<String, Object> row = loadDraftRowOrInit(contractType);

            List<ContractTypeField> draftFields;
            try {
                draftFields = parseDraftFields(draftCellToJsonString(row.get("fields_json")));
            } catch (Exception e) {
                draftFields = new ArrayList<>();
            }
            if (draftFields.isEmpty()) {
                List<ContractTypeField> liveFields = fieldMapper.selectByContractType(contractType);
                if (!liveFields.isEmpty()) {
                    draftFields = liveFields;
                    try {
                        String fieldsJson = objectMapper.writeValueAsString(draftFields);
                        jdbcTemplate.update(
                                "UPDATE contract_type_field_draft SET fields_json = ?, draft_updated_at = NOW() WHERE contract_type = ?",
                                fieldsJson,
                                contractType
                        );
                    } catch (Exception e) {
                        return ApiResponse.error("草稿初始化失败: " + e.getMessage());
                    }
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("contractType", contractType);
            result.put("fields", draftFields);
            result.put("total", draftFields.size());
            result.put("draftUpdatedAt", row.get("draft_updated_at"));
            result.put("publishedAt", row.get("published_at"));
            result.put("publishVersion", row.get("publish_version"));
            result.put("hasDraft", true);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("加载草稿失败: " + e.getMessage());
        }
    }

    /** 读取草稿行；若因并发等原因无行则再初始化一次，仍无则 UPSERT 空数组后再读 */
    private Map<String, Object> loadDraftRowOrInit(String contractType) {
        try {
            return queryDraftRow(contractType);
        } catch (EmptyResultDataAccessException e) {
            ensureDraftInitialized(contractType);
            try {
                return queryDraftRow(contractType);
            } catch (EmptyResultDataAccessException e2) {
                jdbcTemplate.update(
                        """
                        INSERT INTO contract_type_field_draft(contract_type, fields_json, draft_updated_at, publish_version)
                        VALUES(?, '[]', NOW(), 0)
                        ON DUPLICATE KEY UPDATE contract_type = contract_type
                        """,
                        contractType);
                return queryDraftRow(contractType);
            }
        }
    }

    private Map<String, Object> queryDraftRow(String contractType) {
        return jdbcTemplate.queryForMap(
                "SELECT contract_type, fields_json, draft_updated_at, published_at, publish_version FROM contract_type_field_draft WHERE contract_type = ?",
                contractType);
    }

    /**
     * MySQL 驱动可能将 LONGTEXT 以 String、byte[] 或 Clob 等形式返回，强转为 String 会 ClassCastException 导致 500。
     */
    private static String draftCellToJsonString(Object cell) {
        if (cell == null) {
            return null;
        }
        if (cell instanceof String s) {
            return s;
        }
        if (cell instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (cell instanceof Clob clob) {
            try {
                long len = clob.length();
                if (len == 0) {
                    return "";
                }
                if (len > Integer.MAX_VALUE - 1) {
                    throw new IllegalStateException("草稿 fields_json 过大");
                }
                return clob.getSubString(1, (int) len);
            } catch (Exception e) {
                throw new IllegalStateException("读取草稿 fields_json 失败", e);
            }
        }
        return cell.toString();
    }

    @PutMapping("/draft/{contractType}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Map<String, Object>> saveDraftByType(
            @PathVariable String contractType,
            @RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fieldMaps = (List<Map<String, Object>>) payload.get("fields");
        if (fieldMaps == null) {
            return ApiResponse.error("草稿字段不能为空");
        }
        List<ContractTypeField> draftFields = parseDraftFieldMaps(fieldMaps);
        for (int i = 0; i < draftFields.size(); i++) {
            ContractTypeField field = draftFields.get(i);
            field.setContractType(contractType);
            if (field.getFieldOrder() == null) {
                field.setFieldOrder(i);
            }
        }

        String fieldsJson;
        try {
            fieldsJson = objectMapper.writeValueAsString(draftFields);
        } catch (Exception e) {
            return ApiResponse.error("草稿序列化失败");
        }

        int updated = jdbcTemplate.update(
                "UPDATE contract_type_field_draft SET fields_json = ?, draft_updated_at = NOW() WHERE contract_type = ?",
                fieldsJson, contractType);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO contract_type_field_draft(contract_type, fields_json, draft_updated_at, publish_version) VALUES(?, ?, NOW(), 0)",
                    contractType, fieldsJson);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("contractType", contractType);
        result.put("total", draftFields.size());
        result.put("draftUpdatedAt", new Date());
        return ApiResponse.success(result);
    }

    @DeleteMapping("/draft/{contractType}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Void> discardDraft(@PathVariable String contractType) {
        List<ContractTypeField> liveFields = fieldMapper.selectByContractType(contractType);
        String fieldsJson;
        try {
            fieldsJson = objectMapper.writeValueAsString(liveFields);
        } catch (Exception e) {
            return ApiResponse.error("重置草稿失败");
        }
        int updated = jdbcTemplate.update(
                "UPDATE contract_type_field_draft SET fields_json = ?, draft_updated_at = NOW() WHERE contract_type = ?",
                fieldsJson, contractType);
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO contract_type_field_draft(contract_type, fields_json, draft_updated_at, publish_version) VALUES(?, ?, NOW(), 0)",
                    contractType, fieldsJson);
        }
        return ApiResponse.success(null);
    }

    @PostMapping("/publish/{contractType}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Map<String, Object>> publishDraft(@PathVariable String contractType) {
        ensureDraftInitialized(contractType);
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT fields_json, publish_version FROM contract_type_field_draft WHERE contract_type = ?",
                contractType);
        String fieldsJsonStr = draftCellToJsonString(row.get("fields_json"));
        List<ContractTypeField> draftFields = parseDraftFields(fieldsJsonStr);

        QueryWrapper<ContractTypeField> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("contract_type", contractType);
        fieldMapper.delete(deleteWrapper);

        for (int i = 0; i < draftFields.size(); i++) {
            ContractTypeField field = draftFields.get(i);
            field.setId(null);
            field.setContractType(contractType);
            field.setFieldOrder(i);
            if (!"select".equals(field.getFieldType()) && !"multiselect".equals(field.getFieldType())) {
                field.setQuickCodeId(null);
            }
            fieldMapper.insert(field);
        }

        jdbcTemplate.update(
                "UPDATE contract_type_field_draft SET published_at = NOW(), publish_version = publish_version + 1 WHERE contract_type = ?",
                contractType);

        long prevVersion = 0L;
        Object pv = row.get("publish_version");
        if (pv instanceof Number n) {
            prevVersion = n.longValue();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("contractType", contractType);
        result.put("publishedCount", draftFields.size());
        result.put("publishVersion", (int) Math.min(prevVersion + 1L, Integer.MAX_VALUE));
        return ApiResponse.success(result);
    }

    /**
     * 创建字段
     */
    @PostMapping
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<ContractTypeField> createField(@RequestBody ContractTypeField field) {
        if (fieldMapper.countByTypeAndKey(field.getContractType(), field.getFieldKey()) > 0) {
            return ApiResponse.error("该字段已存在");
        }
        
        fieldMapper.insert(field);
        return ApiResponse.success(field);
    }

    /**
     * 更新字段
     */
    @PutMapping("/{id}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Void> updateField(@PathVariable Long id, @RequestBody ContractTypeField field) {
        try {
            ContractTypeField existing = fieldMapper.selectById(id);
            if (existing == null) {
                return ApiResponse.error("字段不存在");
            }

            // 合并更新：防止前端只传部分字段时把必填列覆盖为 null 导致保存失败
            existing.setFieldLabel(field.getFieldLabel() != null ? field.getFieldLabel() : existing.getFieldLabel());
            existing.setFieldLabelEn(field.getFieldLabelEn() != null ? field.getFieldLabelEn() : existing.getFieldLabelEn());
            existing.setFieldType(field.getFieldType() != null ? field.getFieldType() : existing.getFieldType());
            existing.setRequired(field.getRequired() != null ? field.getRequired() : existing.getRequired());
            existing.setShowInList(field.getShowInList() != null ? field.getShowInList() : existing.getShowInList());
            existing.setShowInForm(field.getShowInForm() != null ? field.getShowInForm() : existing.getShowInForm());
            existing.setFieldOrder(field.getFieldOrder() != null ? field.getFieldOrder() : existing.getFieldOrder());

            if (field.getPlaceholder() != null) existing.setPlaceholder(field.getPlaceholder());
            if (field.getPlaceholderEn() != null) existing.setPlaceholderEn(field.getPlaceholderEn());
            if (field.getDefaultValue() != null) existing.setDefaultValue(field.getDefaultValue());
            if (field.getOptions() != null) existing.setOptions(field.getOptions());
            if (field.getMinValue() != null) existing.setMinValue(field.getMinValue());
            if (field.getMaxValue() != null) existing.setMaxValue(field.getMaxValue());

            // select/multiselect 才使用 quickCodeId；其它类型强制清空，避免脏数据
            String effectiveType = existing.getFieldType();
            if ("select".equals(effectiveType) || "multiselect".equals(effectiveType)) {
                if (field.getQuickCodeId() != null) {
                    existing.setQuickCodeId(field.getQuickCodeId());
                }
            } else {
                existing.setQuickCodeId(null);
            }

            // 确保关键字段不丢失
            if (existing.getContractType() == null || existing.getContractType().isBlank()
                    || existing.getFieldKey() == null || existing.getFieldKey().isBlank()
                    || existing.getFieldLabel() == null || existing.getFieldLabel().isBlank()) {
                return ApiResponse.error("字段配置不完整，无法保存");
            }

            existing.setId(id);
            try {
                fieldMapper.updateById(existing);
            } catch (Exception ex) {
                String message = ex.getMessage() != null ? ex.getMessage() : "";
                if (message.contains("quick_code_id")) {
                    return ApiResponse.error("当前数据库缺少 quick_code_id 字段，请先完成数据库迁移");
                }
                throw ex;
            }
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除字段
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Void> deleteField(@PathVariable Long id) {
        fieldMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 批量创建字段
     */
    @PostMapping("/batch")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Void> batchCreate(@RequestBody Map<String, Object> data) {
        String contractType = (String) data.get("contractType");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) data.get("fields");
        
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                Map<String, Object> fieldData = fields.get(i);
                ContractTypeField field = new ContractTypeField();
                field.setContractType(contractType);
                field.setFieldKey((String) fieldData.get("fieldKey"));
                field.setFieldLabel((String) fieldData.get("fieldLabel"));
                field.setFieldLabelEn((String) fieldData.get("fieldLabelEn"));
                field.setFieldType((String) fieldData.getOrDefault("fieldType", "text"));
                field.setRequired(fieldData.get("required") != null && (Boolean) fieldData.get("required"));
                field.setShowInList(fieldData.get("showInList") != null ? (Boolean) fieldData.get("showInList") : true);
                field.setShowInForm(fieldData.get("showInForm") != null ? (Boolean) fieldData.get("showInForm") : true);
                field.setFieldOrder(i);
                field.setPlaceholder((String) fieldData.get("placeholder"));
                field.setPlaceholderEn((String) fieldData.get("placeholderEn"));
                field.setDefaultValue((String) fieldData.get("defaultValue"));
                field.setOptions(fieldData.get("options") != null ? fieldData.get("options").toString() : null);
                
                if (fieldMapper.countByTypeAndKey(contractType, field.getFieldKey()) == 0) {
                    fieldMapper.insert(field);
                }
            }
        }
        
        return ApiResponse.success(null);
    }

    /**
     * 删除某类型的所有字段
     */
    @DeleteMapping("/type/{contractType}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Void> deleteByType(@PathVariable String contractType) {
        QueryWrapper<ContractTypeField> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_type", contractType);
        fieldMapper.delete(wrapper);
        return ApiResponse.success(null);
    }
    
    /**
     * 导出字段配置
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public void exportFields(@RequestParam(required = false) String contractType, HttpServletResponse response) throws IOException {
        List<ContractTypeField> fields;
        String fileName;
        
        if (contractType != null && !contractType.isEmpty()) {
            fields = fieldMapper.selectByContractType(contractType);
            fileName = "field_config_" + contractType + ".json";
        } else {
            fields = fieldMapper.selectList(new QueryWrapper<ContractTypeField>().orderByAsc("contract_type", "field_order"));
            fileName = "field_config_all.json";
        }
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("version", "1.0");
        exportData.put("exportTime", new Date().toString());
        exportData.put("contractType", contractType != null ? contractType : "ALL");
        exportData.put("fieldCount", fields.size());
        exportData.put("fields", fields);
        
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", 
            "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        
        response.getWriter().write(objectMapper.writeValueAsString(exportData));
    }
    
    /**
     * 导入字段配置
     */
    @PostMapping("/import")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAnyAuthority('CONTRACT_MANAGE','CATEGORY_MANAGE')")
    public ApiResponse<Map<String, Object>> importFields(@RequestBody Map<String, Object> importData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) importData.get("fields");
            if (fields == null || fields.isEmpty()) {
                return ApiResponse.error("导入数据为空");
            }
            
            int imported = 0;
            int skipped = 0;
            int updated = 0;
            
            for (Map<String, Object> fieldData : fields) {
                String fieldContractType = (String) fieldData.get("contractType");
                String fieldKey = (String) fieldData.get("fieldKey");
                
                if (fieldContractType == null || fieldKey == null) {
                    skipped++;
                    continue;
                }
                
                ContractTypeField field = new ContractTypeField();
                field.setContractType(fieldContractType);
                field.setFieldKey(fieldKey);
                field.setFieldLabel((String) fieldData.get("fieldLabel"));
                field.setFieldLabelEn((String) fieldData.get("fieldLabelEn"));
                field.setFieldType((String) fieldData.getOrDefault("fieldType", "text"));
                field.setRequired(fieldData.get("required") != null && (Boolean) fieldData.get("required"));
                field.setShowInList(fieldData.get("showInList") != null ? (Boolean) fieldData.get("showInList") : true);
                field.setShowInForm(fieldData.get("showInForm") != null ? (Boolean) fieldData.get("showInForm") : true);
                field.setFieldOrder(fieldData.get("fieldOrder") != null ? ((Number) fieldData.get("fieldOrder")).intValue() : 0);
                field.setPlaceholder((String) fieldData.get("placeholder"));
                field.setPlaceholderEn((String) fieldData.get("placeholderEn"));
                field.setDefaultValue((String) fieldData.get("defaultValue"));
                if (fieldData.get("options") != null) {
                    field.setOptions(fieldData.get("options").toString());
                }
                
                if (fieldMapper.countByTypeAndKey(fieldContractType, fieldKey) > 0) {
                    QueryWrapper<ContractTypeField> wrapper = new QueryWrapper<>();
                    wrapper.eq("contract_type", fieldContractType).eq("field_key", fieldKey);
                    ContractTypeField existing = fieldMapper.selectList(wrapper).stream().findFirst().orElse(null);
                    if (existing != null) {
                        field.setId(existing.getId());
                        fieldMapper.updateById(field);
                        updated++;
                    }
                } else {
                    fieldMapper.insert(field);
                    imported++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("imported", imported);
            result.put("updated", updated);
            result.put("skipped", skipped);
            result.put("total", fields.size());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("导入失败: " + e.getMessage());
        }
    }

    private void ensureDraftInitialized(String contractType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM contract_type_field_draft WHERE contract_type = ?",
                Integer.class,
                contractType);
        if (count != null && count > 0) {
            return;
        }
        List<ContractTypeField> liveFields = fieldMapper.selectByContractType(contractType);
        String fieldsJson = "[]";
        try {
            fieldsJson = objectMapper.writeValueAsString(liveFields);
        } catch (Exception e) {
            throw new IllegalStateException("草稿初始化序列化失败", e);
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO contract_type_field_draft(contract_type, fields_json, draft_updated_at, publish_version) VALUES(?, ?, NOW(), 0)",
                    contractType,
                    fieldsJson);
        } catch (DataIntegrityViolationException e) {
            // 并发下可能同时插入同一 contract_type，忽略重复即可
        }
    }

    private List<ContractTypeField> parseDraftFields(String fieldsJson) {
        if (fieldsJson == null || fieldsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(fieldsJson, new TypeReference<List<ContractTypeField>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("草稿字段格式错误，无法解析", e);
        }
    }

    private List<ContractTypeField> parseDraftFieldMaps(List<Map<String, Object>> fieldMaps) {
        List<ContractTypeField> result = new ArrayList<>();
        for (Map<String, Object> item : fieldMaps) {
            ContractTypeField field = new ContractTypeField();
            field.setId(parseLong(item.get("id")));
            field.setContractType(parseString(item.get("contractType")));
            field.setFieldKey(parseString(item.get("fieldKey")));
            field.setFieldLabel(parseString(item.get("fieldLabel")));
            field.setFieldLabelEn(parseString(item.get("fieldLabelEn")));
            field.setFieldType(parseString(item.get("fieldType")));
            field.setQuickCodeId(parseString(item.get("quickCodeId")));
            field.setRequired(parseBoolean(item.get("required")));
            field.setShowInList(parseBoolean(item.get("showInList")));
            field.setShowInForm(parseBoolean(item.get("showInForm")));
            field.setFieldOrder(parseInteger(item.get("fieldOrder")));
            field.setPlaceholder(parseString(item.get("placeholder")));
            field.setPlaceholderEn(parseString(item.get("placeholderEn")));
            field.setDefaultValue(parseString(item.get("defaultValue")));
            field.setOptions(parseString(item.get("options")));
            field.setMinValue(parseBigDecimal(item.get("minValue")));
            field.setMaxValue(parseBigDecimal(item.get("maxValue")));
            result.add(field);
        }
        return result;
    }

    private String parseString(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    private Long parseLong(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(String.valueOf(raw));
        } catch (Exception ex) {
            throw new IllegalArgumentException("id 格式非法: " + raw, ex);
        }
    }

    private Integer parseInteger(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.intValue();
        try {
            return Integer.valueOf(String.valueOf(raw));
        } catch (Exception ex) {
            throw new IllegalArgumentException("fieldOrder 格式非法: " + raw, ex);
        }
    }

    private Boolean parseBoolean(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Boolean b) return b;
        String text = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
        if ("1".equals(text) || "true".equals(text) || "yes".equals(text)) return true;
        if ("0".equals(text) || "false".equals(text) || "no".equals(text)) return false;
        return null;
    }

    private BigDecimal parseBigDecimal(Object raw) {
        if (raw == null) return null;
        if (raw instanceof BigDecimal bd) return bd;
        if (raw instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(raw));
        } catch (Exception ex) {
            throw new IllegalArgumentException("数值格式非法: " + raw, ex);
        }
    }

    /** MyBatis 对 COUNT(*) 可能返回 Long / BigInteger 等，禁止强转为 Long */
    private static String mapRowString(Map<String, Object> row, String... keys) {
        for (String k : keys) {
            Object v = row.get(k);
            if (v != null) {
                return String.valueOf(v);
            }
        }
        return null;
    }

    private static long mapRowCount(Map<String, Object> row) {
        Object v = row.get("count");
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }
}
