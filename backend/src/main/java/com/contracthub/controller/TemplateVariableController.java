package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.dto.TemplateVariableCreateRequest;
import com.contracthub.dto.TemplateVariableUpdateRequest;
import com.contracthub.entity.ContractTemplate;
import com.contracthub.entity.TemplateVariable;
import com.contracthub.mapper.ContractTemplateMapper;
import com.contracthub.mapper.TemplateVariableMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/template-variables")
@PreAuthorize("hasAuthority('VARIABLE_MANAGE')")
public class TemplateVariableController {
    
    private final TemplateVariableMapper variableMapper;
    private final ContractTemplateMapper templateMapper;
    
    public TemplateVariableController(TemplateVariableMapper variableMapper, ContractTemplateMapper templateMapper) {
        this.variableMapper = variableMapper;
        this.templateMapper = templateMapper;
    }
    
    @GetMapping
    public ApiResponse<Map<String, Object>> getAllVariables(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        QueryWrapper<TemplateVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("category", "sort_order", "id");
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq("type", type);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        } else {
            queryWrapper.eq("status", 1);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            queryWrapper.and(w -> w
                    .like("code", kw)
                    .or().like("name", kw)
                    .or().like("name_en", kw)
                    .or().like("label", kw));
        }

        Page<TemplateVariable> pageQuery = new Page<>(Math.max(page, 1), Math.max(pageSize, 1));
        Page<TemplateVariable> resultPage = variableMapper.selectPage(pageQuery, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", resultPage.getCurrent());
        result.put("pageSize", resultPage.getSize());
        return ApiResponse.success(result);
    }
    
    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> getCategories() {
        QueryWrapper<TemplateVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT category").eq("status", 1);
        queryWrapper.orderByAsc("category");
        
        List<TemplateVariable> variables = variableMapper.selectList(queryWrapper);
        List<Map<String, Object>> categories = variables.stream()
            .filter(v -> v.getCategory() != null && !v.getCategory().isEmpty())
            .map(v -> {
                Map<String, Object> cat = new HashMap<>();
                cat.put("value", v.getCategory());
                cat.put("label", v.getCategory());
                return cat;
            })
            .distinct()
            .collect(Collectors.toList());
        
        return ApiResponse.success(categories);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<?> getVariable(@PathVariable Long id) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在", "error.templateVariable.notFound");
        }
        return ApiResponse.success(variable);
    }
    
    @PostMapping
    public ApiResponse<?> createVariable(@Valid @RequestBody TemplateVariableCreateRequest data) {
        String code = data.getCode() == null ? "" : data.getCode().trim();
        String name = data.getName() == null ? "" : data.getName().trim();
        
        QueryWrapper<TemplateVariable> codeCheck = new QueryWrapper<>();
        codeCheck.eq("code", code);
        if (variableMapper.selectCount(codeCheck) > 0) {
            return ApiResponse.error(
                    400,
                    "变量编码已存在",
                    "error.templateVariable.codeExists",
                    Map.of("value", code));
        }
        
        TemplateVariable variable = new TemplateVariable();
        variable.setCode(code);
        variable.setName(name);
        variable.setNameEn(trimToNull(data.getNameEn()));
        variable.setLabel(trimToNull(data.getLabel()) != null ? data.getLabel().trim() : name);
        variable.setType(data.getType() == null ? "text" : data.getType().trim());
        variable.setQuickCodeCode(trimToNull(data.getQuickCodeCode()));
        variable.setCategory(data.getCategory() == null ? "custom" : data.getCategory().trim());
        variable.setDefaultValue(data.getDefaultValue() == null ? "" : data.getDefaultValue());
        variable.setDescription(data.getDescription() == null ? "" : data.getDescription());
        variable.setRequired(Boolean.TRUE.equals(data.getRequired()) ? 1 : 0);
        variable.setSortOrder(data.getSortOrder() == null ? 0 : data.getSortOrder());
        variable.setStatus(1);
        
        variableMapper.insert(variable);
        return ApiResponse.success(variable);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<?> updateVariable(@PathVariable Long id, @Valid @RequestBody TemplateVariableUpdateRequest data) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在", "error.templateVariable.notFound");
        }
        
        if (data.getName() != null) {
            String name = data.getName().trim();
            if (name.isEmpty()) {
                return ApiResponse.error(
                        400,
                        "变量名称不能为空",
                        "error.templateVariable.fieldRequired",
                        Map.of("field", "name"));
            }
            variable.setName(name);
        }
        if (data.getNameEn() != null) {
            variable.setNameEn(trimToNull(data.getNameEn()));
        }
        if (data.getLabel() != null) {
            variable.setLabel(data.getLabel().trim());
        }
        if (data.getType() != null) {
            variable.setType(data.getType().trim());
        }
        if (data.getQuickCodeCode() != null) {
            variable.setQuickCodeCode(trimToNull(data.getQuickCodeCode()));
        }
        if (data.getCategory() != null) {
            variable.setCategory(data.getCategory().trim());
        }
        if (data.getDefaultValue() != null) {
            variable.setDefaultValue(data.getDefaultValue());
        }
        if (data.getDescription() != null) {
            variable.setDescription(data.getDescription());
        }
        if (data.getRequired() != null) {
            variable.setRequired(Boolean.TRUE.equals(data.getRequired()) ? 1 : 0);
        }
        if (data.getSortOrder() != null) {
            variable.setSortOrder(data.getSortOrder());
        }
        if (data.getStatus() != null) {
            variable.setStatus(data.getStatus());
        }
        
        variableMapper.updateById(variable);
        return ApiResponse.success(variable);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteVariable(@PathVariable Long id) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在", "error.templateVariable.notFound");
        }
        Map<String, Object> impact = buildVariableImpact(variable.getCode());
        if ((Integer) impact.getOrDefault("templateCount", 0) > 0) {
            return ApiResponse.error(
                    400,
                    "变量仍被模板引用，无法删除",
                    "error.templateVariable.inUseCannotDelete",
                    impact);
        }
        variableMapper.deleteById(id);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/{id}/impact")
    public ApiResponse<?> getVariableImpact(@PathVariable Long id) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在", "error.templateVariable.notFound");
        }
        return ApiResponse.success(buildVariableImpact(variable.getCode()));
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batchCreate(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        String conflictPolicy = String.valueOf(payload.getOrDefault("conflictPolicy", "skip")).toLowerCase(Locale.ROOT);
        if (items == null || items.isEmpty()) {
            return ApiResponse.error(400, "导入数据为空", "error.templateVariable.batchEmpty");
        }

        Set<String> seenCodes = new HashSet<>();
        List<Map<String, Object>> created = new ArrayList<>();
        List<Map<String, Object>> updated = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String code = trimToNull(parseString(item.get("code")));
            String name = trimToNull(parseString(item.get("name")));
            if (code == null || name == null) {
                failed.add(Map.of(
                        "code", code == null ? "" : code,
                        "reason", "missing_required"));
                continue;
            }
            if (!seenCodes.add(code)) {
                skipped.add(Map.of("code", code, "reason", "duplicate_in_request"));
                continue;
            }

            QueryWrapper<TemplateVariable> query = new QueryWrapper<>();
            query.eq("code", code);
            TemplateVariable existing = variableMapper.selectOne(query);
            if (existing != null && !"overwrite".equals(conflictPolicy)) {
                skipped.add(Map.of("code", code, "reason", "exists"));
                continue;
            }

            try {
                TemplateVariable target = existing == null ? new TemplateVariable() : existing;
                target.setCode(code);
                target.setName(name);
                target.setNameEn(trimToNull(parseString(item.get("nameEn"))));
                target.setLabel(trimToNull(parseString(item.get("label"))) != null ? parseString(item.get("label")).trim() : name);
                target.setType(defaultString(trimToNull(parseString(item.get("type"))), "text"));
                target.setQuickCodeCode(trimToNull(parseString(item.get("quickCodeCode"))));
                target.setCategory(defaultString(trimToNull(parseString(item.get("category"))), "custom"));
                target.setDefaultValue(defaultString(parseString(item.get("defaultValue")), ""));
                target.setDescription(defaultString(parseString(item.get("description")), ""));
                target.setRequired(Boolean.TRUE.equals(parseBoolean(item.get("required"))) ? 1 : 0);
                target.setSortOrder(parseInteger(item.get("sortOrder")) == null ? 0 : parseInteger(item.get("sortOrder")));
                target.setStatus(parseInteger(item.get("status")) == null ? 1 : parseInteger(item.get("status")));

                if (existing == null) {
                    variableMapper.insert(target);
                    created.add(Map.of("id", target.getId(), "code", code));
                } else {
                    variableMapper.updateById(target);
                    updated.add(Map.of("id", target.getId(), "code", code));
                }
            } catch (Exception e) {
                failed.add(Map.of(
                        "code", code,
                        "reason", defaultString(e.getMessage(), "save_failed")));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", created);
        result.put("updated", updated);
        result.put("skipped", skipped);
        result.put("failed", failed);
        result.put("total", items.size());
        return ApiResponse.success(result);
    }
    
    @PostMapping("/init-defaults")
    public ApiResponse<String> initDefaultVariables() {
        List<Map<String, Object>> defaults = new ArrayList<>();
        
        defaults.add(createVar("contractNo", "合同编号", "合同编号", "text", "system", "自动生成的合同编号"));
        defaults.add(createVar("title", "合同名称", "合同名称", "text", "system", "合同的标题"));
        defaults.add(createVar("amount", "合同金额", "合同金额", "number", "system", "合同的总金额"));
        defaults.add(createVar("amountChinese", "金额大写", "金额大写", "text", "system", "金额的中文大写形式"));
        defaults.add(createVar("startDate", "开始日期", "开始日期", "date", "system", "合同开始日期"));
        defaults.add(createVar("endDate", "结束日期", "结束日期", "date", "system", "合同结束日期"));
        defaults.add(createVar("signDate", "签订日期", "签订日期", "date", "system", "合同签订日期"));
        defaults.add(createVar("today", "当前日期", "当前日期", "text", "system", "当前日期"));
        
        defaults.add(createVar("partyA", "甲方名称", "甲方公司名称", "text", "party", "甲方公司全称"));
        defaults.add(createVar("partyAContact", "甲方联系人", "甲方联系人", "text", "party", "甲方的联系人姓名"));
        defaults.add(createVar("partyAPhone", "甲方电话", "甲方联系电话", "text", "party", "甲方的联系电话"));
        defaults.add(createVar("partyAEmail", "甲方邮箱", "甲方邮箱", "text", "party", "甲方的电子邮箱"));
        defaults.add(createVar("partyAAddress", "甲方地址", "甲方地址", "text", "party", "甲方的公司地址"));
        
        defaults.add(createVar("partyB", "乙方名称", "乙方公司名称", "text", "party", "乙方公司全称"));
        defaults.add(createVar("partyBContact", "乙方联系人", "乙方联系人", "text", "party", "乙方的联系人姓名"));
        defaults.add(createVar("partyBPhone", "乙方电话", "乙方联系电话", "text", "party", "乙方的联系电话"));
        defaults.add(createVar("partyBEmail", "乙方邮箱", "乙方邮箱", "text", "party", "乙方的电子邮箱"));
        defaults.add(createVar("partyBAddress", "乙方地址", "乙方地址", "text", "party", "乙方的公司地址"));
        
        defaults.add(createVar("productName", "产品名称", "产品名称", "text", "purchase", "采购产品的名称"));
        defaults.add(createVar("quantity", "数量", "数量", "number", "purchase", "采购产品的数量"));
        defaults.add(createVar("unitPrice", "单价", "单价", "number", "purchase", "产品的单价"));
        defaults.add(createVar("totalPrice", "总价", "总价", "number", "purchase", "产品的总价格"));
        defaults.add(createVar("deliveryDays", "交货天数", "交货天数", "number", "purchase", "交货期限（天）"));
        defaults.add(createVar("paymentDays", "付款天数", "付款天数", "number", "purchase", "付款期限（天）"));
        defaults.add(createVar("penaltyRate", "违约金比例", "违约金比例", "number", "purchase", "违约金的百分比"));
        
        defaults.add(createVar("projectName", "项目名称", "项目名称", "text", "service", "服务项目的名称"));
        defaults.add(createVar("projectDesc", "项目描述", "项目描述", "textarea", "service", "项目的详细描述"));
        defaults.add(createVar("deliverables", "交付成果", "交付成果", "textarea", "service", "项目交付的成果物"));
        defaults.add(createVar("totalDays", "总工期", "总工期", "number", "service", "项目总工期（天）"));
        defaults.add(createVar("totalAmount", "总金额", "服务总金额", "number", "service", "服务的总金额"));
        
        defaults.add(createVar("address", "房屋地址", "房屋地址", "text", "lease", "租赁房屋的地址"));
        defaults.add(createVar("area", "建筑面积", "建筑面积", "number", "lease", "房屋的建筑面积（平方米）"));
        defaults.add(createVar("leaseMonths", "租赁月数", "租赁月数", "number", "lease", "租赁的月数"));
        defaults.add(createVar("monthlyRent", "月租金", "月租金", "number", "lease", "每月的租金"));
        defaults.add(createVar("deposit", "押金", "押金", "number", "lease", "租赁押金"));
        
        defaults.add(createVar("employeeName", "员工姓名", "员工姓名", "text", "employment", "员工的全名"));
        defaults.add(createVar("companyName", "公司名称", "公司名称", "text", "employment", "公司的全称"));
        defaults.add(createVar("position", "职位", "职位", "text", "employment", "员工担任的职位"));
        defaults.add(createVar("salary", "薪资", "月薪", "number", "employment", "每月的工资"));
        defaults.add(createVar("probationPeriod", "试用期", "试用期", "number", "employment", "试用期的月数"));
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", defaults);
        payload.put("conflictPolicy", "skip");
        batchCreate(payload);
        return ApiResponse.success("已初始化 " + defaults.size() + " 个默认变量");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, Object> buildVariableImpact(String code) {
        String contentNeedle = "[[" + code + "]]";
        String variablesNeedle = "\"" + code + "\"";
        QueryWrapper<ContractTemplate> wrapper = new QueryWrapper<>();
        wrapper.select("id", "name", "category", "updated_at")
                .and(w -> w.like("content", contentNeedle).or().like("variables", variablesNeedle));
        List<ContractTemplate> templates = templateMapper.selectList(wrapper);
        List<Map<String, Object>> templateRefs = templates.stream().map(t -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", t.getId());
            row.put("name", t.getName());
            row.put("category", t.getCategory());
            row.put("updatedAt", t.getUpdatedAt());
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> impact = new HashMap<>();
        impact.put("code", code);
        impact.put("templateCount", templateRefs.size());
        impact.put("templates", templateRefs);
        return impact;
    }

    private String parseString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool;
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if ("1".equals(text) || "true".equals(text) || "yes".equals(text)) return true;
        if ("0".equals(text) || "false".equals(text) || "no".equals(text)) return false;
        return null;
    }

    private String defaultString(String value, String fallback) {
        return value == null ? fallback : value;
    }
    
    private Map<String, Object> createVar(String code, String name, String label, String type, String category, String desc) {
        Map<String, Object> v = new HashMap<>();
        v.put("code", code);
        v.put("name", name);
        v.put("label", label);
        v.put("type", type);
        v.put("category", category);
        v.put("description", desc);
        return v;
    }
}
