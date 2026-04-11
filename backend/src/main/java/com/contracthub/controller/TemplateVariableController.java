package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.TemplateVariable;
import com.contracthub.mapper.TemplateVariableMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/template-variables")
@PreAuthorize("hasAuthority('VARIABLE_MANAGE')")
public class TemplateVariableController {
    
    private final TemplateVariableMapper variableMapper;
    
    public TemplateVariableController(TemplateVariableMapper variableMapper) {
        this.variableMapper = variableMapper;
    }
    
    @GetMapping
    public ApiResponse<List<TemplateVariable>> getAllVariables(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        
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
        
        List<TemplateVariable> variables = variableMapper.selectList(queryWrapper);
        return ApiResponse.success(variables);
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
                cat.put("label", getCategoryLabel(v.getCategory()));
                return cat;
            })
            .distinct()
            .collect(Collectors.toList());
        
        return ApiResponse.success(categories);
    }
    
    private String getCategoryLabel(String category) {
        Map<String, String> labels = new HashMap<>();
        labels.put("system", "系统变量");
        labels.put("contract", "合同信息");
        labels.put("party", "相对方信息");
        labels.put("purchase", "采购合同");
        labels.put("service", "服务合同");
        labels.put("lease", "租赁合同");
        labels.put("employment", "劳动合同");
        labels.put("custom", "自定义变量");
        return labels.getOrDefault(category, category);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<TemplateVariable> getVariable(@PathVariable Long id) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在");
        }
        return ApiResponse.success(variable);
    }
    
    @PostMapping
    public ApiResponse<TemplateVariable> createVariable(@RequestBody Map<String, Object> data) {
        if (data.get("code") == null || data.get("name") == null) {
            return ApiResponse.error("变量编码和名称不能为空");
        }
        
        QueryWrapper<TemplateVariable> codeCheck = new QueryWrapper<>();
        codeCheck.eq("code", data.get("code"));
        if (variableMapper.selectCount(codeCheck) > 0) {
            return ApiResponse.error("变量编码已存在");
        }
        
        TemplateVariable variable = new TemplateVariable();
        variable.setCode((String) data.get("code"));
        variable.setName((String) data.get("name"));
        variable.setLabel((String) data.getOrDefault("label", data.get("name")));
        variable.setType((String) data.getOrDefault("type", "text"));
        variable.setCategory((String) data.getOrDefault("category", "custom"));
        variable.setDefaultValue((String) data.getOrDefault("defaultValue", ""));
        variable.setDescription((String) data.getOrDefault("description", ""));
        variable.setRequired(data.get("required") != null ? 1 : 0);
        variable.setSortOrder(data.get("sortOrder") != null ? ((Number) data.get("sortOrder")).intValue() : 0);
        variable.setStatus(1);
        
        variableMapper.insert(variable);
        return ApiResponse.success(variable);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<TemplateVariable> updateVariable(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        TemplateVariable variable = variableMapper.selectById(id);
        if (variable == null) {
            return ApiResponse.error("变量不存在");
        }
        
        if (data.containsKey("name")) {
            variable.setName((String) data.get("name"));
        }
        if (data.containsKey("nameEn")) {
            variable.setNameEn((String) data.get("nameEn"));
        }
        if (data.containsKey("label")) {
            variable.setLabel((String) data.get("label"));
        }
        if (data.containsKey("type")) {
            variable.setType((String) data.get("type"));
        }
        if (data.containsKey("category")) {
            variable.setCategory((String) data.get("category"));
        }
        if (data.containsKey("defaultValue")) {
            variable.setDefaultValue((String) data.get("defaultValue"));
        }
        if (data.containsKey("description")) {
            variable.setDescription((String) data.get("description"));
        }
        if (data.containsKey("required")) {
            variable.setRequired((Integer) data.get("required"));
        }
        if (data.containsKey("sortOrder")) {
            variable.setSortOrder((Integer) data.get("sortOrder"));
        }
        if (data.containsKey("status")) {
            variable.setStatus((Integer) data.get("status"));
        }
        
        variableMapper.updateById(variable);
        return ApiResponse.success(variable);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVariable(@PathVariable Long id) {
        int result = variableMapper.deleteById(id);
        if (result == 0) {
            return ApiResponse.error("变量不存在");
        }
        return ApiResponse.success(null);
    }
    
    @PostMapping("/batch")
    public ApiResponse<List<TemplateVariable>> batchCreate(@RequestBody List<Map<String, Object>> dataList) {
        List<TemplateVariable> created = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            if (data.get("code") == null || data.get("name") == null) {
                continue;
            }
            
            TemplateVariable variable = new TemplateVariable();
            variable.setCode((String) data.get("code"));
            variable.setName((String) data.get("name"));
            variable.setLabel((String) data.getOrDefault("label", data.get("name")));
            variable.setType((String) data.getOrDefault("type", "text"));
            variable.setCategory((String) data.getOrDefault("category", "custom"));
            variable.setDefaultValue((String) data.getOrDefault("defaultValue", ""));
            variable.setDescription((String) data.getOrDefault("description", ""));
            variable.setRequired(0);
            variable.setSortOrder(0);
            variable.setStatus(1);
            
            variableMapper.insert(variable);
            created.add(variable);
        }
        return ApiResponse.success(created);
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
        
        batchCreate(defaults);
        return ApiResponse.success("已初始化 " + defaults.size() + " 个默认变量");
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
