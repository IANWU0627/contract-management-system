package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractTemplate;
import com.contracthub.mapper.ContractTemplateMapper;
import com.contracthub.service.TemplateVariableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    private final ContractTemplateMapper templateMapper;
    private final TemplateVariableService variableService;
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TemplateController(ContractTemplateMapper templateMapper, TemplateVariableService variableService) {
        this.templateMapper = templateMapper;
        this.variableService = variableService;
        
        try {
            if (templateMapper.selectCount(null) == 0) {
                initDefaultTemplates();
            }
        } catch (Exception e) {
            // 表不存在时跳过初始化
        }
    }
    
    private Long getUsageCount(Long templateId) {
        ContractTemplate template = templateMapper.selectById(templateId);
        return template != null && template.getUsageCount() != null ? template.getUsageCount() : 0L;
    }
    
    private void incrementUsageCount(Long templateId) {
        ContractTemplate template = templateMapper.selectById(templateId);
        if (template != null) {
            Long currentCount = template.getUsageCount() != null ? template.getUsageCount() : 0L;
            template.setUsageCount(currentCount + 1);
            templateMapper.updateById(template);
        }
    }
    
    private void initDefaultTemplates() {
        ContractTemplate template1 = new ContractTemplate();
        template1.setName("标准采购合同模板");
        template1.setCategory("PURCHASE");
        template1.setContent("【合同编号】：[[contractNo]]\n\n" +
            "【甲方】：[[partyA]]\n【乙方】：[[partyB]]\n\n" +
            "一、合同标的\n甲方同意购买，乙方同意出售以下商品/服务：\n商品名称：[[productName]]\n数量：[[quantity]]\n单价：[[unitPrice]]\n总价：[[totalPrice]]\n\n" +
            "二、质量标准\n符合国家相关质量标准及双方约定的技术规范。\n\n" +
            "三、交货/服务期限\n自合同签订之日起[[deliveryDays]]日内完成。\n\n" +
            "四、付款方式\n□ 全款预付  □ 货到付款  □ 分期付款\n付款周期：[[paymentDays]]天\n\n" +
            "五、违约责任\n任一方违约，应向守约方支付合同总金额[[penaltyRate]]%的违约金。\n\n" +
            "六、争议解决\n本合同适用中华人民共和国法律。如有争议，提交甲方所在地人民法院管辖。\n\n" +
            "【甲方签章】：[[partyASign]]  【乙方签章】：[[partyBSign]]\n【签订日期】：[[signDate]]");
        template1.setVariables("{\"contractNo\": \"合同编号\", \"partyA\": \"甲方名称\", \"partyB\": \"乙方名称\", \"productName\": \"商品名称\", \"quantity\": \"数量\", \"unitPrice\": \"单价\", \"totalPrice\": \"总价\", \"deliveryDays\": \"交货天数\", \"paymentDays\": \"付款周期\", \"penaltyRate\": \"违约金比例\", \"signDate\": \"签订日期\", \"partyASign\": \"甲方签章\", \"partyBSign\": \"乙方签章\"}");
        templateMapper.insert(template1);
        
        
        ContractTemplate template2 = new ContractTemplate();
        template2.setName("软件外包服务合同");
        template2.setCategory("SERVICE");
        template2.setContent("【合同编号】：[[contractNo]]\n\n" +
            "【甲方（委托方）】：[[partyA]]\n【乙方（受托方）】：[[partyB]]\n\n" +
            "一、服务内容\n乙方为甲方提供以下软件开发/服务：\n项目名称：[[projectName]]\n项目描述：[[projectDesc]]\n交付成果：[[deliverables]]\n\n" +
            "二、项目周期\n起始日期：[[startDate]]\n完成日期：[[endDate]]\n总工期：[[totalDays]]个工作日\n\n" +
            "三、服务费用\n合同总金额：人民币（大写）[[totalAmountChinese]]元整（¥[[totalAmount]]）\n付款方式：\n- 首付款（30%）：[[firstPayment]]元，项目启动时支付\n- 进度款（40%）：[[progressPayment]]元，完成中期验收后支付\n- 尾款（30%）：[[finalPayment]]元，最终交付后支付\n\n" +
            "四、知识产权\n项目成果的知识产权归属：\n□ 归甲方所有  □ 双方共有  □ 归乙方所有\n\n" +
            "五、保密条款\n双方应对合作过程中知悉的对方商业秘密承担保密义务，未经书面授权不得对外披露。\n\n" +
            "六、验收标准\n以双方确认的需求文档和设计文档为验收标准。\n\n" +
            "【甲方签章】：[[partyASign]]  【乙方签章】：[[partyBSign]]\n【签订日期】：[[signDate]]");
        template2.setVariables("{\"contractNo\": \"合同编号\", \"partyA\": \"甲方名称\", \"partyB\": \"乙方名称\", \"projectName\": \"项目名称\", \"projectDesc\": \"项目描述\", \"deliverables\": \"交付成果\", \"startDate\": \"起始日期\", \"endDate\": \"完成日期\", \"totalDays\": \"总工期\", \"totalAmount\": \"合同总金额\", \"totalAmountChinese\": \"大写金额\", \"firstPayment\": \"首付款\", \"progressPayment\": \"进度款\", \"finalPayment\": \"尾款\", \"signDate\": \"签订日期\", \"partyASign\": \"甲方签章\", \"partyBSign\": \"乙方签章\"}");
        templateMapper.insert(template2);
        
        
        ContractTemplate template3 = new ContractTemplate();
        template3.setName("房屋租赁合同");
        template3.setCategory("LEASE");
        template3.setContent("【合同编号】：[[contractNo]]\n\n" +
            "【出租方（甲方）】：[[partyA]]  证件号码：[[partyAId]]\n【承租方（乙方）】：[[partyB]]  证件号码：[[partyBId]]\n\n" +
            "一、租赁标的\n房屋地址：[[address]]\n建筑面积：[[area]]平方米\n用途：□ 住宅  □ 办公  □ 商业\n\n" +
            "二、租赁期限\n起始日期：[[startDate]]\n终止日期：[[endDate]]\n租赁期限：[[leaseMonths]]个月\n\n" +
            "三、租金及押金\n月租金：人民币（大写）[[monthlyRentChinese]]元整（¥[[monthlyRent]]/月）\n押金：人民币（大写）[[depositChinese]]元整\n支付方式：□ 月付  □ 季付  □ 年付\n\n" +
            "四、费用承担\n水费：□ 甲方  □ 乙方\n电费：□ 甲方  □ 乙方\n燃气费：□ 甲方  □ 乙方\n物业费：□ 甲方  □ 乙方\n\n" +
            "五、转租条款\n未经甲方书面同意，乙方不得将房屋全部或部分转租、转借给他人。\n\n" +
            "六、提前解约\n任一方提前解约，应提前[[noticeDays]]天书面通知对方，并支付[[penaltyMonths]]个月租金作为违约金。\n\n" +
            "【甲方签章】：[[partyASign]]  【乙方签章】：[[partyBSign]]\n【签订日期】：[[signDate]]");
        template3.setVariables("{\"contractNo\": \"合同编号\", \"partyA\": \"出租方\", \"partyAId\": \"甲方证件号\", \"partyB\": \"承租方\", \"partyBId\": \"乙方证件号\", \"address\": \"房屋地址\", \"area\": \"建筑面积\", \"startDate\": \"起始日期\", \"endDate\": \"终止日期\", \"leaseMonths\": \"租赁月数\", \"monthlyRent\": \"月租金\", \"monthlyRentChinese\": \"月租金大写\", \"deposit\": \"押金\", \"depositChinese\": \"押金大写\", \"noticeDays\": \"通知天数\", \"penaltyMonths\": \"违约金月数\", \"signDate\": \"签订日期\", \"partyASign\": \"甲方签章\", \"partyBSign\": \"乙方签章\"}");
        templateMapper.insert(template3);
        
        
        ContractTemplate template4 = new ContractTemplate();
        template4.setName("劳动合同模板");
        template4.setCategory("EMPLOYMENT");
        template4.setContent("【合同编号】：[[contractNo]]\n\n" +
            "【甲方（用人单位）】：[[companyName]]\n【乙方（劳动者）】：[[employeeName]]\n身份证号：[[employeeId]]  联系电话：[[employeePhone]]\n\n" +
            "一、合同期限\n□ 固定期限：[[startDate]]至[[endDate]]\n□ 无固定期限\n□ 以完成一定工作任务为期限\n\n" +
            "二、工作内容和工作地点\n岗位/职位：[[position]]\n工作内容：[[workContent]]\n工作地点：[[workLocation]]\n\n" +
            "三、工作时间和休息休假\n工作时间：□ 标准工时制（每日8小时，每周40小时）\n          □ 综合计算工时制\n          □ 不定时工作制\n休假：按国家规定享有带薪年假、法定节假日、婚假、产假等。\n\n" +
            "四、劳动报酬\n月工资：人民币（大写）[[monthlySalaryChinese]]元整（¥[[monthlySalary]]/月）\n工资发放日：每月[[payDay]]日\n支付方式：□ 银行转账  □ 现金\n\n" +
            "五、社会保险\n甲方依法为乙方缴纳社会保险。\n\n" +
            "六、保密与竞业限制\n□ 需要  □ 不需要\n如需要，竞业限制期限：[[nonCompeteMonths]]个月，补偿金：[[nonCompeteCompensation]]元/月\n\n" +
            "七、合同解除\n按《劳动合同法》相关规定执行。\n\n" +
            "【甲方签章】：[[companySign]]  【乙方签章】：[[employeeSign]]\n【签订日期】：[[signDate]]");
        template4.setVariables("{\"contractNo\": \"合同编号\", \"companyName\": \"公司名称\", \"employeeName\": \"员工姓名\", \"employeeId\": \"身份证号\", \"employeePhone\": \"联系电话\", \"startDate\": \"起始日期\", \"endDate\": \"终止日期\", \"position\": \"岗位\", \"workContent\": \"工作内容\", \"workLocation\": \"工作地点\", \"monthlySalary\": \"月工资\", \"monthlySalaryChinese\": \"月工资大写\", \"payDay\": \"工资发放日\", \"nonCompeteMonths\": \"竞业限制月数\", \"nonCompeteCompensation\": \"竞业限制补偿金\", \"signDate\": \"签订日期\", \"companySign\": \"公司签章\", \"employeeSign\": \"员工签章\"}");
        templateMapper.insert(template4);
        
        
        ContractTemplate template5 = new ContractTemplate();
        template5.setName("销售代理协议");
        template5.setCategory("SALES");
        template5.setContent("【合同编号】：[[contractNo]]\n\n" +
            "【甲方（委托方）】：[[partyA]]\n【乙方（代理方）】：[[partyB]]\n\n" +
            "一、代理产品\n乙方代理销售甲方的以下产品/服务：\n产品名称：[[productName]]\n品牌：[[brand]]\n型号规格：[[specification]]\n\n" +
            "二、代理区域和权限\n代理区域：[[region]]\n代理权限：□ 独家代理  □ 非独家代理\n代理级别：□ 省级  □ 市级  □ 县级\n\n" +
            "三、销售任务\n年度销售任务：人民币[[annualTask]]元\n季度任务：[[quarterlyTask]]元\n如连续[[consecutiveQuarters]]个季度未完成任务的[[taskPercentage]]%，甲方有权调整代理权限。\n\n" +
            "四、价格政策\n代理价格：按甲方提供的价格表执行\n最低零售价：[[minRetailPrice]]\n价格调整：提前[[priceNoticeDays]]天书面通知\n\n" +
            "五、佣金/返利政策\n佣金比例：[[commissionRate]]%\n返利政策：\n- 年销售额达[[salesTarget1]]万，返利[[rebateRate1]]%\n- 年销售额达[[salesTarget2]]万，返利[[rebateRate2]]%\n\n" +
            "六、市场支持\n甲方提供的市场支持：\n- 样品：[[samplesCount]]套\n- 宣传物料：[[promoMaterialsCount]]份\n- 广告支持：[[adSupportAmount]]元\n\n" +
            "七、代理期限\n[[startDate]]至[[endDate]]\n\n" +
            "【甲方签章】：[[partyASign]]  【乙方签章】：[[partyBSign]]\n【签订日期】：[[signDate]]");
        template5.setVariables("{\"contractNo\": \"合同编号\", \"partyA\": \"甲方名称\", \"partyB\": \"乙方名称\", \"productName\": \"产品名称\", \"brand\": \"品牌\", \"specification\": \"型号规格\", \"region\": \"代理区域\", \"annualTask\": \"年度任务\", \"quarterlyTask\": \"季度任务\", \"consecutiveQuarters\": \"连续季度\", \"taskPercentage\": \"任务完成比例\", \"minRetailPrice\": \"最低零售价\", \"priceNoticeDays\": \"价格调整通知天数\", \"commissionRate\": \"佣金比例\", \"salesTarget1\": \"销售目标1\", \"rebateRate1\": \"返利比例1\", \"salesTarget2\": \"销售目标2\", \"rebateRate2\": \"返利比例2\", \"samplesCount\": \"样品数量\", \"promoMaterialsCount\": \"宣传物料数量\", \"adSupportAmount\": \"广告支持金额\", \"startDate\": \"起始日期\", \"endDate\": \"终止日期\", \"signDate\": \"签订日期\", \"partyASign\": \"甲方签章\", \"partyBSign\": \"乙方签章\"}");
        templateMapper.insert(template5);
        
    }
    
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<ContractTemplate> templateList = templateMapper.selectList(null);
        List<Map<String, Object>> filtered = new ArrayList<>();
        
        for (ContractTemplate template : templateList) {
            Map<String, Object> map = buildTemplateMap(template);
            filtered.add(map);
        }
        
        if (category != null && !category.isEmpty()) {
            filtered.removeIf(t -> !category.equals(t.get("category")));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered.removeIf(t -> {
                String name = String.valueOf(t.getOrDefault("name", "")).toLowerCase();
                String desc = String.valueOf(t.getOrDefault("description", "")).toLowerCase();
                return !name.contains(kw) && !desc.contains(kw);
            });
        }
        
        int total = filtered.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageList = start < total ? filtered.subList(start, end) : new ArrayList<>();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long id) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        Map<String, Object> result = buildTemplateMap(template);
        return ApiResponse.success(result);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('TEMPLATE_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> templateData) {
        ContractTemplate template = new ContractTemplate();
        template.setName((String) templateData.get("name"));
        template.setCategory((String) templateData.get("category"));
        template.setDescription((String) templateData.getOrDefault("description", ""));
        String content = (String) templateData.get("content");
        template.setContent(content);
        
        String extractedVars = extractAndBuildVariables(content);
        String providedVars = (String) templateData.getOrDefault("variables", "{}");
        String mergedVars = mergeVariables(extractedVars, providedVars);
        template.setVariables(mergedVars);
        
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        templateMapper.insert(template);
        
        Map<String, Object> result = buildTemplateMap(template);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_MANAGE')")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> templateData) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        if (templateData.containsKey("name")) {
            template.setName((String) templateData.get("name"));
        }
        if (templateData.containsKey("category")) {
            template.setCategory((String) templateData.get("category"));
        }
        if (templateData.containsKey("description")) {
            template.setDescription((String) templateData.get("description"));
        }
        if (templateData.containsKey("content")) {
            String content = (String) templateData.get("content");
            template.setContent(content);
            String extractedVars = extractAndBuildVariables(content);
            String providedVars = (String) templateData.getOrDefault("variables", "{}");
            String mergedVars = mergeVariables(extractedVars, providedVars);
            template.setVariables(mergedVars);
        }
        if (templateData.containsKey("variables") && !templateData.containsKey("content")) {
            template.setVariables((String) templateData.get("variables"));
        }
        
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);
        
        Map<String, Object> result = buildTemplateMap(template);
        return ApiResponse.success(result);
    }
    
    private String extractAndBuildVariables(String content) {
        List<String> varNames = variableService.extractVariables(content);
        Map<String, String> varMap = new LinkedHashMap<>();
        for (String varName : varNames) {
            varMap.put(varName, varName);
        }
        try {
            return objectMapper.writeValueAsString(varMap);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String mergeVariables(String extractedVars, String providedVars) {
        try {
            Map<String, String> merged = objectMapper.readValue(extractedVars, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            if (providedVars != null && !providedVars.isEmpty()) {
                Map<String, String> provided = objectMapper.readValue(providedVars, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                merged.putAll(provided);
            }
            return objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            return extractedVars;
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        int result = templateMapper.deleteById(id);
        if (result == 0) {
            return ApiResponse.error("模板不存在");
        }
        return ApiResponse.success(null);
    }
    
    @PostMapping("/{id}/preview-simple")
    public ApiResponse<Map<String, Object>> previewSimple(
            @PathVariable Long id, 
            @RequestBody(required = false) Map<String, String> params) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        String content = template.getContent();
        Map<String, String> variables = parseVariables(template.getVariables());
        
        if (params == null) {
            params = new HashMap<>();
        }
        
        content = substituteVariables(content, params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", template.getId());
        result.put("name", template.getName());
        result.put("category", template.getCategory());
        result.put("content", content);
        result.put("variables", variables);
        result.put("filledAt", LocalDate.now().format(df));
        
        return ApiResponse.success(result);
    }
    
    @PostMapping("/{id}/substitute")
    public ApiResponse<Map<String, Object>> substitute(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> contractData) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        Map<String, Object> variables = variableService.getDefaultVariables(template, contractData);
        
        String content = variableService.replaceVariables(template.getContent(), variables);
        
        incrementUsageCount(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("usageCount", getUsageCount(id));
        
        return ApiResponse.success(result);
    }
    
    @PostMapping("/{id}/clone")
    @PreAuthorize("hasAuthority('TEMPLATE_MANAGE')")
    public ApiResponse<Map<String, Object>> clone(@PathVariable Long id) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        ContractTemplate clone = new ContractTemplate();
        clone.setName(template.getName() + " (副本)");
        clone.setCategory(template.getCategory());
        clone.setContent(template.getContent());
        clone.setVariables(template.getVariables());
        clone.setCreatedAt(LocalDateTime.now());
        clone.setUpdatedAt(LocalDateTime.now());
        
        templateMapper.insert(clone);
        
        Map<String, Object> result = buildTemplateMap(clone);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/variables/{id}")
    public ApiResponse<Map<String, String>> getVariables(@PathVariable Long id) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        Map<String, String> variables = parseVariables(template.getVariables());
        return ApiResponse.success(variables);
    }
    
    private Map<String, Object> buildTemplateMap(ContractTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", template.getId());
        map.put("name", template.getName());
        map.put("category", template.getCategory());
        map.put("content", template.getContent());
        map.put("variables", parseVariables(template.getVariables()));
        map.put("description", template.getDescription() != null ? template.getDescription() : "");
        map.put("createdAt", template.getCreatedAt() != null ? template.getCreatedAt().format(df) : "");
        map.put("updatedAt", template.getUpdatedAt() != null ? template.getUpdatedAt().format(df) : "");
        map.put("usageCount", getUsageCount(template.getId()));
        map.put("creator", "admin");
        return map;
    }
    
    private Map<String, String> parseVariables(String variablesJson) {
        Map<String, String> variables = new LinkedHashMap<>();
        if (variablesJson == null || variablesJson.isEmpty()) {
            return variables;
        }
        
        try {
            Map<String, String> parsed = objectMapper.readValue(variablesJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            variables.putAll(parsed);
        } catch (Exception e) {
            // ignore
        }
        
        return variables;
    }
    
    @GetMapping("/variables/list")
    public ApiResponse<List<Map<String, Object>>> getAvailableVariables(@RequestParam(required = false) String contractType) {
        List<Map<String, Object>> variables = variableService.getAvailableVariables(contractType);
        return ApiResponse.success(variables);
    }
    
    @GetMapping("/variables/extract")
    public ApiResponse<List<String>> extractVariables(@RequestParam String content) {
        List<String> variables = variableService.extractVariables(content);
        return ApiResponse.success(variables);
    }
    
    @PostMapping("/{id}/preview")
    public ApiResponse<Map<String, Object>> previewTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> data) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        Map<String, Object> contractData = data != null ? data : new HashMap<>();
        Map<String, Object> variables = variableService.getDefaultVariables(template, contractData);
        
        Map<String, Object> filledValues = new HashMap<>();
        if (data != null && data.containsKey("values")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> customValues = (Map<String, Object>) data.get("values");
            if (customValues != null) {
                filledValues.putAll(customValues);
            }
        }
        variables.putAll(filledValues);
        
        String previewContent = variableService.replaceVariables(template.getContent(), variables);
        
        Map<String, Object> result = new HashMap<>();
        result.put("originalContent", template.getContent());
        result.put("previewContent", previewContent);
        result.put("variables", variables);
        result.put("template", buildTemplateMap(template));
        
        return ApiResponse.success(result);
    }
    
    @PostMapping("/replace")
    public ApiResponse<String> replaceVariables(
            @RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) request.get("values");
        
        if (content == null) {
            return ApiResponse.error("内容不能为空");
        }
        
        String result = variableService.replaceVariables(content, values != null ? values : new HashMap<>());
        return ApiResponse.success(result);
    }
    
    @PostMapping("/{id}/watermark")
    public ApiResponse<Map<String, Object>> applyWatermark(
            @PathVariable Long id,
            @RequestBody Map<String, Object> config) {
        
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        String text = (String) config.get("text");
        String imageUrl = (String) config.get("imageUrl");
        String position = (String) config.getOrDefault("position", "diagonal");
        Integer opacity = (Integer) config.getOrDefault("opacity", 30);
        
        String content = template.getContent();
        
        String watermarkedContent;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            watermarkedContent = applyImageWatermark(content, imageUrl, position, opacity);
        } else {
            watermarkedContent = applyTextWatermark(content, text, position, opacity);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("originalContent", content);
        result.put("watermarkedContent", watermarkedContent);
        result.put("position", position);
        result.put("opacity", opacity);
        
        return ApiResponse.success(result);
    }
    
    private String applyTextWatermark(String content, String text, String position, int opacity) {
        if (text == null || text.isEmpty()) {
            return content;
        }
        
        String watermarkHtml;
        switch (position) {
            case "header":
                watermarkHtml = "<div style='text-align: center; opacity: " + (opacity / 100.0) + "; color: #ccc; font-size: 12px; border-bottom: 1px dashed #ccc; padding: 10px; margin-bottom: 20px;'>" + text + "</div>";
                break;
            case "footer":
                watermarkHtml = "<div style='text-align: center; opacity: " + (opacity / 100.0) + "; color: #ccc; font-size: 12px; border-top: 1px dashed #ccc; padding: 10px; margin-top: 20px;'>" + text + "</div>";
                break;
            case "corner":
                watermarkHtml = "<div style='position: fixed; bottom: 20px; right: 20px; opacity: " + (opacity / 100.0) + "; color: #ccc; font-size: 14px;'>" + text + "</div>";
                break;
            case "diagonal":
            default:
                watermarkHtml = "<div style='position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); opacity: " + (opacity / 100.0) + "; color: #ccc; font-size: 48px; font-weight: bold; pointer-events: none; z-index: 9999;'>" + text + "</div>";
                break;
        }
        
        return watermarkHtml + content;
    }
    
    private String applyImageWatermark(String content, String imageUrl, String position, int opacity) {
        String watermarkHtml = "<div style='text-align: center; opacity: " + (opacity / 100.0) + "; padding: 10px;'><img src='" + imageUrl + "' style='max-width: 200px;' /></div>";
        
        switch (position) {
            case "header":
                return watermarkHtml + content;
            case "footer":
                return content + watermarkHtml;
            case "corner":
                String cornerWatermark = "<div style='position: fixed; bottom: 20px; right: 20px; opacity: " + (opacity / 100.0) + "; z-index: 9999;'><img src='" + imageUrl + "' style='max-width: 100px;' /></div>";
                return cornerWatermark + content;
            case "diagonal":
            default:
                String diagonalWatermark = "<div style='position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); opacity: " + (opacity / 100.0) + "; z-index: 9999; pointer-events: none;'><img src='" + imageUrl + "' style='max-width: 300px;' /></div>";
                return diagonalWatermark + content;
        }
    }
    
    @GetMapping("/{id}/export/pdf")
    public void exportPdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=template_" + id + ".pdf");
        
        String htmlContent = template.getContent();
        
        if (htmlContent == null || htmlContent.isEmpty()) {
            htmlContent = "<p>No content</p>";
        }
        
        String fullHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
            "body { font-family: 'Noto Sans SC', 'SimSun', sans-serif; font-size: 14px; line-height: 1.8; padding: 20px; } " +
            "h1 { font-size: 24px; text-align: center; } h2 { font-size: 18px; } h3 { font-size: 16px; } " +
            "p { margin: 8px 0; } table { border-collapse: collapse; width: 100%; margin: 10px 0; } " +
            "th, td { border: 1px solid #333; padding: 8px; text-align: left; } " +
            "th { background-color: #f5f5f5; } img { max-width: 100%; height: auto; } " +
            "ul, ol { margin: 8px 0; padding-left: 24px; } " +
            ".watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); " +
            "opacity: 0.3; font-size: 48px; font-weight: bold; color: #ccc; pointer-events: none; z-index: 9999; }" +
            "</style></head><body>" + htmlContent + "</body></html>";
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.itextpdf.html2pdf.HtmlConverter.convertToPdf(fullHtml, baos);
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }
    
    @GetMapping("/{id}/export")
    public ApiResponse<Map<String, Object>> exportTemplate(@PathVariable Long id) {
        ContractTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return ApiResponse.error("模板不存在");
        }
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("id", template.getId());
        exportData.put("name", template.getName());
        exportData.put("category", template.getCategory());
        exportData.put("content", template.getContent());
        exportData.put("variables", template.getVariables());
        exportData.put("exportTime", LocalDateTime.now().toString());
        
        return ApiResponse.success(exportData);
    }
    
    private String substituteVariables(String content, Map<String, String> replacements) {
        if (content == null || replacements == null) {
            return content;
        }
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "[[" + entry.getKey() + "]]";
            String value = entry.getValue() != null ? entry.getValue() : "";
            content = content.replace(placeholder, value);
        }
        
        return content;
    }
}
