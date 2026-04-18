package com.contracthub.service;

import com.contracthub.entity.ContractTemplate;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateVariableService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\[\\[(\\w+)\\]\\]");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ContractTypeFieldMapper typeFieldMapper;
    
    public TemplateVariableService(ContractTypeFieldMapper typeFieldMapper) {
        this.typeFieldMapper = typeFieldMapper;
    }
    
    public String replaceVariables(String content, Map<String, Object> values) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = values.get(variableName);
            String replacement = value != null ? String.valueOf(value) : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    public Map<String, Object> getDefaultVariables(ContractTemplate template, Map<String, Object> contractData) {
        Map<String, Object> variables = new HashMap<>();
        
        if (template == null) {
            return variables;
        }
        
        // Parse template's predefined variables
        Map<String, String> templateVars = parseTemplateVariables(template.getVariables());
        
        // Step 1: Add default values for common variables FIRST
        variables.put("productName", "");
        variables.put("quantity", "");
        variables.put("unitPrice", "");
        variables.put("totalPrice", "");
        variables.put("deliveryDays", "");
        variables.put("paymentDays", "");
        variables.put("penaltyRate", "10");
        variables.put("contractNo", "自动生成");
        variables.put("title", "");
        variables.put("amount", "");
        variables.put("startDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        variables.put("endDate", "");
        variables.put("signDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        variables.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        
        // Step 2: Override with base contract fields from contractData
        if (contractData.containsKey("contractNo")) {
            variables.put("contractNo", contractData.get("contractNo"));
        }
        if (contractData.containsKey("title")) {
            variables.put("title", contractData.get("title"));
        }
        if (contractData.containsKey("amount")) {
            Object amount = contractData.get("amount");
            variables.put("amount", amount);
            variables.put("amountChinese", numberToChinese(amount));
        }
        if (contractData.containsKey("startDate")) {
            variables.put("startDate", contractData.get("startDate"));
        }
        if (contractData.containsKey("endDate")) {
            variables.put("endDate", contractData.get("endDate"));
        }
        if (contractData.containsKey("signDate")) {
            variables.put("signDate", contractData.get("signDate"));
        }
        
        // Step 3: Add counterparty info
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> counterparties = (List<Map<String, Object>>) contractData.get("counterparties");
        if (counterparties != null && !counterparties.isEmpty()) {
            for (Map<String, Object> cp : counterparties) {
                String type = (String) cp.getOrDefault("type", "");
                String name = (String) cp.getOrDefault("name", "");
                if ("partyA".equals(type) || "甲方".equals(type)) {
                    variables.put("partyA", name);
                    variables.put("partyAContact", cp.getOrDefault("contact", ""));
                    variables.put("partyAPhone", cp.getOrDefault("phone", ""));
                    variables.put("partyAEmail", cp.getOrDefault("email", ""));
                    variables.put("partyAId", cp.getOrDefault("creditCode", ""));
                    variables.put("partyAAddress", cp.getOrDefault("address", ""));
                    variables.put("partyABank", cp.getOrDefault("bank", ""));
                    variables.put("partyAAccount", cp.getOrDefault("account", ""));
                } else if ("partyB".equals(type) || "乙方".equals(type)) {
                    variables.put("partyB", name);
                    variables.put("partyBContact", cp.getOrDefault("contact", ""));
                    variables.put("partyBPhone", cp.getOrDefault("phone", ""));
                    variables.put("partyBEmail", cp.getOrDefault("email", ""));
                    variables.put("partyBId", cp.getOrDefault("creditCode", ""));
                    variables.put("partyBAddress", cp.getOrDefault("address", ""));
                    variables.put("partyBBank", cp.getOrDefault("bank", ""));
                    variables.put("partyBAccount", cp.getOrDefault("account", ""));
                }
            }
        }
        
        // Step 4: Add dynamic field values
        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicFields = (Map<String, Object>) contractData.get("dynamicFields");
        if (dynamicFields != null) {
            variables.putAll(dynamicFields);
        }
        
        // Step 5: Add all direct variables from contractData (for variables sent directly)
        // These override any previous values
        for (Map.Entry<String, Object> entry : contractData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Skip complex objects and reserved keys that are already handled
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                if (!key.equals("contractNo") && !key.equals("title") && !key.equals("amount") 
                    && !key.equals("startDate") && !key.equals("endDate") && !key.equals("signDate")
                    && !key.equals("counterparties") && !key.equals("dynamicFields")) {
                    variables.put(key, value);
                }
            }
        }
        
        // Step 6: Add template-specific variables with defaults from template vars definition
        // Only add if not already present
        if (templateVars != null) {
            for (Map.Entry<String, String> entry : templateVars.entrySet()) {
                String varName = entry.getKey();
                if (!variables.containsKey(varName)) {
                    variables.put(varName, "");
                }
            }
        }
        
        return variables;
    }
    
    public List<Map<String, Object>> getAvailableVariables(String contractType) {
        List<Map<String, Object>> variables = new ArrayList<>();
        
        // Base contract variables
        variables.add(createVarInfo("contractNo", "合同编号", "text", true));
        variables.add(createVarInfo("title", "合同标题", "text", true));
        variables.add(createVarInfo("amount", "合同金额", "number", true));
        variables.add(createVarInfo("amountChinese", "金额大写", "text", true));
        variables.add(createVarInfo("startDate", "开始日期", "date", true));
        variables.add(createVarInfo("endDate", "结束日期", "date", true));
        variables.add(createVarInfo("signDate", "签订日期", "date", true));
        variables.add(createVarInfo("today", "当前日期", "text", true));
        
        // Counterparty variables
        variables.add(createVarInfo("partyA", "甲方名称", "text", true));
        variables.add(createVarInfo("partyAContact", "甲方联系人", "text", false));
        variables.add(createVarInfo("partyAPhone", "甲方电话", "text", false));
        variables.add(createVarInfo("partyAEmail", "甲方邮箱", "text", false));
        variables.add(createVarInfo("partyAId", "甲方证件号", "text", false));
        variables.add(createVarInfo("partyAAddress", "甲方地址", "text", false));
        variables.add(createVarInfo("partyACompany", "甲方公司", "text", false));
        
        variables.add(createVarInfo("partyB", "乙方名称", "text", true));
        variables.add(createVarInfo("partyBContact", "乙方联系人", "text", false));
        variables.add(createVarInfo("partyBPhone", "乙方电话", "text", false));
        variables.add(createVarInfo("partyBEmail", "乙方邮箱", "text", false));
        variables.add(createVarInfo("partyBId", "乙方证件号", "text", false));
        variables.add(createVarInfo("partyBAddress", "乙方地址", "text", false));
        variables.add(createVarInfo("partyBCompany", "乙方公司", "text", false));
        
        // Signature variables
        variables.add(createVarInfo("partyASign", "甲方签章", "text", false));
        variables.add(createVarInfo("partyBSign", "乙方签章", "text", false));
        
        // Dynamic fields for this contract type
        if (contractType != null && !contractType.isEmpty()) {
            List<ContractTypeField> typeFields = typeFieldMapper.selectByContractType(contractType);
            for (ContractTypeField field : typeFields) {
                Map<String, Object> varInfo = createVarInfo(
                    field.getFieldKey(),
                    field.getFieldLabel(),
                    field.getFieldType(),
                    field.getRequired() != null && field.getRequired()
                );
                varInfo.put("options", field.getOptions());
                varInfo.put("defaultValue", field.getDefaultValue());
                variables.add(varInfo);
            }
        }
        
        // Common purchase variables
        variables.add(createVarInfo("productName", "商品名称", "text", false));
        variables.add(createVarInfo("quantity", "数量", "number", false));
        variables.add(createVarInfo("unitPrice", "单价", "number", false));
        variables.add(createVarInfo("totalPrice", "总价", "number", false));
        variables.add(createVarInfo("deliveryDays", "交货天数", "number", false));
        variables.add(createVarInfo("paymentDays", "付款天数", "number", false));
        variables.add(createVarInfo("penaltyRate", "违约金比例", "number", false));
        
        // Service contract variables
        variables.add(createVarInfo("projectName", "项目名称", "text", false));
        variables.add(createVarInfo("projectDesc", "项目描述", "textarea", false));
        variables.add(createVarInfo("deliverables", "交付成果", "textarea", false));
        variables.add(createVarInfo("totalDays", "总工期", "number", false));
        variables.add(createVarInfo("totalAmount", "总金额", "number", false));
        variables.add(createVarInfo("totalAmountChinese", "金额大写", "text", false));
        variables.add(createVarInfo("firstPayment", "首付款", "number", false));
        variables.add(createVarInfo("progressPayment", "进度款", "number", false));
        variables.add(createVarInfo("finalPayment", "尾款", "number", false));
        
        // Lease contract variables
        variables.add(createVarInfo("address", "房屋地址", "text", false));
        variables.add(createVarInfo("area", "建筑面积", "number", false));
        variables.add(createVarInfo("leaseMonths", "租赁月数", "number", false));
        variables.add(createVarInfo("monthlyRent", "月租金", "number", false));
        variables.add(createVarInfo("monthlyRentChinese", "月租金大写", "text", false));
        variables.add(createVarInfo("deposit", "押金", "number", false));
        variables.add(createVarInfo("noticeDays", "通知天数", "number", false));
        
        // Employment contract variables
        variables.add(createVarInfo("companyName", "公司名称", "text", false));
        variables.add(createVarInfo("employeeName", "员工姓名", "text", false));
        variables.add(createVarInfo("employeeId", "身份证号", "text", false));
        variables.add(createVarInfo("employeePhone", "联系电话", "text", false));
        variables.add(createVarInfo("department", "部门", "text", false));
        variables.add(createVarInfo("position", "职位", "text", false));
        variables.add(createVarInfo("salary", "月薪", "number", false));
        variables.add(createVarInfo("salaryChinese", "月薪大写", "text", false));
        variables.add(createVarInfo("probationSalary", "试用期工资", "number", false));
        variables.add(createVarInfo("workLocation", "工作地点", "text", false));
        variables.add(createVarInfo("workHours", "工作时间", "text", false));
        
        return variables;
    }
    
    private Map<String, Object> createVarInfo(String key, String label, String type, boolean required) {
        Map<String, Object> info = new HashMap<>();
        info.put("key", key);
        info.put("label", label);
        info.put("type", type);
        info.put("required", required);
        return info;
    }
    
    private Map<String, String> parseTemplateVariables(String variablesJson) {
        if (variablesJson == null || variablesJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(variablesJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("模板变量定义格式错误", e);
        }
    }
    
    public String numberToChinese(Object amount) {
        if (amount == null) return "零";
        
        BigDecimal num;
        if (amount instanceof BigDecimal) {
            num = (BigDecimal) amount;
        } else if (amount instanceof Number) {
            num = new BigDecimal(amount.toString());
        } else {
            try {
                num = new BigDecimal(amount.toString());
            } catch (Exception e) {
                return "零";
            }
        }
        
        if (num.compareTo(BigDecimal.ZERO) == 0) {
            return "零";
        }
        
        String[] units = {"", "万", "亿"};
        String[] digits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] tens = {"", "拾", "佰", "仟"};
        
        StringBuilder result = new StringBuilder();
        String numStr = num.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        String[] parts = numStr.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? parts[1] : "";
        
        // Handle negative
        if (integerPart.startsWith("-")) {
            result.append("负");
            integerPart = integerPart.substring(1);
        }
        
        // Process integer part
        int len = integerPart.length();
        int unitIndex = 0;
        
        for (int i = len; i > 0; i -= 4) {
            int start = Math.max(0, i - 4);
            int end = i;
            String section = integerPart.substring(start, end);
            
            StringBuilder sectionResult = new StringBuilder();
            for (int j = 0; j < section.length(); j++) {
                int digit = section.charAt(j) - '0';
                if (digit != 0) {
                    sectionResult.append(digits[digit]);
                    sectionResult.append(tens[section.length() - j - 1]);
                } else if (sectionResult.length() > 0 && !sectionResult.toString().endsWith("零")) {
                    sectionResult.append("零");
                }
            }
            
            String sectionStr = sectionResult.toString();
            if (!sectionStr.isEmpty()) {
                result.append(sectionStr);
                if (unitIndex > 0 && !sectionStr.endsWith("零")) {
                    result.append(units[unitIndex]);
                }
            }
            unitIndex++;
        }
        
        // Remove trailing zero
        String intResult = result.toString();
        while (intResult.endsWith("零")) {
            intResult = intResult.substring(0, intResult.length() - 1);
        }
        if (intResult.isEmpty()) {
            intResult = "零";
        }
        
        // Process decimal part
        if (decimalPart.length() > 0) {
            intResult += "点";
            for (int i = 0; i < decimalPart.length() && i < 2; i++) {
                int digit = decimalPart.charAt(i) - '0';
                intResult += digits[digit];
            }
        } else {
            intResult += "元整";
        }
        
        return intResult;
    }
    
    public List<String> extractVariables(String content) {
        List<String> variables = new ArrayList<>();
        if (content == null) return variables;
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!variables.contains(varName)) {
                variables.add(varName);
            }
        }
        return variables;
    }
}
