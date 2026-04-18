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
                target.setNameEn(trimToNull(parseString(firstNonNull(item.get("nameEn"), item.get("name_en")))));
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
        // 与 TemplateController 内置五套模板中出现的 [[变量]] 对齐，并保留通用字段；sort_order 与 data.sql / Flyway 一致
        defaults.add(createVar("contractNo", "合同编号", "Contract Number", "合同编号", "text", "system", "自动生成的合同编号", 1));
        defaults.add(createVar("title", "合同名称", "Contract Title", "合同名称", "text", "system", "合同的标题", 2));
        defaults.add(createVar("amount", "合同金额", "Contract Amount", "合同金额", "number", "system", "合同总金额", 3));
        defaults.add(createVar("amountChinese", "金额大写", "Amount in Words", "金额大写", "text", "system", "金额中文大写", 4));
        defaults.add(createVar("startDate", "开始日期", "Start Date", "起始日期", "date", "system", "合同开始/起始日期", 5));
        defaults.add(createVar("endDate", "结束日期", "End Date", "结束日期", "date", "system", "合同结束日期", 6));
        defaults.add(createVar("signDate", "签订日期", "Signing Date", "签订日期", "date", "system", "合同签订日期", 7));
        defaults.add(createVar("today", "当前日期", "Today's Date", "当前日期", "text", "system", "当前日期", 8));

        defaults.add(createVar("partyA", "甲方名称", "Party A Name", "甲方公司名称", "text", "party", "甲方公司全称", 10));
        defaults.add(createVar("partyB", "乙方名称", "Party B Name", "乙方公司名称", "text", "party", "乙方公司全称", 11));
        defaults.add(createVar("partyAId", "甲方证件号", "Party A ID Number", "甲方证件号码", "text", "party", "甲方身份证明号码", 12));
        defaults.add(createVar("partyBId", "乙方证件号", "Party B ID Number", "乙方证件号码", "text", "party", "乙方身份证明号码", 13));
        defaults.add(createVar("partyAContact", "甲方联系人", "Party A Contact", "甲方联系人", "text", "party", "甲方联系人姓名", 14));
        defaults.add(createVar("partyAPhone", "甲方电话", "Party A Phone", "甲方联系电话", "text", "party", "甲方联系电话", 15));
        defaults.add(createVar("partyAEmail", "甲方邮箱", "Party A Email", "甲方邮箱", "text", "party", "甲方电子邮箱", 16));
        defaults.add(createVar("partyAAddress", "甲方地址", "Party A Address", "甲方地址", "text", "party", "甲方公司地址", 17));
        defaults.add(createVar("partyBContact", "乙方联系人", "Party B Contact", "乙方联系人", "text", "party", "乙方联系人姓名", 18));
        defaults.add(createVar("partyBPhone", "乙方电话", "Party B Phone", "乙方联系电话", "text", "party", "乙方联系电话", 19));
        defaults.add(createVar("partyBEmail", "乙方邮箱", "Party B Email", "乙方邮箱", "text", "party", "乙方电子邮箱", 20));
        defaults.add(createVar("partyBAddress", "乙方地址", "Party B Address", "乙方地址", "text", "party", "乙方公司地址", 21));
        defaults.add(createVar("partyASign", "甲方签章", "Party A Signature", "甲方签章", "text", "party", "甲方签章栏", 22));
        defaults.add(createVar("partyBSign", "乙方签章", "Party B Signature", "乙方签章", "text", "party", "乙方签章栏", 23));

        defaults.add(createVar("productName", "产品名称", "Product Name", "产品名称", "text", "purchase", "采购/代理产品名称", 30));
        defaults.add(createVar("quantity", "数量", "Quantity", "数量", "number", "purchase", "采购数量", 31));
        defaults.add(createVar("unitPrice", "单价", "Unit Price", "单价", "number", "purchase", "产品单价", 32));
        defaults.add(createVar("totalPrice", "总价", "Total Price", "总价", "number", "purchase", "产品总价", 33));
        defaults.add(createVar("deliveryDays", "交货天数", "Delivery Days", "交货天数", "number", "purchase", "交货期限（天）", 34));
        defaults.add(createVar("deliveryAddress", "交货地址", "Delivery Address", "交货地址", "text", "purchase", "交货/收货详细地址（与表单 delivery_address 对齐）", 35));
        defaults.add(createVar("paymentDays", "付款天数", "Payment Days", "付款周期", "number", "purchase", "付款期限（天）", 36));
        defaults.add(createVar("penaltyRate", "违约金比例", "Penalty Rate (%)", "违约金比例", "number", "purchase", "违约金百分比", 37));

        defaults.add(createVar("projectName", "项目名称", "Project Name", "项目名称", "text", "service", "服务项目名称", 40));
        defaults.add(createVar("projectDesc", "项目描述", "Project Description", "项目描述", "textarea", "service", "项目详细描述", 41));
        defaults.add(createVar("deliverables", "交付成果", "Deliverables", "交付成果", "textarea", "service", "交付成果物", 42));
        defaults.add(createVar("totalDays", "总工期", "Total Duration (Days)", "总工期", "number", "service", "项目总工期（工作日）", 43));
        defaults.add(createVar("totalAmount", "合同总金额", "Total Contract Amount", "合同总金额", "number", "service", "服务合同总金额（数字）", 44));
        defaults.add(createVar("totalAmountChinese", "合同总金额(大写)", "Total Amount (Chinese)", "合同总金额大写", "text", "service", "服务合同总金额中文大写", 45));
        defaults.add(createVar("firstPayment", "首付款", "Down Payment", "首付款", "number", "service", "首付款金额", 46));
        defaults.add(createVar("progressPayment", "进度款", "Progress Payment", "进度款", "number", "service", "进度款金额", 47));
        defaults.add(createVar("finalPayment", "尾款", "Final Payment", "尾款", "number", "service", "尾款金额", 48));

        defaults.add(createVar("address", "房屋地址", "Property Address", "房屋地址", "text", "lease", "租赁房屋地址", 50));
        defaults.add(createVar("area", "建筑面积", "Floor Area (sqm)", "建筑面积", "number", "lease", "建筑面积（平方米）", 51));
        defaults.add(createVar("leaseMonths", "租赁月数", "Lease Term (Months)", "租赁月数", "number", "lease", "租赁期限（月）", 52));
        defaults.add(createVar("monthlyRent", "月租金", "Monthly Rent", "月租金", "number", "lease", "每月租金", 53));
        defaults.add(createVar("monthlyRentChinese", "月租金(大写)", "Monthly Rent (Chinese)", "月租金大写", "text", "lease", "月租金中文大写", 54));
        defaults.add(createVar("deposit", "押金", "Security Deposit", "押金", "number", "lease", "租赁押金", 55));
        defaults.add(createVar("depositChinese", "押金(大写)", "Deposit (Chinese)", "押金大写", "text", "lease", "押金中文大写", 56));
        defaults.add(createVar("noticeDays", "提前通知天数", "Notice Period (Days)", "提前通知天数", "number", "lease", "提前解约书面通知天数", 57));
        defaults.add(createVar("penaltyMonths", "违约金月数", "Penalty (Months of Rent)", "违约金月数", "number", "lease", "提前解约违约金（月租金倍数）", 58));

        defaults.add(createVar("companyName", "公司名称", "Company Name", "公司名称", "text", "employment", "用人单位名称", 60));
        defaults.add(createVar("employeeName", "员工姓名", "Employee Name", "员工姓名", "text", "employment", "劳动者姓名", 61));
        defaults.add(createVar("employeeId", "身份证号", "ID Number", "身份证号", "text", "employment", "劳动者身份证号码", 62));
        defaults.add(createVar("employeePhone", "联系电话", "Phone Number", "联系电话", "text", "employment", "劳动者联系电话", 63));
        defaults.add(createVar("position", "职位", "Position", "岗位/职位", "text", "employment", "工作岗位", 64));
        defaults.add(createVar("workContent", "工作内容", "Job Duties", "工作内容", "textarea", "employment", "工作内容描述", 65));
        defaults.add(createVar("workLocation", "工作地点", "Work Location", "工作地点", "text", "employment", "工作地点", 66));
        defaults.add(createVar("salary", "月薪(通用)", "Monthly Salary (Generic)", "月薪", "number", "employment", "通用月薪（可与月工资字段并存）", 67));
        defaults.add(createVar("monthlySalary", "月工资", "Monthly Salary", "月工资", "number", "employment", "劳动合同月工资", 68));
        defaults.add(createVar("monthlySalaryChinese", "月工资(大写)", "Monthly Salary (Chinese)", "月工资大写", "text", "employment", "月工资中文大写", 69));
        defaults.add(createVar("payDay", "工资发放日", "Pay Day", "工资发放日", "number", "employment", "每月发薪日（日）", 70));
        defaults.add(createVar("probationPeriod", "试用期", "Probation Period (Months)", "试用期", "number", "employment", "试用期（月）", 71));
        defaults.add(createVar("nonCompeteMonths", "竞业限制月数", "Non-compete Period (Months)", "竞业限制月数", "number", "employment", "竞业限制期限（月）", 72));
        defaults.add(createVar("nonCompeteCompensation", "竞业限制补偿金", "Non-compete Compensation", "竞业限制补偿金", "number", "employment", "竞业限制补偿（元/月）", 73));
        defaults.add(createVar("companySign", "用人单位签章", "Company Signature", "公司签章", "text", "employment", "用人单位签章栏", 74));
        defaults.add(createVar("employeeSign", "员工签章", "Employee Signature", "员工签章", "text", "employment", "劳动者签章栏", 75));

        defaults.add(createVar("brand", "品牌", "Brand", "品牌", "text", "agency", "代理产品品牌", 80));
        defaults.add(createVar("specification", "型号规格", "Model / Specification", "型号规格", "text", "agency", "产品型号规格", 81));
        defaults.add(createVar("region", "代理区域", "Territory", "代理区域", "text", "agency", "销售代理区域", 82));
        defaults.add(createVar("annualTask", "年度销售任务", "Annual Sales Target", "年度销售任务", "number", "agency", "年度销售任务金额", 83));
        defaults.add(createVar("quarterlyTask", "季度任务", "Quarterly Target", "季度任务", "number", "agency", "季度销售任务金额", 84));
        defaults.add(createVar("consecutiveQuarters", "连续季度数", "Consecutive Quarters", "连续季度数", "number", "agency", "考核连续未完成季度数", 85));
        defaults.add(createVar("taskPercentage", "任务完成比例", "Task Completion (%)", "任务完成比例", "number", "agency", "任务完成比例阈值（%）", 86));
        defaults.add(createVar("minRetailPrice", "最低零售价", "Minimum Retail Price", "最低零售价", "number", "agency", "最低零售价格", 87));
        defaults.add(createVar("priceNoticeDays", "价格调整通知天数", "Price Change Notice (Days)", "价格调整通知天数", "number", "agency", "价格调整提前通知天数", 88));
        defaults.add(createVar("commissionRate", "佣金比例", "Commission Rate (%)", "佣金比例", "number", "agency", "佣金比例（%）", 89));
        defaults.add(createVar("salesTarget1", "销售目标1", "Sales Target 1", "销售目标1", "number", "agency", "返利档位销售额1（万）", 90));
        defaults.add(createVar("rebateRate1", "返利比例1", "Rebate Rate 1 (%)", "返利比例1", "number", "agency", "档位1返利比例（%）", 91));
        defaults.add(createVar("salesTarget2", "销售目标2", "Sales Target 2", "销售目标2", "number", "agency", "返利档位销售额2（万）", 92));
        defaults.add(createVar("rebateRate2", "返利比例2", "Rebate Rate 2 (%)", "返利比例2", "number", "agency", "档位2返利比例（%）", 93));
        defaults.add(createVar("samplesCount", "样品套数", "Sample Sets", "样品数量", "number", "agency", "市场支持样品套数", 94));
        defaults.add(createVar("promoMaterialsCount", "宣传物料份数", "Promo Materials (Copies)", "宣传物料数量", "number", "agency", "宣传物料份数", 95));
        defaults.add(createVar("adSupportAmount", "广告支持金额", "Ad Support Amount", "广告支持金额", "number", "agency", "广告支持金额（元）", 96));

        Map<String, Object> payload = new HashMap<>();
        payload.put("items", defaults);
        payload.put("conflictPolicy", "overwrite");
        batchCreate(payload);
        return ApiResponse.success("已初始化 " + defaults.size() + " 个默认变量（含内置模板全部占位符）");
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
    
    private static Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object v : values) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private Map<String, Object> createVar(String code, String name, String nameEn, String label, String type, String category, String desc, int sortOrder) {
        Map<String, Object> v = new HashMap<>();
        v.put("code", code);
        v.put("name", name);
        v.put("nameEn", nameEn);
        v.put("label", label);
        v.put("type", type);
        v.put("category", category);
        v.put("description", desc);
        v.put("sortOrder", sortOrder);
        v.put("status", 1);
        return v;
    }
}
