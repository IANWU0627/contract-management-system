package com.contracthub.service.impl;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractCounterparty;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.entity.User;
import com.contracthub.entity.ApprovalRecord;
import com.contracthub.mapper.ContractCounterpartyMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.mapper.ContractTagMapper;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.ApprovalRecordMapper;
import com.contracthub.service.ContractService;
import com.contracthub.service.ContractNumberService;
import com.contracthub.service.NotificationService;
import com.contracthub.service.ContractVersionService;
import com.contracthub.service.ContractChangeLogService;
import com.contracthub.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContractServiceImpl implements ContractService {
    private static final int CONTRACT_NO_RETRY_TIMES = 5;

    private final ContractMapper contractMapper;
    private final ContractCounterpartyMapper counterpartyMapper;
    private final ContractFieldValueMapper fieldValueMapper;
    private final ContractTagMapper tagMapper;
    private final ContractTypeFieldMapper typeFieldMapper;
    private final UserMapper userMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ContractNumberService contractNumberService;
    private final NotificationService notificationService;
    private final ContractVersionService contractVersionService;
    private final ContractChangeLogService contractChangeLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractServiceImpl(ContractMapper contractMapper,
                                ContractCounterpartyMapper counterpartyMapper,
                                ContractFieldValueMapper fieldValueMapper,
                                ContractTagMapper tagMapper,
                                ContractTypeFieldMapper typeFieldMapper,
                                UserMapper userMapper,
                                ApprovalRecordMapper approvalRecordMapper,
                                ContractNumberService contractNumberService,
                                NotificationService notificationService,
                                ContractVersionService contractVersionService,
                                ContractChangeLogService contractChangeLogService) {
        this.contractMapper = contractMapper;
        this.counterpartyMapper = counterpartyMapper;
        this.fieldValueMapper = fieldValueMapper;
        this.tagMapper = tagMapper;
        this.typeFieldMapper = typeFieldMapper;
        this.userMapper = userMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.contractNumberService = contractNumberService;
        this.notificationService = notificationService;
        this.contractVersionService = contractVersionService;
        this.contractChangeLogService = contractChangeLogService;
    }

    @Override
    public IPage<Contract> listContracts(Map<String, Object> params, int page, int pageSize) {
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>();
        
        String title = (String) params.get("title");
        String type = (String) params.get("type");
        String status = (String) params.get("status");
        String counterparty = (String) params.get("counterparty");
        String contractNo = (String) params.get("contractNo");
        String startDateFrom = (String) params.get("startDateFrom");
        String startDateTo = (String) params.get("startDateTo");
        String endDateFrom = (String) params.get("endDateFrom");
        String endDateTo = (String) params.get("endDateTo");
        Double amountMin = params.get("amountMin") != null ? ((Number) params.get("amountMin")).doubleValue() : null;
        Double amountMax = params.get("amountMax") != null ? ((Number) params.get("amountMax")).doubleValue() : null;
        String keyword = (String) params.get("keyword");
        Long folderId = params.get("folderId") != null ? ((Number) params.get("folderId")).longValue() : null;
        String sortBy = (String) params.get("sortBy");
        String sortOrder = (String) params.getOrDefault("sortOrder", "desc");

        if (title != null && !title.isEmpty()) {
            queryWrapper.like("title", title);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq("type", type);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (counterparty != null && !counterparty.isEmpty()) {
            queryWrapper.like("counterparty", counterparty);
        }
        if (folderId != null) {
            queryWrapper.eq("folder_id", folderId);
        }
        if (contractNo != null && !contractNo.isEmpty()) {
            queryWrapper.like("contract_no", contractNo);
        }
        if (startDateFrom != null && !startDateFrom.isEmpty()) {
            queryWrapper.ge("start_date", startDateFrom);
        }
        if (startDateTo != null && !startDateTo.isEmpty()) {
            queryWrapper.le("start_date", startDateTo);
        }
        if (endDateFrom != null && !endDateFrom.isEmpty()) {
            queryWrapper.ge("end_date", endDateFrom);
        }
        if (endDateTo != null && !endDateTo.isEmpty()) {
            queryWrapper.le("end_date", endDateTo);
        }
        if (amountMin != null) {
            queryWrapper.ge("amount", amountMin);
        }
        if (amountMax != null) {
            queryWrapper.le("amount", amountMax);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("contract_no", keyword)
                .or()
                .like("counterparty", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("remark", keyword)
                .or()
                .like("type", keyword)
                .or()
                .like("status", keyword)
            );
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            String dbColumn = camelToUnderscore(sortBy);
            if ("asc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByAsc(dbColumn);
            } else {
                queryWrapper.orderByDesc(dbColumn);
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<Contract> mpPage = new Page<>(page, pageSize);
        return contractMapper.selectPage(mpPage, queryWrapper);
    }

    @Override
    public Contract getContractById(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    @Transactional
    public Contract createContract(Map<String, Object> contractMap) {
        Contract contract = new Contract();
        assignNextContractNo(contract);
        contract.setTitle((String) contractMap.get("title"));
        contract.setType((String) contractMap.get("type"));
        contract.setRemark((String) contractMap.get("remark"));
        
        // 处理相对方信息
        List<Map<String, Object>> counterpartiesList = null;
        if (contractMap.containsKey("counterparties")) {
            Object counterpartiesObj = contractMap.get("counterparties");
            if (counterpartiesObj instanceof List) {
                counterpartiesList = (List<Map<String, Object>>) counterpartiesObj;
                try {
                    contract.setCounterparties(objectMapper.writeValueAsString(counterpartiesList));
                } catch (Exception e) {
                    throw new RuntimeException("保存相对方信息失败", e);
                }
                // 为了兼容旧数据，使用第一个相对方的名称作为counterparty字段
                if (!counterpartiesList.isEmpty()) {
                    Map<String, Object> firstCp = counterpartiesList.get(0);
                    if (firstCp.containsKey("name")) {
                        contract.setCounterparty((String) firstCp.get("name"));
                    }
                }
            }
        }
        // 如果没有counterparties字段，使用旧的counterparty字段
        if (contract.getCounterparty() == null && contractMap.containsKey("counterparty")) {
            contract.setCounterparty((String) contractMap.get("counterparty"));
        }
        // 数据库中 counterparty 为 NOT NULL，前端传空列表时兜底为空串避免 500。
        if (contract.getCounterparty() == null) {
            contract.setCounterparty("");
        }
        
        // 处理金额
        Object amountObj = contractMap.get("amount");
        if (amountObj != null) {
            if (amountObj instanceof Number) {
                contract.setAmount(java.math.BigDecimal.valueOf(((Number) amountObj).doubleValue()));
            } else if (amountObj instanceof String) {
                contract.setAmount(new java.math.BigDecimal((String) amountObj));
            }
        }
        contract.setCurrency((String) contractMap.getOrDefault("currency", "CNY"));
        
        // 处理日期
        Object startDateObj = contractMap.get("startDate");
        if (startDateObj != null) {
            contract.setStartDate(java.time.LocalDate.parse(startDateObj.toString()));
        }
        
        Object endDateObj = contractMap.get("endDate");
        if (endDateObj != null) {
            contract.setEndDate(java.time.LocalDate.parse(endDateObj.toString()));
        }
        
        contract.setStatus((String) contractMap.getOrDefault("status", "DRAFT"));
        contract.setContent((String) contractMap.get("content"));
        
        // 处理模板相关字段
        Object templateIdObj = contractMap.get("templateId");
        if (templateIdObj != null) {
            if (templateIdObj instanceof Number) {
                contract.setTemplateId(((Number) templateIdObj).longValue());
            } else if (templateIdObj != null) {
                try {
                    contract.setTemplateId(Long.parseLong(templateIdObj.toString()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        
        if (contractMap.containsKey("contentMode") && contractMap.get("contentMode") != null) {
            contract.setContentMode((String) contractMap.get("contentMode"));
        }
        
        if (contractMap.containsKey("templateVariables") && contractMap.get("templateVariables") != null) {
            Object templateVars = contractMap.get("templateVariables");
            if (templateVars instanceof String) {
                contract.setTemplateVariables((String) templateVars);
            } else {
                try {
                    contract.setTemplateVariables(objectMapper.writeValueAsString(templateVars));
                } catch (Exception e) {
                    throw new RuntimeException("保存模板变量失败", e);
                }
            }
        }
        
        // 处理附件信息
        List<Map<String, Object>> attachmentsList = null;
        if (contractMap.containsKey("attachments")) {
            Object attachmentsObj = contractMap.get("attachments");
            if (attachmentsObj instanceof List) {
                attachmentsList = (List<Map<String, Object>>) attachmentsObj;
                try {
                    contract.setAttachments(objectMapper.writeValueAsString(attachmentsList));
                } catch (Exception e) {
                    throw new RuntimeException("保存附件信息失败", e);
                }
                // 为了兼容旧数据，使用第一个附件的名称作为attachment字段
                if (!attachmentsList.isEmpty()) {
                    Map<String, Object> firstAttachment = attachmentsList.get(0);
                    if (firstAttachment.containsKey("name")) {
                        contract.setAttachment((String) firstAttachment.get("name"));
                    }
                }
            }
        } else if (contractMap.containsKey("attachment")) {
            contract.setAttachment((String) contractMap.get("attachment"));
        }
        
        // 处理文件夹
        Object folderIdObj = contractMap.get("folderId");
        if (folderIdObj != null) {
            if (folderIdObj instanceof Number) {
                contract.setFolderId(((Number) folderIdObj).longValue());
            } else if (folderIdObj != null) {
                contract.setFolderId(Long.parseLong(folderIdObj.toString()));
            }
        }
        
        // 处理时区
        if (contractMap.containsKey("timezone") && contractMap.get("timezone") != null) {
            contract.setTimezone((String) contractMap.get("timezone"));
        } else {
            contract.setTimezone(java.time.ZoneId.systemDefault().getId());
        }
        
        contract.setCreatorId(SecurityUtils.getCurrentUserId());
        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateTime(LocalDateTime.now());
        
        insertContractWithRetry(contract);

        // 同步保存相对方到独立表（contract_counterparty）
        saveCounterpartiesToTable(contract.getId(), resolveCounterparties(counterpartiesList, contract.getCounterparty()));
        
        Map<String, Object> dynamicFields = resolveDynamicFields(contractMap);
        if (!dynamicFields.isEmpty()) {
            try {
                contract.setDynamicFieldValues(objectMapper.writeValueAsString(dynamicFields));
                contractMapper.updateById(contract);
            } catch (Exception ignored) {
                // Keep create flow resilient even if JSON serialization fails.
            }
            for (Map.Entry<String, Object> entry : dynamicFields.entrySet()) {
                ContractFieldValue fv = new ContractFieldValue();
                fv.setContractId(contract.getId());
                fv.setFieldKey(entry.getKey());
                fv.setFieldValue(entry.getValue() != null ? entry.getValue().toString() : null);
                fieldValueMapper.insert(fv);
            }
        }
        
        // 创建合同后记录版本和变更
        try {
            Long operatorId = SecurityUtils.getCurrentUserId();
            String operatorName = SecurityUtils.getCurrentUserName();
            if (operatorName == null || operatorName.isEmpty()) {
                operatorName = "admin";
            }
            
            // 1. 创建版本记录
            String contentForVersion = contract.getContent() != null ? contract.getContent() : "";
            contractVersionService.createVersion(
                contract.getId(),
                contentForVersion,
                "创建合同",
                operatorId,
                operatorName
            );
            
            // 2. 记录创建变更
            contractChangeLogService.logChange(
                contract.getId(), "CREATE", null,
                null, null,
                operatorId, operatorName, "创建合同"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract updateContract(Long id, Map<String, Object> contractMap) {
        // 查询合同
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        // 保存原始合同内容用于对比和版本记录
        Contract oldContract = new Contract();
        oldContract.setTitle(contract.getTitle());
        oldContract.setType(contract.getType());
        oldContract.setRemark(contract.getRemark());
        oldContract.setCounterparty(contract.getCounterparty());
        oldContract.setCounterparties(contract.getCounterparties());
        oldContract.setAmount(contract.getAmount());
        oldContract.setCurrency(contract.getCurrency());
        oldContract.setStartDate(contract.getStartDate());
        oldContract.setEndDate(contract.getEndDate());
        oldContract.setStatus(contract.getStatus());
        oldContract.setContent(contract.getContent());
        
        // 获取当前用户信息
        Long operatorId = SecurityUtils.getCurrentUserId();
        String operatorName = SecurityUtils.getCurrentUserName();
        if (operatorName == null || operatorName.isEmpty()) {
            operatorName = "admin";
        }
        
        // 更新字段
        if (contractMap.containsKey("title")) {
            contract.setTitle((String) contractMap.get("title"));
        }
        if (contractMap.containsKey("type")) {
            contract.setType((String) contractMap.get("type"));
        }
        if (contractMap.containsKey("remark")) {
            contract.setRemark((String) contractMap.get("remark"));
        }
        
        // 处理相对方信息
        List<Map<String, Object>> counterpartiesList = null;
        if (contractMap.containsKey("counterparties")) {
            Object counterpartiesObj = contractMap.get("counterparties");
            if (counterpartiesObj instanceof List) {
                counterpartiesList = (List<Map<String, Object>>) counterpartiesObj;
                try {
                    contract.setCounterparties(objectMapper.writeValueAsString(counterpartiesList));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 为了兼容旧数据，使用第一个相对方的名称作为counterparty字段
                if (!counterpartiesList.isEmpty()) {
                    Map<String, Object> firstCp = counterpartiesList.get(0);
                    if (firstCp.containsKey("name")) {
                        contract.setCounterparty((String) firstCp.get("name"));
                    }
                }
            }
        } else if (contractMap.containsKey("counterparty")) {
            contract.setCounterparty((String) contractMap.get("counterparty"));
        }
        
        // 处理金额
        if (contractMap.containsKey("amount")) {
            Object amountObj = contractMap.get("amount");
            if (amountObj != null) {
                if (amountObj instanceof Number) {
                    contract.setAmount(java.math.BigDecimal.valueOf(((Number) amountObj).doubleValue()));
                } else if (amountObj instanceof String) {
                    contract.setAmount(new java.math.BigDecimal((String) amountObj));
                }
            }
        }
        if (contractMap.containsKey("currency")) {
            contract.setCurrency((String) contractMap.get("currency"));
        }
        
        // 处理日期
        if (contractMap.containsKey("startDate")) {
            String startDateStr = (String) contractMap.get("startDate");
            if (startDateStr != null) {
                contract.setStartDate(java.time.LocalDate.parse(startDateStr));
            }
        }
        if (contractMap.containsKey("endDate")) {
            String endDateStr = (String) contractMap.get("endDate");
            if (endDateStr != null) {
                contract.setEndDate(java.time.LocalDate.parse(endDateStr));
            }
        }
        
        if (contractMap.containsKey("status")) {
            contract.setStatus((String) contractMap.get("status"));
        }
        if (contractMap.containsKey("content")) {
            contract.setContent((String) contractMap.get("content"));
        }
        
        // 处理模板相关字段
        if (contractMap.containsKey("templateId")) {
            Object templateIdObj = contractMap.get("templateId");
            if (templateIdObj != null) {
                try {
                    if (templateIdObj instanceof Number) {
                        contract.setTemplateId(((Number) templateIdObj).longValue());
                    } else {
                        contract.setTemplateId(Long.parseLong(templateIdObj.toString()));
                    }
                } catch (NumberFormatException e) {
                    contract.setTemplateId(null);
                }
            } else {
                contract.setTemplateId(null);
            }
        }
        
        if (contractMap.containsKey("contentMode")) {
            contract.setContentMode((String) contractMap.get("contentMode"));
        }
        
        if (contractMap.containsKey("templateVariables")) {
            Object templateVars = contractMap.get("templateVariables");
            if (templateVars instanceof String) {
                contract.setTemplateVariables((String) templateVars);
            } else if (templateVars != null) {
                try {
                    contract.setTemplateVariables(objectMapper.writeValueAsString(templateVars));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 处理文件夹
        if (contractMap.containsKey("folderId")) {
            Object folderIdObj = contractMap.get("folderId");
            if (folderIdObj instanceof Number) {
                contract.setFolderId(((Number) folderIdObj).longValue());
            } else if (folderIdObj != null) {
                contract.setFolderId(Long.parseLong(folderIdObj.toString()));
            }
        }
        
        // 处理附件信息
        if (contractMap.containsKey("attachments")) {
            Object attachmentsObj = contractMap.get("attachments");
            if (attachmentsObj instanceof List) {
                List<Map<String, Object>> attachmentsList = (List<Map<String, Object>>) attachmentsObj;
                try {
                    contract.setAttachments(objectMapper.writeValueAsString(attachmentsList));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 为了兼容旧数据，使用第一个附件的名称作为attachment字段
                if (!attachmentsList.isEmpty()) {
                    Map<String, Object> firstAttachment = attachmentsList.get(0);
                    if (firstAttachment.containsKey("name")) {
                        contract.setAttachment((String) firstAttachment.get("name"));
                    }
                }
            }
        } else if (contractMap.containsKey("attachment")) {
            contract.setAttachment((String) contractMap.get("attachment"));
        }
        
        contract.setUpdateTime(LocalDateTime.now());
        
        // 相对方字段有变化时，同步更新独立表（contract_counterparty）
        if (contractMap.containsKey("counterparties") || contractMap.containsKey("counterparty")) {
            saveCounterpartiesToTable(id, resolveCounterparties(counterpartiesList, contract.getCounterparty()));
        }
        
        Map<String, Object> dynamicFields = resolveDynamicFields(contractMap);
        if (!dynamicFields.isEmpty()) {
            try {
                contract.setDynamicFieldValues(objectMapper.writeValueAsString(dynamicFields));
            } catch (Exception ignored) {
                // Keep update flow resilient even if JSON serialization fails.
            }
        } else if (contractMap.containsKey("dynamicFields") || contractMap.containsKey("dynamicFieldValues")) {
            contract.setDynamicFieldValues("{}");
        }
        contractMapper.updateById(contract);

        if (contractMap.containsKey("dynamicFields") || contractMap.containsKey("dynamicFieldValues")) {
            // 先删除旧的动态字段
            fieldValueMapper.deleteByContractId(id);
            // 插入新的动态字段
            for (Map.Entry<String, Object> entry : dynamicFields.entrySet()) {
                ContractFieldValue fv = new ContractFieldValue();
                fv.setContractId(id);
                fv.setFieldKey(entry.getKey());
                fv.setFieldValue(entry.getValue() != null ? entry.getValue().toString() : null);
                fieldValueMapper.insert(fv);
            }
        }
        
        // 记录合同变更
        try {
            // 1. 创建版本记录
            String contentForVersion = contract.getContent() != null ? contract.getContent() : "";
            contractVersionService.createVersion(
                contract.getId(),
                contentForVersion,
                "更新合同",
                operatorId,
                operatorName
            );
            
            // 2. 记录字段变更
            if (!Objects.equals(oldContract.getTitle(), contract.getTitle())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "title",
                    oldContract.getTitle(), contract.getTitle(),
                    operatorId, operatorName, "标题变更"
                );
            }
            
            if (!Objects.equals(oldContract.getType(), contract.getType())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "type",
                    oldContract.getType(), contract.getType(),
                    operatorId, operatorName, "合同类型变更"
                );
            }
            
            if (!Objects.equals(oldContract.getRemark(), contract.getRemark())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "remark",
                    oldContract.getRemark(), contract.getRemark(),
                    operatorId, operatorName, "备注变更"
                );
            }
            
            if (!Objects.equals(oldContract.getCounterparty(), contract.getCounterparty())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "counterparty",
                    oldContract.getCounterparty(), contract.getCounterparty(),
                    operatorId, operatorName, "相对方变更"
                );
            }
            
            if (!Objects.equals(oldContract.getAmount(), contract.getAmount())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "amount",
                    oldContract.getAmount() != null ? oldContract.getAmount().toString() : null,
                    contract.getAmount() != null ? contract.getAmount().toString() : null,
                    operatorId, operatorName, "金额变更"
                );
            }
            
            if (!Objects.equals(oldContract.getStartDate(), contract.getStartDate())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "startDate",
                    oldContract.getStartDate() != null ? oldContract.getStartDate().toString() : null,
                    contract.getStartDate() != null ? contract.getStartDate().toString() : null,
                    operatorId, operatorName, "开始日期变更"
                );
            }
            
            if (!Objects.equals(oldContract.getEndDate(), contract.getEndDate())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "endDate",
                    oldContract.getEndDate() != null ? oldContract.getEndDate().toString() : null,
                    contract.getEndDate() != null ? contract.getEndDate().toString() : null,
                    operatorId, operatorName, "结束日期变更"
                );
            }
            
            if (!Objects.equals(oldContract.getStatus(), contract.getStatus())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "status",
                    oldContract.getStatus(), contract.getStatus(),
                    operatorId, operatorName, "状态变更"
                );
            }
            
            if (!Objects.equals(oldContract.getContent(), contract.getContent())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "content",
                    "内容变更", "内容已更新",
                    operatorId, operatorName, "合同内容变更"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return contract;
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        int result = contractMapper.deleteById(id);
        if (result == 0) {
            throw new RuntimeException("合同不存在");
        }
        // 删除动态字段值
        fieldValueMapper.deleteByContractId(id);
    }

    @Override
    @Transactional
    public void batchDeleteContracts(java.util.List<Long> ids) {
        if (ids != null) {
            for (Long id : ids) {
                deleteContract(id);
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Object> batchUpdateStatus(Map<String, Object> request) {
        List<Object> idsObj = (List<Object>) request.get("ids");
        String status = (String) request.get("status");
        
        if (idsObj == null || idsObj.isEmpty()) {
            throw new RuntimeException("请选择要更新的合同");
        }
        if (status == null || status.isEmpty()) {
            throw new RuntimeException("请选择目标状态");
        }
        
        // 转换ids为Long类型
        List<Long> ids = idsObj.stream()
            .filter(id -> id instanceof Number)
            .map(id -> ((Number) id).longValue())
            .collect(java.util.stream.Collectors.toList());
        
        int successCount = 0;
        List<Map<String, Object>> failedItems = new ArrayList<>();
        
        for (Long id : ids) {
            try {
                Contract contract = contractMapper.selectById(id);
                if (contract != null) {
                    contract.setStatus(status);
                    contract.setUpdateTime(LocalDateTime.now());
                    contractMapper.updateById(contract);
                    successCount++;
                } else {
                    failedItems.add(Map.of("id", id, "reason", "合同不存在"));
                }
            } catch (Exception e) {
                failedItems.add(Map.of("id", id, "reason", e.getMessage()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedItems.size());
        result.put("failedItems", failedItems);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> batchEditContracts(Map<String, Object> request) {
        List<Object> idsObj = (List<Object>) request.get("ids");
        
        if (idsObj == null || idsObj.isEmpty()) {
            throw new RuntimeException("请选择要编辑的合同");
        }
        
        String type = (String) request.get("type");
        String counterparty = (String) request.get("counterparty");
        String remark = (String) request.get("remark");
        
        if ((type == null || type.isEmpty()) && 
            (counterparty == null || counterparty.isEmpty()) && 
            (remark == null || remark.isEmpty())) {
            throw new RuntimeException("请选择要更新的字段");
        }
        
        // 转换ids为Long类型
        List<Long> ids = idsObj.stream()
            .filter(id -> id instanceof Number)
            .map(id -> ((Number) id).longValue())
            .collect(java.util.stream.Collectors.toList());
        
        int successCount = 0;
        List<Map<String, Object>> failedItems = new ArrayList<>();
        
        for (Long id : ids) {
            try {
                Contract contract = contractMapper.selectById(id);
                if (contract != null) {
                    if (type != null && !type.isEmpty()) {
                        contract.setType(type);
                    }
                    if (counterparty != null) {
                        contract.setCounterparty(counterparty);
                    }
                    if (remark != null) {
                        contract.setRemark(remark);
                    }
                    contract.setUpdateTime(LocalDateTime.now());
                    contractMapper.updateById(contract);
                    successCount++;
                } else {
                    failedItems.add(Map.of("id", id, "reason", "合同不存在"));
                }
            } catch (Exception e) {
                failedItems.add(Map.of("id", id, "reason", e.getMessage()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedItems.size());
        result.put("failedItems", failedItems);
        return result;
    }

    @Override
    @Transactional
    public Contract submitForApproval(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        contract.setStatus("PENDING");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知所有管理员/审批者有新合同待审批
        notifyApprovers(contract);
        
        return contract;
    }

    @Override
    @Transactional
    public Contract approveContract(Long id, String comment) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        // 保存审批记录
        saveApprovalRecord(id, "APPROVE", comment);
        
        contract.setStatus("APPROVED");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知合同创建者审批已通过
        if (contract.getCreatorId() != null) {
            notificationService.sendApprovalResult(contract.getCreatorId(), contract.getId(), 
                contract.getContractNo(), "APPROVED", comment);
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract rejectContract(Long id, String comment) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        // 保存审批记录
        saveApprovalRecord(id, "REJECT", comment);
        
        contract.setStatus("DRAFT");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知合同创建者审批被拒绝
        if (contract.getCreatorId() != null) {
            notificationService.sendApprovalResult(contract.getCreatorId(), contract.getId(), 
                contract.getContractNo(), "REJECTED", comment);
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract signContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        contract.setStatus("SIGNED");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知合同创建者合同已签署
        if (contract.getCreatorId() != null) {
            notificationService.sendContractUpdate(contract.getCreatorId(), contract.getId(), 
                contract.getContractNo(), "签署完成");
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract archiveContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        contract.setStatus("ARCHIVED");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知合同创建者合同已归档
        if (contract.getCreatorId() != null) {
            notificationService.sendContractUpdate(contract.getCreatorId(), contract.getId(), 
                contract.getContractNo(), "已归档");
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract terminateContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        contract.setStatus("TERMINATED");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        
        // 通知合同创建者合同已终止
        if (contract.getCreatorId() != null) {
            notificationService.sendContractUpdate(contract.getCreatorId(), contract.getId(), 
                contract.getContractNo(), "已终止");
        }
        
        return contract;
    }

    @Override
    @Transactional
    public Contract copyContract(Long id) {
        Contract originalContract = contractMapper.selectById(id);
        if (originalContract == null) {
            throw new RuntimeException("合同不存在");
        }
        
        // 创建新合同
        Contract newContract = new Contract();
        assignNextContractNo(newContract);
        newContract.setTitle(originalContract.getTitle() + " (副本)");
        newContract.setType(originalContract.getType());
        newContract.setCounterparty(originalContract.getCounterparty());
        newContract.setCounterparties(originalContract.getCounterparties());
        newContract.setAmount(originalContract.getAmount());
        newContract.setCurrency(originalContract.getCurrency());
        newContract.setStartDate(originalContract.getStartDate());
        newContract.setEndDate(originalContract.getEndDate());
        newContract.setStatus("DRAFT");
        newContract.setContent(originalContract.getContent());
        newContract.setRemark(originalContract.getRemark());
        newContract.setTemplateId(originalContract.getTemplateId());
        newContract.setContentMode(originalContract.getContentMode());
        newContract.setTemplateVariables(originalContract.getTemplateVariables());
        newContract.setDynamicFieldValues(originalContract.getDynamicFieldValues());
        newContract.setFolderId(originalContract.getFolderId());
        newContract.setCreatorId(SecurityUtils.getCurrentUserId());
        newContract.setCreateTime(LocalDateTime.now());
        newContract.setUpdateTime(LocalDateTime.now());
        
        insertContractWithRetry(newContract);
        
        // 复制动态字段值
        List<ContractFieldValue> fieldValues = fieldValueMapper.selectByContractId(id);
        for (ContractFieldValue fv : fieldValues) {
            ContractFieldValue newFv = new ContractFieldValue();
            newFv.setContractId(newContract.getId());
            newFv.setFieldKey(fv.getFieldKey());
            newFv.setFieldValue(fv.getFieldValue());
            fieldValueMapper.insert(newFv);
        }
        
        // 记录版本和变更
        try {
            Long operatorId = SecurityUtils.getCurrentUserId();
            String operatorName = SecurityUtils.getCurrentUserName();
            if (operatorName == null || operatorName.isEmpty()) {
                operatorName = "admin";
            }
            
            // 创建版本记录
            String contentForVersion = newContract.getContent() != null ? newContract.getContent() : "";
            contractVersionService.createVersion(
                newContract.getId(),
                contentForVersion,
                "复制合同",
                operatorId,
                operatorName
            );
            
            // 记录变更
            contractChangeLogService.logChange(
                newContract.getId(), "CREATE", null,
                null, null,
                operatorId, operatorName, "从合同 " + originalContract.getContractNo() + " 复制"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return newContract;
    }

    private void assignNextContractNo(Contract contract) {
        contract.setContractNo(contractNumberService.generateNextContractNo());
    }

    private void insertContractWithRetry(Contract contract) {
        for (int i = 0; i < CONTRACT_NO_RETRY_TIMES; i++) {
            try {
                contractMapper.insert(contract);
                return;
            } catch (DuplicateKeyException e) {
                if (i == CONTRACT_NO_RETRY_TIMES - 1) {
                    throw new RuntimeException("合同编号生成冲突，请稍后重试", e);
                }
                assignNextContractNo(contract);
            }
        }
    }
    
    // 辅助方法：通知审批者
    private void notifyApprovers(Contract contract) {
        // 获取所有管理员/审批者
        List<User> approvers = userMapper.selectList(
            new QueryWrapper<User>().in("role", Arrays.asList("ADMIN", "LEGAL"))
        );
        
        for (User approver : approvers) {
            notificationService.sendApprovalRequest(approver.getId(), contract.getId(), 
                contract.getContractNo(), contract.getTitle());
        }
    }
    
    // 辅助方法：保存审批记录
    private void saveApprovalRecord(Long contractId, String action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setContractId(contractId);
        record.setApproverId(SecurityUtils.getCurrentUserId());
        record.setApproverName(SecurityUtils.getCurrentUserName());
        record.setStatus(action);
        record.setComment(comment);
        record.setCreateTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    @Override
    public Map<String, Object> buildContractResponse(Contract contract) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", contract.getId());
        result.put("contractNo", contract.getContractNo());
        result.put("title", contract.getTitle());
        result.put("type", contract.getType());
        result.put("counterparty", contract.getCounterparty());
        result.put("amount", contract.getAmount());
        result.put("currency", contract.getCurrency());
        result.put("startDate", contract.getStartDate());
        result.put("endDate", contract.getEndDate());
        result.put("status", contract.getStatus());
        result.put("content", contract.getContent());
        result.put("attachment", contract.getAttachment());
        result.put("remark", contract.getRemark());
        result.put("createdBy", "admin");
        result.put("createdAt", contract.getCreateTime());
        result.put("updatedAt", contract.getUpdateTime());
        result.put("timezone", contract.getTimezone());
        result.put("creatorId", contract.getCreatorId());
        result.put("folderId", contract.getFolderId());
        result.put("templateId", contract.getTemplateId());
        result.put("contentMode", contract.getContentMode());
        result.put("templateVariables", contract.getTemplateVariables());
        
        // 添加相对方列表：优先读取独立表，兼容历史 JSON/单字段
        result.put("counterparties", loadCounterpartiesAsMaps(contract));
        
        // 添加附件列表
        result.put("attachments", processAttachments(contract));
        
        // 添加动态字段值
        Map<String, Object> dynamicFields = new HashMap<>();
        List<ContractFieldValue> fieldValues = fieldValueMapper.selectByContractId(contract.getId());
        for (ContractFieldValue fv : fieldValues) {
            dynamicFields.put(fv.getFieldKey(), fv.getFieldValue());
        }
        result.put("dynamicFields", dynamicFields);
        
        // 添加标签列表
        List<Map<String, Object>> tags = tagMapper.selectTagsByContractId(contract.getId());
        result.put("tags", tags);
        
        return result;
    }

    @Override
    public Map<String, Object> buildContractDetailResponse(Contract contract) {
        Map<String, Object> result = buildContractResponse(contract);
        
        // 添加动态字段定义
        if (contract.getType() != null) {
            List<ContractTypeField> fieldDefs = typeFieldMapper.selectByContractType(contract.getType());
            result.put("fieldDefinitions", fieldDefs);
        }
        
        return result;
    }
    
    // 处理附件信息的辅助方法
    private List<Map<String, Object>> processAttachments(Contract contract) {
        List<Map<String, Object>> attachments = new ArrayList<>();
        if (contract.getAttachments() != null && !contract.getAttachments().isEmpty()) {
            try {
                attachments = objectMapper.readValue(contract.getAttachments(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                // 规范化附件数据，确保包含必要的字段
                for (Map<String, Object> att : attachments) {
                    if (!att.containsKey("size") || att.get("size") == null) {
                        att.put("size", 0L);
                    }
                    if (!att.containsKey("uploadTime") || att.get("uploadTime") == null) {
                        att.put("uploadTime", contract.getCreateTime() != null ? contract.getCreateTime().toString() : java.time.LocalDate.now().toString());
                    }
                    if (att.containsKey("name") && !att.containsKey("url")) {
                        att.put("url", "/api/contracts/download/" + att.get("name"));
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，尝试使用旧的attachment字段
                if (contract.getAttachment() != null && !contract.getAttachment().isEmpty()) {
                    Map<String, Object> attachment = createLegacyAttachment(contract);
                    attachments.add(attachment);
                }
            }
        } else if (contract.getAttachment() != null && !contract.getAttachment().isEmpty()) {
            // 兼容旧数据
            Map<String, Object> attachment = createLegacyAttachment(contract);
            attachments.add(attachment);
        }
        return attachments;
    }
    
    // 创建遗留附件信息
    private Map<String, Object> createLegacyAttachment(Contract contract) {
        Map<String, Object> attachment = new HashMap<>();
        String legacyFileName = contract.getAttachment();
        attachment.put("name", legacyFileName);
        attachment.put("originalName", legacyFileName);
        attachment.put("uploadTime", contract.getCreateTime() != null ? contract.getCreateTime().toString() : java.time.LocalDate.now().toString());
        attachment.put("url", "/api/contracts/download/" + legacyFileName);
        
        // 尝试获取真实文件大小
        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            File file = new File(uploadDir, legacyFileName);
            if (file.exists()) {
                attachment.put("size", file.length());
            } else {
                attachment.put("size", 0L);
            }
        } catch (Exception e) {
            attachment.put("size", 0L);
        }
        
        return attachment;
    }

    private String camelToUnderscore(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private Map<String, Object> resolveDynamicFields(Map<String, Object> contractMap) {
        Object dynamicFieldsObj = contractMap.get("dynamicFields");
        if (dynamicFieldsObj instanceof Map<?, ?> mapObj) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }

        Object dynamicFieldValuesObj = contractMap.get("dynamicFieldValues");
        if (dynamicFieldValuesObj instanceof Map<?, ?> mapObj) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }
        if (dynamicFieldValuesObj instanceof String valuesStr && !valuesStr.isBlank()) {
            try {
                return objectMapper.readValue(valuesStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private List<Map<String, Object>> resolveCounterparties(List<Map<String, Object>> counterpartiesList, String legacyCounterparty) {
        if (counterpartiesList != null) {
            return counterpartiesList;
        }
        List<Map<String, Object>> resolved = new ArrayList<>();
        if (legacyCounterparty != null && !legacyCounterparty.isBlank()) {
            Map<String, Object> cp = new HashMap<>();
            cp.put("type", "partyA");
            cp.put("name", legacyCounterparty);
            cp.put("contact", "");
            cp.put("phone", "");
            cp.put("email", "");
            cp.put("address", "");
            resolved.add(cp);
        }
        return resolved;
    }

    private void saveCounterpartiesToTable(Long contractId, List<Map<String, Object>> counterparties) {
        QueryWrapper<ContractCounterparty> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId);
        counterpartyMapper.delete(wrapper);

        if (counterparties == null || counterparties.isEmpty()) {
            return;
        }

        for (int i = 0; i < counterparties.size(); i++) {
            Map<String, Object> cpMap = counterparties.get(i);
            ContractCounterparty cp = new ContractCounterparty();
            cp.setContractId(contractId);
            cp.setSortOrder(i);
            cp.setType((String) cpMap.getOrDefault("type", "partyA"));
            cp.setName((String) cpMap.getOrDefault("name", ""));
            cp.setContactPerson((String) cpMap.getOrDefault("contact", cpMap.getOrDefault("contactPerson", "")));
            cp.setContactPhone((String) cpMap.getOrDefault("phone", cpMap.getOrDefault("contactPhone", "")));
            cp.setContactEmail((String) cpMap.getOrDefault("email", cpMap.getOrDefault("contactEmail", "")));
            cp.setAddress((String) cpMap.getOrDefault("address", ""));
            counterpartyMapper.insert(cp);
        }
    }

    private List<Map<String, Object>> loadCounterpartiesAsMaps(Contract contract) {
        List<ContractCounterparty> cpRows = counterpartyMapper.selectByContractId(contract.getId());
        if (cpRows != null && !cpRows.isEmpty()) {
            List<Map<String, Object>> counterparties = new ArrayList<>();
            for (ContractCounterparty row : cpRows) {
                Map<String, Object> cp = new HashMap<>();
                cp.put("type", row.getType());
                cp.put("name", row.getName());
                cp.put("contact", row.getContactPerson());
                cp.put("phone", row.getContactPhone());
                cp.put("email", row.getContactEmail());
                cp.put("address", row.getAddress());
                counterparties.add(cp);
            }
            return counterparties;
        }

        List<Map<String, Object>> counterparties = new ArrayList<>();
        if (contract.getCounterparties() != null && !contract.getCounterparties().isEmpty()) {
            try {
                return objectMapper.readValue(contract.getCounterparties(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception ignored) {
                // ignore and fallback to legacy field
            }
        }
        if (contract.getCounterparty() != null && !contract.getCounterparty().isEmpty()) {
            Map<String, Object> cp = new HashMap<>();
            cp.put("type", "partyA");
            cp.put("name", contract.getCounterparty());
            cp.put("contact", "");
            cp.put("phone", "");
            cp.put("email", "");
            counterparties.add(cp);
        }
        return counterparties;
    }
}
