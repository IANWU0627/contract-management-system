package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.entity.ContractCategory;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.contracthub.mapper.ContractCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 合同类型字段配置 Controller
 */
@RestController
@RequestMapping("/api/contract-type-fields")
public class ContractTypeFieldController {

    private final ContractTypeFieldMapper fieldMapper;
    private final ContractCategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    public ContractTypeFieldController(ContractTypeFieldMapper fieldMapper, ContractCategoryMapper categoryMapper) {
        this.fieldMapper = fieldMapper;
        this.categoryMapper = categoryMapper;
        this.objectMapper = new ObjectMapper();
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
            String type = (String) row.get("contractType");
            Long count = (Long) row.get("count");
            countMap.put(type, count);
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
            String type = (String) row.get("contractType");
            Long count = (Long) row.get("count");
            counts.put(type, count.intValue());
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

    /**
     * 创建字段
     */
    @PostMapping
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
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
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ApiResponse<Void> updateField(@PathVariable Long id, @RequestBody ContractTypeField field) {
        ContractTypeField existing = fieldMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.error("字段不存在");
        }
        
        field.setId(id);
        fieldMapper.updateById(field);
        return ApiResponse.success(null);
    }

    /**
     * 删除字段
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ApiResponse<Void> deleteField(@PathVariable Long id) {
        fieldMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 批量创建字段
     */
    @PostMapping("/batch")
    @CacheEvict(value = {"contractTypeFields", "contractTypes"}, allEntries = true)
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
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
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
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
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
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
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
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
}
