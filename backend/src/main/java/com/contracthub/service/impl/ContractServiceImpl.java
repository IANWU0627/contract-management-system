package com.contracthub.service.impl;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractCounterparty;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.entity.ContractSnapshot;
import com.contracthub.entity.ContractSnapshotDiffAnalysis;
import com.contracthub.entity.ContractAttachment;
import com.contracthub.entity.ContractAiSummary;
import com.contracthub.entity.ContractPayload;
import com.contracthub.entity.User;
import com.contracthub.entity.ApprovalRecord;
import com.contracthub.entity.QuickCodeHeader;
import com.contracthub.entity.QuickCodeItem;
import com.contracthub.mapper.ContractCounterpartyMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.mapper.ContractTagMapper;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.contracthub.mapper.ContractSnapshotMapper;
import com.contracthub.mapper.ContractSnapshotDiffAnalysisMapper;
import com.contracthub.mapper.ContractAttachmentMapper;
import com.contracthub.mapper.ContractAiSummaryMapper;
import com.contracthub.mapper.ContractPayloadMapper;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.ApprovalRecordMapper;
import com.contracthub.mapper.QuickCodeHeaderMapper;
import com.contracthub.mapper.QuickCodeItemMapper;
import com.contracthub.service.ContractService;
import com.contracthub.service.ContractNumberService;
import com.contracthub.service.NotificationService;
import com.contracthub.service.ContractVersionService;
import com.contracthub.service.ContractChangeLogService;
import com.contracthub.service.ContractDataScopeService;
import com.contracthub.service.ContractAiAssistantService;
import com.contracthub.exception.BusinessException;
import com.contracthub.enums.UserRole;
import com.contracthub.util.ClauseCompareUtils;
import com.contracthub.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
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
    private final ContractSnapshotMapper contractSnapshotMapper;
    private final ContractSnapshotDiffAnalysisMapper contractSnapshotDiffAnalysisMapper;
    private final ContractAttachmentMapper contractAttachmentMapper;
    private final ContractAiSummaryMapper contractAiSummaryMapper;
    private final ContractPayloadMapper contractPayloadMapper;
    private final UserMapper userMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final QuickCodeHeaderMapper quickCodeHeaderMapper;
    private final QuickCodeItemMapper quickCodeItemMapper;
    private final ContractNumberService contractNumberService;
    private final NotificationService notificationService;
    private final ContractVersionService contractVersionService;
    private final ContractChangeLogService contractChangeLogService;
    private final ContractDataScopeService contractDataScopeService;
    private final ContractAiAssistantService contractAiAssistantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractServiceImpl(ContractMapper contractMapper,
                                ContractCounterpartyMapper counterpartyMapper,
                                ContractFieldValueMapper fieldValueMapper,
                                ContractTagMapper tagMapper,
                                ContractTypeFieldMapper typeFieldMapper,
                                ContractSnapshotMapper contractSnapshotMapper,
                                ContractSnapshotDiffAnalysisMapper contractSnapshotDiffAnalysisMapper,
                                ContractAttachmentMapper contractAttachmentMapper,
                                ContractAiSummaryMapper contractAiSummaryMapper,
                                ContractPayloadMapper contractPayloadMapper,
                                UserMapper userMapper,
                                ApprovalRecordMapper approvalRecordMapper,
                                QuickCodeHeaderMapper quickCodeHeaderMapper,
                                QuickCodeItemMapper quickCodeItemMapper,
                                ContractNumberService contractNumberService,
                                NotificationService notificationService,
                                ContractVersionService contractVersionService,
                                ContractChangeLogService contractChangeLogService,
                                ContractDataScopeService contractDataScopeService,
                                ContractAiAssistantService contractAiAssistantService) {
        this.contractMapper = contractMapper;
        this.counterpartyMapper = counterpartyMapper;
        this.fieldValueMapper = fieldValueMapper;
        this.tagMapper = tagMapper;
        this.typeFieldMapper = typeFieldMapper;
        this.contractSnapshotMapper = contractSnapshotMapper;
        this.contractSnapshotDiffAnalysisMapper = contractSnapshotDiffAnalysisMapper;
        this.contractAttachmentMapper = contractAttachmentMapper;
        this.contractAiSummaryMapper = contractAiSummaryMapper;
        this.contractPayloadMapper = contractPayloadMapper;
        this.userMapper = userMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.quickCodeHeaderMapper = quickCodeHeaderMapper;
        this.quickCodeItemMapper = quickCodeItemMapper;
        this.contractNumberService = contractNumberService;
        this.notificationService = notificationService;
        this.contractVersionService = contractVersionService;
        this.contractChangeLogService = contractChangeLogService;
        this.contractDataScopeService = contractDataScopeService;
        this.contractAiAssistantService = contractAiAssistantService;
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
            if (status.contains(",")) {
                List<String> statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
                if (!statuses.isEmpty()) {
                    queryWrapper.in("status", statuses);
                }
            } else {
                queryWrapper.eq("status", status);
            }
        }
        if (counterparty != null && !counterparty.isEmpty()) {
            queryWrapper.apply(
                "id IN (SELECT contract_id FROM contract_counterparty WHERE name LIKE {0})",
                "%" + counterparty + "%"
            );
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
            String kw = keyword.trim();
            if (!kw.isEmpty()) {
                final String payloadKw = kw.length() >= 2 ? kw : null;
                queryWrapper.and(wrapper -> {
                    wrapper.like("title", kw).or().like("contract_no", kw);
                    if (payloadKw != null) {
                        wrapper.or().apply(
                            "id IN (SELECT contract_id FROM contract_payload WHERE content LIKE {0})",
                            "%" + payloadKw + "%"
                        );
                    }
                    wrapper.or()
                        .like("remark", kw)
                        .or()
                        .like("type", kw)
                        .or()
                        .like("status", kw)
                        .or()
                        .apply(
                            "id IN (SELECT contract_id FROM contract_counterparty WHERE name LIKE {0})",
                            "%" + kw + "%"
                        );
                });
            }
        }

        // 数据范围：管理员/法务全部；其他用户本人 + 同部门
        contractDataScopeService.applyContractVisibilityFilter(queryWrapper);

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
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            return null;
        }
        if (contractDataScopeService.canCurrentUserViewContract(contract)) {
            attachPayload(contract);
            return contract;
        }
        return null;
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
            }
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
        String payloadContent = (String) contractMap.get("content");
        
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
        applyContractRelation(contract, contractMap, false);
        
        contract.setCreatorId(SecurityUtils.getCurrentUserId());
        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateTime(LocalDateTime.now());
        
        insertContractWithRetry(contract);

        // 同步保存相对方到独立表（contract_counterparty）
        saveCounterpartiesToTable(contract.getId(), resolveCounterparties(counterpartiesList));
        
        Map<String, Object> dynamicFields = resolveDynamicFields(contractMap);
        validateDynamicFieldQuickCodeValues(contract.getType(), dynamicFields);
        String payloadDynamicFieldValues = null;
        if (!dynamicFields.isEmpty()) {
            try {
                payloadDynamicFieldValues = objectMapper.writeValueAsString(dynamicFields);
                contract.setDynamicFieldValues(payloadDynamicFieldValues);
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

        saveOrUpdatePayload(
            contract.getId(),
            payloadContent,
            contract.getTemplateVariables(),
            payloadDynamicFieldValues,
            contract.getAttachments()
        );
        attachPayload(contract);
        
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
        attachPayload(contract);
        assertCanModifyContract(contract);
        assertNotInApprovalFlow(contract);
        
        // 保存原始合同内容用于对比和版本记录
        Contract oldContract = new Contract();
        oldContract.setTitle(contract.getTitle());
        oldContract.setType(contract.getType());
        oldContract.setRemark(contract.getRemark());
        String oldCounterpartySummary = buildCounterpartySummary(loadCounterpartiesAsMaps(contract));
        oldContract.setCounterparties(contract.getCounterparties());
        oldContract.setAmount(contract.getAmount());
        oldContract.setCurrency(contract.getCurrency());
        oldContract.setStartDate(contract.getStartDate());
        oldContract.setEndDate(contract.getEndDate());
        oldContract.setStatus(contract.getStatus());
        oldContract.setContent(contract.getContent());
        oldContract.setCurrency(contract.getCurrency());
        oldContract.setParentContractId(contract.getParentContractId());
        oldContract.setRelationType(contract.getRelationType());
        
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
            }
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
        applyContractRelation(contract, contractMap, true);
        
        contract.setUpdateTime(LocalDateTime.now());
        
        // 相对方字段有变化时，同步更新独立表（contract_counterparty）
        if (contractMap.containsKey("counterparties") || contractMap.containsKey("counterparty")) {
            saveCounterpartiesToTable(id, resolveCounterparties(counterpartiesList));
        }
        
        Map<String, Object> dynamicFields = resolveDynamicFields(contractMap);
        validateDynamicFieldQuickCodeValues(contract.getType(), dynamicFields);
        String payloadDynamicFieldValues = contract.getDynamicFieldValues();
        if (!dynamicFields.isEmpty()) {
            try {
                payloadDynamicFieldValues = objectMapper.writeValueAsString(dynamicFields);
                contract.setDynamicFieldValues(payloadDynamicFieldValues);
            } catch (Exception ignored) {
                // Keep update flow resilient even if JSON serialization fails.
            }
        } else if (contractMap.containsKey("dynamicFields") || contractMap.containsKey("dynamicFieldValues")) {
            payloadDynamicFieldValues = "{}";
            contract.setDynamicFieldValues(payloadDynamicFieldValues);
        }
        contractMapper.updateById(contract);
        saveOrUpdatePayload(
            contract.getId(),
            contract.getContent(),
            contract.getTemplateVariables(),
            payloadDynamicFieldValues,
            contract.getAttachments()
        );

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
            String updateSummary = buildUpdateSummary(oldContract, contract);
            contractVersionService.createVersion(
                contract.getId(),
                contentForVersion,
                updateSummary,
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
            
            String newCounterpartySummary = buildCounterpartySummary(loadCounterpartiesAsMaps(contract));
            if (!Objects.equals(oldCounterpartySummary, newCounterpartySummary)) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "counterparty",
                    oldCounterpartySummary, newCounterpartySummary,
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

            if (!Objects.equals(oldContract.getCurrency(), contract.getCurrency())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "currency",
                    oldContract.getCurrency(), contract.getCurrency(),
                    operatorId, operatorName, "币种变更"
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

            if (!Objects.equals(oldContract.getParentContractId(), contract.getParentContractId())) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "parentContractId",
                    oldContract.getParentContractId() != null ? oldContract.getParentContractId().toString() : null,
                    contract.getParentContractId() != null ? contract.getParentContractId().toString() : null,
                    operatorId, operatorName, "主合同关联变更"
                );
            }

            if (!Objects.equals(normalizeRelationType(oldContract.getRelationType()),
                    normalizeRelationType(contract.getRelationType()))) {
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "relationType",
                    normalizeRelationType(oldContract.getRelationType()),
                    normalizeRelationType(contract.getRelationType()),
                    operatorId, operatorName, "合同关系类型变更"
                );
            }
            
            if (!Objects.equals(oldContract.getContent(), contract.getContent())) {
                String oc = oldContract.getContent();
                String nc = contract.getContent();
                int oldLen = oc != null ? oc.length() : 0;
                int newLen = nc != null ? nc.length() : 0;
                Map<String, Object> contentExtras = new HashMap<>();
                contentExtras.put("longText", true);
                contentExtras.put("oldLength", oldLen);
                contentExtras.put("newLength", newLen);
                contractChangeLogService.logChange(
                    contract.getId(), "UPDATE", "content",
                    previewForAuditLog(oc, 200),
                    previewForAuditLog(nc, 200),
                    operatorId, operatorName, "合同内容变更",
                    contentExtras
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
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);
        int result = contractMapper.deleteById(id);
        if (result == 0) {
            throw new RuntimeException("合同不存在");
        }
        // 删除动态字段值
        fieldValueMapper.deleteByContractId(id);
        contractPayloadMapper.delete(new QueryWrapper<ContractPayload>().eq("contract_id", id));
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
                    if (!canModifyContract(contract)) {
                        failedItems.add(Map.of("id", id, "reason", "无权限修改该合同"));
                        continue;
                    }
                    validateBatchStatusTransition(contract.getStatus(), status);
                    updateStatusWithAudit(contract, status, "批量状态更新", "批量状态变更");
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
                    if (!canModifyContract(contract)) {
                        failedItems.add(Map.of("id", id, "reason", "无权限修改该合同"));
                        continue;
                    }
                    if (type != null && !type.isEmpty()) {
                        contract.setType(type);
                    }
                    if (counterparty != null) {
                        // 已切换到独立表，批量编辑接口暂不处理相对方（沿用原有独立表数据）。
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
        attachPayload(contract);
        assertCanModifyContract(contract);
        ensureStatusAllowed(contract.getStatus(), Set.of("DRAFT"), "仅草稿合同可以提交审批");
        validateBeforeSubmit(contract);

        saveApprovalSnapshot(contract);
        contract.setSubmittedAt(LocalDateTime.now());
        contract.setCurrentApproverName("法务审批中");
        
        contract.setStatus("PENDING");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        saveApprovalRecord(id, "SUBMIT", "提交审批并冻结快照");
        
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
        assertCanModifyContract(contract);
        
        // 保存审批记录
        saveApprovalRecord(id, "APPROVE", comment);
        
        contract.setStatus("APPROVED");
        contract.setCurrentApproverName("");
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
        assertCanModifyContract(contract);
        
        // 保存审批记录
        saveApprovalRecord(id, "REJECT", comment);
        
        contract.setStatus("DRAFT");
        contract.setCurrentApproverName("");
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
    public Contract withdrawApproval(Long id, String reason) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);
        ensureStatusAllowed(contract.getStatus(), Set.of("PENDING", "APPROVING"), "当前状态不允许撤回审批");

        String oldStatus = contract.getStatus();
        saveApprovalRecord(id, "WITHDRAW", reason);
        contract.setStatus("DRAFT");
        contract.setCurrentApproverName("");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        String operatorName = SecurityUtils.getCurrentUserName();
        if (operatorName == null || operatorName.isEmpty()) {
            operatorName = "admin";
        }
        contractChangeLogService.logChange(
            contract.getId(), "UPDATE", "status",
            oldStatus, "DRAFT",
            SecurityUtils.getCurrentUserId(), operatorName,
            (reason == null || reason.isBlank()) ? "撤回审批" : "撤回审批，原因：" + reason
        );
        return contract;
    }

    @Override
    @Transactional
    public Contract signContract(Long id) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);
        
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
        assertCanModifyContract(contract);
        
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
    public Contract terminateContract(Long id, String reason) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);

        ensureStatusAllowed(contract.getStatus(), Set.of("SIGNED", "APPROVED", "ARCHIVED", "RENEWING"), "当前状态不允许终止");
        updateStatusWithAudit(contract, "TERMINATED", reason, "合同终止");
        notifyContractOwner(contract, "已终止");
        return contract;
    }

    @Override
    @Transactional
    public Contract startRenewal(Long id, String reason) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);

        ensureStatusAllowed(contract.getStatus(), Set.of("SIGNED", "APPROVED", "ARCHIVED"), "当前状态不允许发起续签");
        updateStatusWithAudit(contract, "RENEWING", reason, "发起续签");
        notifyContractOwner(contract, "进入续签流程");
        return contract;
    }

    @Override
    @Transactional
    public Contract completeRenewal(Long id, String reason) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);

        ensureStatusAllowed(contract.getStatus(), Set.of("RENEWING"), "仅续签中合同可以标记为续签完成");
        updateStatusWithAudit(contract, "RENEWED", reason, "续签完成");
        notifyContractOwner(contract, "续签完成");
        return contract;
    }

    @Override
    @Transactional
    public Contract markNotRenewed(Long id, String reason) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        assertCanModifyContract(contract);

        ensureStatusAllowed(contract.getStatus(), Set.of("RENEWING"), "仅续签中合同可以标记为不续签");
        updateStatusWithAudit(contract, "NOT_RENEWED", reason, "不续签");
        notifyContractOwner(contract, "标记为不续签");
        return contract;
    }

    @Override
    @Transactional
    public Contract copyContract(Long id) {
        Contract originalContract = contractMapper.selectById(id);
        if (originalContract == null) {
            throw new RuntimeException("合同不存在");
        }
        attachPayload(originalContract);
        if (!contractDataScopeService.canCurrentUserViewContract(originalContract)) {
            throw new RuntimeException("无权限查看该合同");
        }
        
        // 创建新合同
        Contract newContract = new Contract();
        assignNextContractNo(newContract);
        newContract.setTitle(originalContract.getTitle() + " (副本)");
        newContract.setType(originalContract.getType());
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
        newContract.setParentContractId(originalContract.getParentContractId());
        newContract.setRelationType(originalContract.getRelationType());
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

    private void ensureStatusAllowed(String currentStatus, Set<String> allowedStatuses, String errorMessage) {
        if (currentStatus == null || !allowedStatuses.contains(currentStatus)) {
            throw new RuntimeException(errorMessage);
        }
    }

    private void validateBatchStatusTransition(String currentStatus, String targetStatus) {
        if (targetStatus == null || targetStatus.isBlank()) {
            throw new RuntimeException("目标状态不能为空");
        }
        if (Objects.equals(currentStatus, targetStatus)) {
            return;
        }
        Map<String, Set<String>> transitions = new HashMap<>();
        transitions.put("DRAFT", Set.of("PENDING"));
        transitions.put("PENDING", Set.of("APPROVED", "DRAFT"));
        transitions.put("APPROVING", Set.of("APPROVED", "DRAFT"));
        transitions.put("APPROVED", Set.of("SIGNED", "ARCHIVED", "TERMINATED", "RENEWING"));
        transitions.put("SIGNED", Set.of("ARCHIVED", "TERMINATED", "RENEWING"));
        transitions.put("ARCHIVED", Set.of("TERMINATED", "RENEWING"));
        transitions.put("RENEWING", Set.of("RENEWED", "NOT_RENEWED", "TERMINATED"));
        Set<String> allowedTargets = transitions.getOrDefault(currentStatus, Set.of());
        if (!allowedTargets.contains(targetStatus)) {
            throw new RuntimeException("状态不允许从 " + currentStatus + " 变更为 " + targetStatus);
        }
    }

    private void notifyContractOwner(Contract contract, String actionText) {
        if (contract.getCreatorId() != null) {
            notificationService.sendContractUpdate(contract.getCreatorId(), contract.getId(),
                contract.getContractNo(), actionText);
        }
    }

    private void updateStatusWithAudit(Contract contract, String nextStatus, String reason, String actionName) {
        String oldStatus = contract.getStatus();
        contract.setStatus(nextStatus);
        contract.setCurrentApproverName("");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        String operatorName = SecurityUtils.getCurrentUserName();
        if (operatorName == null || operatorName.isEmpty()) {
            operatorName = "admin";
        }
        String finalReason = (reason == null || reason.isBlank()) ? "" : reason.trim();
        String remark = finalReason.isEmpty() ? actionName : (actionName + "，原因：" + finalReason);
        contractChangeLogService.logChange(
            contract.getId(),
            "UPDATE",
            "status",
            oldStatus,
            nextStatus,
            SecurityUtils.getCurrentUserId(),
            operatorName,
            remark
        );
    }

    @Override
    public Map<String, Object> buildContractResponse(Contract contract) {
        attachPayload(contract);
        Map<String, Object> result = new HashMap<>();
        result.put("id", contract.getId());
        result.put("contractNo", contract.getContractNo());
        result.put("title", contract.getTitle());
        result.put("type", contract.getType());
        List<Map<String, Object>> counterparties = loadCounterpartiesAsMaps(contract);
        result.put("counterparty", buildCounterpartySummary(counterparties));
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
        result.put("parentContractId", contract.getParentContractId());
        result.put("relationType", contract.getRelationType() == null ? "MAIN" : contract.getRelationType());
        result.put("templateId", contract.getTemplateId());
        result.put("contentMode", contract.getContentMode());
        result.put("templateVariables", contract.getTemplateVariables());
        result.put("approvalSnapshotMeta", loadLatestSnapshotMeta(contract.getId()));
        result.put("submittedAt", contract.getSubmittedAt());
        result.put("currentApproverName", contract.getCurrentApproverName());
        
        // 添加相对方列表：优先读取独立表，兼容历史 JSON/单字段
        result.put("counterparties", counterparties);
        
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
    public Map<String, Object> buildContractListResponse(Contract contract) {
        Map<String, Object> result = buildContractResponse(contract);
        result.remove("content");
        result.remove("templateVariables");
        result.remove("attachments");
        result.remove("dynamicFields");
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
        result.put("relatedContracts", buildRelatedContracts(contract));
        result.put("approvalFlow", buildApprovalFlow(contract));
        
        return result;
    }

    @Override
    public Map<String, Object> buildContractPayloadResponse(Long id) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractId", contract.getId());
        payload.put("content", contract.getContent());
        payload.put("templateVariables", contract.getTemplateVariables());
        payload.put("attachments", processAttachments(contract));
        payload.put("dynamicFieldValues", contract.getDynamicFieldValues());

        Map<String, Object> dynamicFields = new HashMap<>();
        List<ContractFieldValue> fieldValues = fieldValueMapper.selectByContractId(contract.getId());
        for (ContractFieldValue fv : fieldValues) {
            dynamicFields.put(fv.getFieldKey(), fv.getFieldValue());
        }
        payload.put("dynamicFields", dynamicFields);
        return payload;
    }

    @Override
    public List<Map<String, Object>> listContractSnapshots(Long id) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        List<ContractSnapshot> snapshots = contractSnapshotMapper.selectList(
            new QueryWrapper<ContractSnapshot>()
                .eq("contract_id", id)
                .orderByDesc("created_at")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContractSnapshot snapshot : snapshots) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", snapshot.getId());
            item.put("contractId", snapshot.getContractId());
            item.put("snapshotType", snapshot.getSnapshotType());
            item.put("contentHash", snapshot.getContentHash());
            item.put("createdBy", snapshot.getCreatedBy());
            item.put("createdAt", snapshot.getCreatedAt());
            item.put("meta", parseJsonMap(snapshot.getSnapshotMeta()));
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> getContractSnapshot(Long id, Long snapshotId) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ContractSnapshot snapshot = contractSnapshotMapper.selectOne(
            new QueryWrapper<ContractSnapshot>()
                .eq("id", snapshotId)
                .eq("contract_id", id)
                .last("LIMIT 1")
        );
        if (snapshot == null) {
            throw new RuntimeException("快照不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", snapshot.getId());
        result.put("contractId", snapshot.getContractId());
        result.put("snapshotType", snapshot.getSnapshotType());
        result.put("content", snapshot.getContent());
        result.put("templateVariables", snapshot.getTemplateVariables());
        result.put("contentHash", snapshot.getContentHash());
        result.put("createdBy", snapshot.getCreatedBy());
        result.put("createdAt", snapshot.getCreatedAt());
        result.put("meta", parseJsonMap(snapshot.getSnapshotMeta()));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> compareContractSnapshots(Long id, Long baseSnapshotId, Long targetSnapshotId) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ContractSnapshot base = contractSnapshotMapper.selectOne(
            new QueryWrapper<ContractSnapshot>()
                .eq("id", baseSnapshotId)
                .eq("contract_id", id)
                .last("LIMIT 1")
        );
        ContractSnapshot target = contractSnapshotMapper.selectOne(
            new QueryWrapper<ContractSnapshot>()
                .eq("id", targetSnapshotId)
                .eq("contract_id", id)
                .last("LIMIT 1")
        );
        if (base == null || target == null) {
            throw new RuntimeException("快照不存在");
        }

        ContractSnapshotDiffAnalysis cached = contractSnapshotDiffAnalysisMapper.selectOne(
            new QueryWrapper<ContractSnapshotDiffAnalysis>()
                .eq("contract_id", id)
                .eq("base_snapshot_id", baseSnapshotId)
                .eq("target_snapshot_id", targetSnapshotId)
                .orderByDesc("updated_at")
                .last("LIMIT 1")
        );
        if (cached != null && cached.getDiffJson() != null && !cached.getDiffJson().isBlank()) {
            return buildSnapshotCompareResponse(base, target, cached);
        }

        List<Map<String, Object>> differences = calculateTextDiff(
            normalizeCompareText(base.getContent()),
            normalizeCompareText(target.getContent())
        );
        List<Map<String, Object>> clauseChanges = analyzeCompareClauseChanges(base.getContent(), target.getContent());
        int addedLines = countDiffLines(differences, "added");
        int removedLines = countDiffLines(differences, "removed");
        Map<String, Object> riskPayload = analyzeCompareRisk(differences, clauseChanges);
        String overallRisk = String.valueOf(riskPayload.getOrDefault("overallRisk", "LOW"));

        ContractSnapshotDiffAnalysis analysis = cached != null ? cached : new ContractSnapshotDiffAnalysis();
        analysis.setContractId(id);
        analysis.setBaseSnapshotId(baseSnapshotId);
        analysis.setTargetSnapshotId(targetSnapshotId);
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
            contractSnapshotDiffAnalysisMapper.insert(analysis);
        } else {
            contractSnapshotDiffAnalysisMapper.updateById(analysis);
        }
        return buildSnapshotCompareResponse(base, target, analysis);
    }

    @Override
    @Transactional
    public Map<String, Object> generateApprovalSummary(Long id, boolean forceRefresh) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ContractSnapshot latestSnapshot = contractSnapshotMapper.selectOne(
            new QueryWrapper<ContractSnapshot>()
                .eq("contract_id", id)
                .orderByDesc("created_at")
                .last("LIMIT 1")
        );
        Long snapshotId = latestSnapshot != null ? latestSnapshot.getId() : null;
        ContractAiSummary existing = contractAiSummaryMapper.selectOne(
            new QueryWrapper<ContractAiSummary>()
                .eq("contract_id", id)
                .eq(snapshotId != null, "snapshot_id", snapshotId)
                .orderByDesc("updated_at")
                .last("LIMIT 1")
        );
        if (!forceRefresh && existing != null && "SUCCESS".equalsIgnoreCase(existing.getStatus())) {
            return buildApprovalSummaryResponse(existing);
        }

        Map<String, Object> analysis = contractAiAssistantService.buildOfflineDemoResult(contract);
        ContractAiSummary summary = existing != null ? existing : new ContractAiSummary();
        summary.setContractId(id);
        summary.setSnapshotId(snapshotId);
        summary.setSummaryText(String.valueOf(analysis.getOrDefault("summary", "")));
        summary.setConfidenceScore(toInt(analysis.get("score"), 75));
        summary.setModelName("offline-demo");
        summary.setSummaryVersion("v1");
        summary.setStatus("SUCCESS");
        summary.setCreatedBy(SecurityUtils.getCurrentUserId());
        summary.setUpdatedAt(LocalDateTime.now());
        if (summary.getCreatedAt() == null) {
            summary.setCreatedAt(LocalDateTime.now());
        }
        try {
            Object keyInfo = analysis.getOrDefault("keyInfo", new HashMap<String, Object>());
            summary.setKeyTermsJson(objectMapper.writeValueAsString(keyInfo));
            Object risks = analysis.getOrDefault("risks", new ArrayList<>());
            summary.setRisksJson(objectMapper.writeValueAsString(risks));
        } catch (Exception e) {
            summary.setKeyTermsJson("{}");
            summary.setRisksJson("[]");
        }
        if (summary.getId() == null) {
            contractAiSummaryMapper.insert(summary);
        } else {
            contractAiSummaryMapper.updateById(summary);
        }
        return buildApprovalSummaryResponse(summary);
    }

    @Override
    public Map<String, Object> getApprovalSummary(Long id) {
        Contract contract = getContractById(id);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }
        ContractAiSummary summary = contractAiSummaryMapper.selectOne(
            new QueryWrapper<ContractAiSummary>()
                .eq("contract_id", id)
                .orderByDesc("updated_at")
                .last("LIMIT 1")
        );
        if (summary == null) {
            return new HashMap<>();
        }
        return buildApprovalSummaryResponse(summary);
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

    private static String normalizeRelationType(String relationType) {
        if (relationType == null || relationType.isBlank()) {
            return "MAIN";
        }
        return relationType.trim().toUpperCase();
    }

    /** 审计日志中展示用，避免正文过长撑爆列表 */
    private static String previewForAuditLog(String text, int maxChars) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "…";
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

    private boolean isContractOwner(Contract contract) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return contract != null && currentUserId != null && Objects.equals(contract.getCreatorId(), currentUserId);
    }

    private boolean canModifyContract(Contract contract) {
        String role = SecurityUtils.getCurrentUserRole();
        if (UserRole.ADMIN.name().equals(role)) {
            return true;
        }
        // 法务与普通用户：只能修改自己创建的合同（同部门仅可见）
        return isContractOwner(contract);
    }

    private void assertCanModifyContract(Contract contract) {
        if (!canModifyContract(contract)) {
            throw new RuntimeException("无权限修改该合同");
        }
    }

    private void assertNotInApprovalFlow(Contract contract) {
        if (contract == null) {
            return;
        }
        if (Set.of("PENDING", "APPROVING").contains(contract.getStatus())) {
            throw new RuntimeException("审批中合同已冻结，请先撤回后再修改");
        }
    }

    private void validateBeforeSubmit(Contract contract) {
        if (contract.getContent() == null || contract.getContent().isBlank()) {
            throw new RuntimeException("合同正文为空，无法提交审批");
        }
        String mode = contract.getContentMode() == null ? "" : contract.getContentMode().trim().toLowerCase(Locale.ROOT);
        if ("upload".equals(mode) && !hasUploadedFinalFile(contract)) {
            throw new RuntimeException("上传模式需先生成或上传最终合同文件后再提交审批");
        }
    }

    private void saveApprovalSnapshot(Contract contract) {
        ContractSnapshot snapshot = new ContractSnapshot();
        snapshot.setContractId(contract.getId());
        snapshot.setSnapshotType("SUBMIT_APPROVAL");
        snapshot.setContent(contract.getContent());
        snapshot.setTemplateVariables(contract.getTemplateVariables());
        snapshot.setSnapshotMeta(buildApprovalSnapshotMeta(contract));
        snapshot.setContentHash(sha256(contract.getContent()));
        snapshot.setCreatedBy(SecurityUtils.getCurrentUserId());
        snapshot.setCreatedAt(LocalDateTime.now());
        contractSnapshotMapper.insert(snapshot);
    }

    private boolean hasUploadedFinalFile(Contract contract) {
        List<ContractAttachment> attachmentRows = contractAttachmentMapper.selectByContractId(contract.getId());
        if (attachmentRows != null && !attachmentRows.isEmpty()) {
            boolean hasContractFile = attachmentRows.stream()
                .anyMatch(a -> "contract".equalsIgnoreCase(String.valueOf(a.getFileCategory())));
            if (hasContractFile) {
                return true;
            }
        }
        List<Map<String, Object>> attachments = processAttachments(contract);
        if (attachments == null || attachments.isEmpty()) {
            return false;
        }
        return attachments.stream().anyMatch(item ->
            "contract".equals(String.valueOf(item.getOrDefault("fileCategory", "")))
                || "application/pdf".equals(String.valueOf(item.getOrDefault("fileType", "")))
        );
    }

    private String buildApprovalSnapshotMeta(Contract contract) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("snapshotAt", LocalDateTime.now().toString());
        meta.put("contentMode", contract.getContentMode());
        meta.put("contentHash", sha256(contract.getContent()));
        meta.put("templateId", contract.getTemplateId());
        meta.put("templateVariablesHash", sha256(contract.getTemplateVariables()));

        List<Map<String, Object>> attachments = processAttachments(contract);
        Map<String, Object> finalFile = attachments.stream()
            .filter(item -> "contract".equals(String.valueOf(item.getOrDefault("fileCategory", ""))))
            .findFirst()
            .orElse(attachments.stream().findFirst().orElse(new HashMap<>()));
        meta.put("finalFile", finalFile);
        meta.put("strategy", "template".equalsIgnoreCase(String.valueOf(contract.getContentMode()))
            ? "TEMPLATE_RENDERED_SNAPSHOT"
            : "UPLOADED_OR_GENERATED_FILE_SNAPSHOT");
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void attachPayload(Contract contract) {
        if (contract == null || contract.getId() == null) {
            return;
        }
        ContractPayload payload = contractPayloadMapper.selectOne(
            new QueryWrapper<ContractPayload>().eq("contract_id", contract.getId()).last("LIMIT 1")
        );
        if (payload == null) {
            return;
        }
        contract.setContent(payload.getContent());
        contract.setTemplateVariables(payload.getTemplateVariables());
        contract.setDynamicFieldValues(payload.getDynamicFieldValues());
        contract.setAttachments(payload.getAttachments());
    }

    private void saveOrUpdatePayload(Long contractId,
                                     String content,
                                     String templateVariables,
                                     String dynamicFieldValues,
                                     String attachments) {
        if (contractId == null) {
            return;
        }
        ContractPayload payload = contractPayloadMapper.selectOne(
            new QueryWrapper<ContractPayload>().eq("contract_id", contractId).last("LIMIT 1")
        );
        if (payload == null) {
            payload = new ContractPayload();
            payload.setContractId(contractId);
            payload.setCreatedAt(LocalDateTime.now());
        }
        payload.setContent(content);
        payload.setTemplateVariables(templateVariables);
        payload.setDynamicFieldValues(dynamicFieldValues);
        payload.setAttachments(attachments);
        payload.setContentHash(sha256(content));
        payload.setUpdatedAt(LocalDateTime.now());
        if (payload.getId() == null) {
            contractPayloadMapper.insert(payload);
            return;
        }
        contractPayloadMapper.updateById(payload);
    }

    private Map<String, Object> loadLatestSnapshotMeta(Long contractId) {
        if (contractId == null) {
            return new HashMap<>();
        }
        ContractSnapshot latest = contractSnapshotMapper.selectOne(
            new QueryWrapper<ContractSnapshot>()
                .eq("contract_id", contractId)
                .orderByDesc("created_at")
                .last("LIMIT 1")
        );
        if (latest == null) {
            return new HashMap<>();
        }
        Map<String, Object> meta = parseJsonMap(latest.getSnapshotMeta());
        meta.put("snapshotType", latest.getSnapshotType());
        meta.put("snapshotAt", latest.getCreatedAt());
        meta.put("contentHash", latest.getContentHash());
        return meta;
    }

    private String sha256(String content) {
        if (content == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> parseJsonListMap(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Map<String, Object> buildSnapshotCompareResponse(ContractSnapshot base,
                                                             ContractSnapshot target,
                                                             ContractSnapshotDiffAnalysis analysis) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseSnapshotId", base.getId());
        result.put("targetSnapshotId", target.getId());
        result.put("version1", base.getSnapshotType() + "@" + base.getId());
        result.put("version2", target.getSnapshotType() + "@" + target.getId());
        result.put("overallRisk", analysis.getOverallRisk());
        result.put("modelName", analysis.getModelName());
        result.put("promptVersion", analysis.getPromptVersion());
        result.put("analyzedAt", analysis.getUpdatedAt());

        try {
            Map<String, Object> diffJson = objectMapper.readValue(
                analysis.getDiffJson() == null ? "{}" : analysis.getDiffJson(),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
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
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
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

    private String normalizeCompareText(String content) {
        if (content == null) {
            return "";
        }
        return content
            .replaceAll("<[^>]+>", " ")
            .replace("&nbsp;", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private List<Map<String, Object>> calculateTextDiff(String content1, String content2) {
        List<Map<String, Object>> diff = new ArrayList<>();
        String[] lines1 = content1.split("\n");
        String[] lines2 = content2.split("\n");
        int i = 0;
        int j = 0;
        while (i < lines1.length || j < lines2.length) {
            if (i < lines1.length && j < lines2.length && lines1[i].equals(lines2[j])) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "unchanged");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                item.put("lineNum2", j + 1);
                diff.add(item);
                i++;
                j++;
            } else if (j < lines2.length && (i >= lines1.length || !containsFrom(lines1, lines2[j], i))) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "added");
                item.put("content", lines2[j]);
                item.put("lineNum2", j + 1);
                diff.add(item);
                j++;
            } else if (i < lines1.length) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "removed");
                item.put("content", lines1[i]);
                item.put("lineNum1", i + 1);
                diff.add(item);
                i++;
            }
        }
        return diff;
    }

    private boolean containsFrom(String[] values, String target, int start) {
        for (int i = start; i < values.length; i++) {
            if (values[i].equals(target)) {
                return true;
            }
        }
        return false;
    }

    private int countDiffLines(List<Map<String, Object>> differences, String type) {
        return (int) differences.stream()
            .filter(item -> type.equals(String.valueOf(item.getOrDefault("type", ""))))
            .count();
    }

    private Map<String, Object> analyzeCompareRisk(List<Map<String, Object>> differences, List<Map<String, Object>> clauseChanges) {
        List<Map<String, Object>> riskItems = new ArrayList<>();
        int high = 0;
        int medium = 0;
        int low = 0;

        for (Map<String, Object> item : differences) {
            String type = String.valueOf(item.getOrDefault("type", ""));
            if (!"added".equals(type) && !"removed".equals(type)) {
                continue;
            }
            String content = normalizeCompareText(String.valueOf(item.getOrDefault("content", "")));
            if (content.isBlank()) {
                continue;
            }
            String level = detectCompareRiskLevel(content);
            if ("LOW".equals(level)) {
                continue;
            }
            Map<String, Object> risk = new LinkedHashMap<>();
            risk.put("level", level.toLowerCase(Locale.ROOT));
            risk.put("changeType", type);
            risk.put("title", detectCompareRiskTitle(content));
            risk.put("titleKey", detectCompareRiskTitleKey(content));
            risk.put("reason", detectCompareRiskReason(content));
            risk.put("reasonKey", detectCompareRiskReasonKey(content));
            risk.put("evidence", content.length() > 120 ? content.substring(0, 120) + "..." : content);
            risk.put("suggestion", detectCompareRiskSuggestion(content));
            risk.put("suggestionKey", detectCompareRiskSuggestionKey(content));
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
            risk.put("reason", String.valueOf(clause.getOrDefault("riskReason", "关键条款变化")));
            risk.put("reasonKey", String.valueOf(clause.getOrDefault("riskReasonKey", "contract.versionsDetail.reason.generic")));
            risk.put("evidence", String.valueOf(clause.getOrDefault("after", clause.getOrDefault("before", ""))));
            risk.put("suggestion", detectCompareRiskSuggestion(String.valueOf(clause.getOrDefault("after", ""))));
            risk.put("suggestionKey", detectCompareRiskSuggestionKey(String.valueOf(clause.getOrDefault("after", ""))));
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
            risk.put("reason", "本次快照变更未命中高风险关键词，建议按审批清单继续人工复核。");
            risk.put("reasonKey", "contract.versionsDetail.reason.none");
            risk.put("evidence", "");
            risk.put("suggestion", "重点复核付款、违约责任、争议解决条款。");
            risk.put("suggestionKey", "contract.versionsDetail.suggestionText.none");
            riskItems.add(risk);
            low = 1;
        }

        String overallRisk = high > 0 ? "HIGH" : (medium > 0 ? "MEDIUM" : "LOW");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallRisk", overallRisk.toLowerCase(Locale.ROOT));
        summary.put("highCount", high);
        summary.put("mediumCount", medium);
        summary.put("lowCount", low);
        summary.put("totalRiskItems", riskItems.size());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("overallRisk", overallRisk);
        payload.put("riskSummary", summary);
        payload.put("riskItems", riskItems);
        payload.put("aiCommentary", buildCompareCommentary(overallRisk));
        payload.put("aiCommentaryKey", buildCompareCommentaryKey(overallRisk));
        return payload;
    }

    private List<Map<String, Object>> analyzeCompareClauseChanges(String content1, String content2) {
        Map<String, String> clauses1 = ClauseCompareUtils.splitToClauseMap(content1, this::normalizeCompareText);
        Map<String, String> clauses2 = ClauseCompareUtils.splitToClauseMap(content2, this::normalizeCompareText);
        List<Map<String, Object>> changes = new ArrayList<>();
        Set<String> matchedRightTitles = new LinkedHashSet<>();

        for (Map.Entry<String, String> left : clauses1.entrySet()) {
            String leftTitle = left.getKey();
            String leftContent = left.getValue();
            String matchedTitle = ClauseCompareUtils.findMatchingTitle(leftTitle, clauses2, matchedRightTitles);
            if (matchedTitle == null) {
                matchedTitle = ClauseCompareUtils.findBestContentMatch(
                    leftContent, clauses2, matchedRightTitles, this::normalizeCompareText);
            }
            if (matchedTitle == null) {
                changes.add(buildCompareClauseChange(leftTitle, "REMOVED", leftContent, ""));
                continue;
            }
            matchedRightTitles.add(matchedTitle);
            String rightContent = clauses2.get(matchedTitle);
            if (!Objects.equals(normalizeCompareText(leftContent), normalizeCompareText(rightContent))) {
                if (ClauseCompareUtils.isMinorChange(
                    leftContent, rightContent, this::normalizeCompareText, this::normalizeCompareForSemantic)) {
                    changes.add(buildCompareClauseChange(
                        ClauseCompareUtils.displayRenamedTitle(leftTitle, matchedTitle), "MINOR", leftContent, rightContent));
                    continue;
                }
                String displayTitle = ClauseCompareUtils.displayRenamedTitle(leftTitle, matchedTitle);
                changes.add(buildCompareClauseChange(displayTitle, "MODIFIED", leftContent, rightContent));
            }
        }

        for (Map.Entry<String, String> right : clauses2.entrySet()) {
            if (!matchedRightTitles.contains(right.getKey())) {
                changes.add(buildCompareClauseChange(right.getKey(), "ADDED", "", right.getValue()));
            }
        }

        return changes;
    }

    private Map<String, Object> buildCompareClauseChange(String title, String changeType, String before, String after) {
        String safeBefore = before == null ? "" : before;
        String safeAfter = after == null ? "" : after;
        String anchor = !safeAfter.isBlank() ? safeAfter : safeBefore;
        String riskLevel = detectCompareRiskLevel(anchor).toLowerCase(Locale.ROOT);
        if ("MINOR".equalsIgnoreCase(changeType)) {
            riskLevel = "low";
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("changeType", changeType);
        item.put("before", safeBefore.length() > 200 ? safeBefore.substring(0, 200) + "..." : safeBefore);
        item.put("after", safeAfter.length() > 200 ? safeAfter.substring(0, 200) + "..." : safeAfter);
        item.put("riskLevel", riskLevel);
        item.put("riskReason", "MINOR".equalsIgnoreCase(changeType)
                ? "仅检测到格式或标点级调整，未识别到实质条款变化。"
                : detectCompareRiskReason(anchor));
        item.put("riskReasonKey", "MINOR".equalsIgnoreCase(changeType)
                ? "contract.versionsDetail.reason.minor"
                : detectCompareRiskReasonKey(anchor));
        return item;
    }

    private String normalizeCompareForSemantic(String content) {
        String normalized = normalizeCompareText(content).toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[\\p{Punct}\\p{IsPunctuation}\\s]+", "");
    }

    private String detectCompareRiskLevel(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn", "争议", "仲裁", "jurisdiction", "termination", "解除"))) {
            return "HIGH";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice", "交付", "验收", "保密", "confidential"))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String detectCompareRiskTitle(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "违约责任条款变更";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "争议解决条款变更";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "付款结算条款变更";
        }
        return "关键条款发生变更";
    }

    private String detectCompareRiskTitleKey(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.riskTopic.liability";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.riskTopic.dispute";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.riskTopic.payment";
        }
        return "contract.versionsDetail.riskTopic.generic";
    }

    private String detectCompareRiskReason(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "责任边界变化可能带来更高违约成本。";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "争议处理路径变化会影响诉讼/仲裁成本与执行效率。";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "付款节点或结算条件变化可能影响现金流与回款。";
        }
        return "关键条款内容调整，建议人工复核业务影响。";
    }

    private String detectCompareRiskReasonKey(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.reason.liability";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.reason.dispute";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.reason.payment";
        }
        return "contract.versionsDetail.reason.generic";
    }

    private String detectCompareRiskSuggestion(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "建议法务确认责任上限、违约金比例与免责条件。";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "建议统一争议解决方式与管辖地。";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "建议复核付款条件、发票类型与逾期责任。";
        }
        return "建议在审批意见中补充该改动说明。";
    }

    private String detectCompareRiskSuggestionKey(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (containsAnyKeyword(lower, List.of("违约", "赔偿", "liability", "indemn"))) {
            return "contract.versionsDetail.suggestionText.liability";
        }
        if (containsAnyKeyword(lower, List.of("争议", "仲裁", "jurisdiction", "dispute"))) {
            return "contract.versionsDetail.suggestionText.dispute";
        }
        if (containsAnyKeyword(lower, List.of("付款", "发票", "payment", "invoice"))) {
            return "contract.versionsDetail.suggestionText.payment";
        }
        return "contract.versionsDetail.suggestionText.generic";
    }

    private String buildCompareCommentary(String overallRisk) {
        if ("HIGH".equals(overallRisk)) {
            return "本次快照差异包含高风险变更，请优先复核违约责任、争议解决与付款条件。";
        }
        if ("MEDIUM".equals(overallRisk)) {
            return "本次快照存在中风险条款变更，建议补充法务说明后再推进审批。";
        }
        return "本次快照未识别到明显高风险改动，建议按标准清单继续复核。";
    }

    private String buildCompareCommentaryKey(String overallRisk) {
        if ("HIGH".equals(overallRisk)) {
            return "contract.versionsDetail.commentary.high";
        }
        if ("MEDIUM".equals(overallRisk)) {
            return "contract.versionsDetail.commentary.medium";
        }
        return "contract.versionsDetail.commentary.low";
    }

    private boolean containsAnyKeyword(String source, List<String> keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildApprovalSummaryResponse(ContractAiSummary summary) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", summary.getId());
        result.put("contractId", summary.getContractId());
        result.put("snapshotId", summary.getSnapshotId());
        result.put("summary", summary.getSummaryText());
        result.put("keyTerms", parseJsonMap(summary.getKeyTermsJson()));
        result.put("risks", parseJsonListMap(summary.getRisksJson()));
        result.put("score", summary.getConfidenceScore());
        result.put("modelName", summary.getModelName());
        result.put("summaryVersion", summary.getSummaryVersion());
        result.put("status", summary.getStatus());
        result.put("updatedAt", summary.getUpdatedAt());
        return result;
    }

    private Map<String, Object> buildApprovalFlow(Contract contract) {
        Map<String, Object> flow = new LinkedHashMap<>();
        flow.put("currentStatus", contract.getStatus());
        flow.put("submittedAt", contract.getSubmittedAt());
        flow.put("blockedBy", contract.getCurrentApproverName() == null || contract.getCurrentApproverName().isBlank()
            ? "-" : contract.getCurrentApproverName());
        flow.put("frozen", Set.of("PENDING", "APPROVING").contains(contract.getStatus()));

        List<ApprovalRecord> records = approvalRecordMapper.selectList(
            new QueryWrapper<ApprovalRecord>()
                .eq("contract_id", contract.getId())
                .orderByAsc("create_time")
        );
        List<Map<String, Object>> steps = new ArrayList<>();
        for (ApprovalRecord r : records) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("action", r.getStatus());
            step.put("operator", r.getApproverName());
            step.put("comment", r.getComment());
            step.put("time", r.getCreateTime());
            steps.add(step);
        }
        flow.put("steps", steps);
        if (!records.isEmpty()) {
            ApprovalRecord latest = records.get(records.size() - 1);
            flow.put("lastAction", latest.getStatus());
            flow.put("lastActionAt", latest.getCreateTime());
        } else {
            flow.put("lastAction", "-");
            flow.put("lastActionAt", null);
        }
        return flow;
    }

    private void applyContractRelation(Contract contract, Map<String, Object> contractMap, boolean isUpdate) {
        String relationType = contract.getRelationType();
        if (contractMap.containsKey("relationType")) {
            Object relationTypeObj = contractMap.get("relationType");
            relationType = relationTypeObj != null ? relationTypeObj.toString().trim().toUpperCase() : "MAIN";
            contract.setRelationType(relationType);
        } else if (relationType == null || relationType.isBlank()) {
            contract.setRelationType("MAIN");
        }

        if (contractMap.containsKey("parentContractId")) {
            Object parentObj = contractMap.get("parentContractId");
            if (parentObj == null || parentObj.toString().isBlank()) {
                contract.setParentContractId(null);
            } else if (parentObj instanceof Number) {
                contract.setParentContractId(((Number) parentObj).longValue());
            } else {
                contract.setParentContractId(Long.parseLong(parentObj.toString()));
            }
        }

        if (!"SUPPLEMENT".equals(contract.getRelationType())) {
            contract.setRelationType("MAIN");
            contract.setParentContractId(null);
            return;
        }

        if (contract.getParentContractId() == null) {
            throw new RuntimeException("补充协议必须关联主合同");
        }
        if (isUpdate && contract.getId() != null && Objects.equals(contract.getParentContractId(), contract.getId())) {
            throw new RuntimeException("补充协议不能关联自己");
        }
        Contract parent = contractMapper.selectById(contract.getParentContractId());
        if (parent == null) {
            throw new RuntimeException("关联主合同不存在");
        }
    }

    private String buildUpdateSummary(Contract oldContract, Contract newContract) {
        List<String> changes = new ArrayList<>();
        if (!Objects.equals(oldContract.getTitle(), newContract.getTitle())) changes.add("标题");
        if (!Objects.equals(oldContract.getType(), newContract.getType())) changes.add("类型");
        if (!Objects.equals(buildCounterpartySummaryFromJson(oldContract.getCounterparties()), buildCounterpartySummary(loadCounterpartiesAsMaps(newContract)))) changes.add("相对方");
        if (!Objects.equals(oldContract.getAmount(), newContract.getAmount())) changes.add("金额");
        if (!Objects.equals(oldContract.getStartDate(), newContract.getStartDate())) changes.add("开始日期");
        if (!Objects.equals(oldContract.getEndDate(), newContract.getEndDate())) changes.add("结束日期");
        if (!Objects.equals(oldContract.getStatus(), newContract.getStatus())) changes.add("状态");
        if (!Objects.equals(oldContract.getRemark(), newContract.getRemark())) changes.add("备注");
        if (!Objects.equals(oldContract.getContent(), newContract.getContent())) changes.add("正文");
        if (changes.isEmpty()) {
            return "更新合同";
        }
        return "更新合同：" + String.join("、", changes);
    }

    private Map<String, Object> buildRelatedContracts(Contract contract) {
        Map<String, Object> related = new HashMap<>();
        related.put("relationType", contract.getRelationType() == null ? "MAIN" : contract.getRelationType());
        related.put("parentContract", null);
        related.put("supplements", new ArrayList<>());

        if (contract.getParentContractId() != null) {
            Contract parent = contractMapper.selectById(contract.getParentContractId());
            if (parent != null) {
                related.put("parentContract", toRelatedContract(parent));
            }
        }

        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_contract_id", contract.getId()).orderByDesc("create_time");
        List<Map<String, Object>> supplements = contractMapper.selectList(wrapper).stream()
            .map(this::toRelatedContract)
            .toList();
        related.put("supplements", supplements);
        return related;
    }

    private Map<String, Object> toRelatedContract(Contract c) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", c.getId());
        item.put("contractNo", c.getContractNo());
        item.put("title", c.getTitle());
        item.put("status", c.getStatus());
        item.put("relationType", c.getRelationType() == null ? "MAIN" : c.getRelationType());
        item.put("endDate", c.getEndDate());
        return item;
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

    private void validateDynamicFieldQuickCodeValues(String contractType, Map<String, Object> dynamicFields) {
        if (contractType == null || contractType.isBlank() || dynamicFields == null || dynamicFields.isEmpty()) {
            return;
        }
        List<ContractTypeField> configuredFields = typeFieldMapper.selectByContractType(contractType);
        if (configuredFields == null || configuredFields.isEmpty()) {
            return;
        }
        Map<String, ContractTypeField> fieldConfigMap = new HashMap<>();
        for (ContractTypeField field : configuredFields) {
            fieldConfigMap.put(field.getFieldKey(), field);
        }

        Map<String, Set<String>> quickCodeValidValueCache = new HashMap<>();
        for (Map.Entry<String, Object> entry : dynamicFields.entrySet()) {
            String fieldKey = entry.getKey();
            ContractTypeField field = fieldConfigMap.get(fieldKey);
            if (field == null) continue;
            String fieldType = field.getFieldType();
            if (!"select".equals(fieldType) && !"multiselect".equals(fieldType)) continue;

            String quickCodeCode = field.getQuickCodeId();
            if (quickCodeCode == null || quickCodeCode.isBlank()) continue;

            Set<String> allowedValues = quickCodeValidValueCache.computeIfAbsent(
                quickCodeCode,
                this::loadValidQuickCodeValues
            );
            if (allowedValues.isEmpty()) {
                throw new BusinessException(
                    "动态字段快码无可用选项",
                    "error.contract.dynamicQuickCodeNoOptions",
                    Map.of(
                        "field", field.getFieldLabel(),
                        "quickCode", quickCodeCode
                    )
                );
            }

            List<String> selectedValues = normalizeSelectedQuickCodeValues(entry.getValue(), "multiselect".equals(fieldType));
            for (String value : selectedValues) {
                if (!allowedValues.contains(value)) {
                    throw new BusinessException(
                        "动态字段包含无效快码值",
                        "error.contract.dynamicQuickCodeInvalidValue",
                        Map.of(
                            "field", field.getFieldLabel(),
                            "value", value,
                            "quickCode", quickCodeCode
                        )
                    );
                }
            }
        }
    }

    private Set<String> loadValidQuickCodeValues(String quickCodeCode) {
        if (quickCodeCode == null || quickCodeCode.isBlank()) {
            return Collections.emptySet();
        }
        QueryWrapper<QuickCodeHeader> headerWrapper = new QueryWrapper<>();
        headerWrapper.eq("code", quickCodeCode).eq("status", 1);
        QuickCodeHeader header = quickCodeHeaderMapper.selectOne(headerWrapper);
        if (header == null) return Collections.emptySet();

        List<QuickCodeItem> items = quickCodeItemMapper.selectEnabledByHeaderId(header.getId());
        if (items == null || items.isEmpty()) return Collections.emptySet();

        LocalDate today = LocalDate.now();
        Set<String> result = new HashSet<>();
        for (QuickCodeItem item : items) {
            boolean valid = true;
            if (item.getValidFrom() != null && item.getValidFrom().isAfter(today)) valid = false;
            if (item.getValidTo() != null && item.getValidTo().isBefore(today)) valid = false;
            if (!valid) continue;
            if (item.getCode() != null && !item.getCode().isBlank()) {
                result.add(item.getCode());
            }
        }
        return result;
    }

    private List<String> normalizeSelectedQuickCodeValues(Object valueObj, boolean multiSelect) {
        if (valueObj == null) return Collections.emptyList();
        if (!multiSelect) {
            String single = String.valueOf(valueObj).trim();
            if (single.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(single);
        }

        List<String> result = new ArrayList<>();
        if (valueObj instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item == null) continue;
                String val = String.valueOf(item).trim();
                if (!val.isEmpty()) result.add(val);
            }
            return result;
        }

        String raw = String.valueOf(valueObj).trim();
        if (raw.isEmpty()) return Collections.emptyList();

        if (raw.startsWith("[") && raw.endsWith("]")) {
            try {
                List<?> parsed = objectMapper.readValue(raw, List.class);
                for (Object item : parsed) {
                    if (item == null) continue;
                    String val = String.valueOf(item).trim();
                    if (!val.isEmpty()) result.add(val);
                }
                return result;
            } catch (Exception ignored) {
                // fallthrough
            }
        }

        if (raw.contains(",")) {
            for (String part : raw.split(",")) {
                String val = part.trim();
                if (!val.isEmpty()) result.add(val);
            }
            return result;
        }

        result.add(raw);
        return result;
    }

    private List<Map<String, Object>> resolveCounterparties(List<Map<String, Object>> counterpartiesList) {
        if (counterpartiesList != null) {
            return counterpartiesList;
        }
        return new ArrayList<>();
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
                // ignore
            }
        }
        return counterparties;
    }

    private String buildCounterpartySummary(List<Map<String, Object>> counterparties) {
        if (counterparties == null || counterparties.isEmpty()) {
            return "";
        }
        return counterparties.stream()
            .map(cp -> cp.get("name"))
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.joining(" / "));
    }

    private String buildCounterpartySummaryFromJson(String counterpartiesJson) {
        if (counterpartiesJson == null || counterpartiesJson.isBlank()) {
            return "";
        }
        try {
            List<Map<String, Object>> counterparties = objectMapper.readValue(
                counterpartiesJson,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );
            return buildCounterpartySummary(counterparties);
        } catch (Exception ignored) {
            return "";
        }
    }
}
