package com.contracthub.controller;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.UserMapper;
import com.contracthub.service.NotificationService;
import com.contracthub.util.SecurityUtils;
import com.contracthub.entity.ApprovalRecord;
import com.contracthub.entity.User;
import com.contracthub.mapper.ApprovalRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ContractControllerHelper {

    public static final String UPLOAD_DIR = "./uploads";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ContractFieldValueMapper fieldValueMapper;
    private final UserMapper userMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final NotificationService notificationService;
    private final ContractMapper contractMapper;

    public ContractControllerHelper(ContractFieldValueMapper fieldValueMapper,
                                      UserMapper userMapper,
                                      ApprovalRecordMapper approvalRecordMapper,
                                      NotificationService notificationService,
                                      ContractMapper contractMapper) {
        this.fieldValueMapper = fieldValueMapper;
        this.userMapper = userMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.notificationService = notificationService;
        this.contractMapper = contractMapper;
    }

    public String generateContractNo() {
        String date = LocalDate.now().format(formatter);
        String prefix = "TC" + date + "-";
        long count = contractMapper.selectCount(null);
        int nextSeq = (int) count + 1;
        return prefix + String.format("%04d", nextSeq);
    }

    public String getNextContractNo() {
        String date = LocalDate.now().format(formatter);
        String prefix = "TC" + date + "-";
        long count = contractMapper.selectCount(null);
        int nextSeq = (int) count + 1;
        return prefix + String.format("%04d", nextSeq);
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        if (fileName.contains("\0")) {
            return false;
        }
        return true;
    }

    public File getSecureFile(String fileName) {
        if (!isValidFileName(fileName)) {
            return null;
        }

        File uploadDir = new File(UPLOAD_DIR);
        File requestedFile = new File(uploadDir, fileName);

        try {
            String canonicalDir = uploadDir.getCanonicalPath();
            String canonicalFile = requestedFile.getCanonicalPath();

            if (!canonicalFile.startsWith(canonicalDir + File.separator)) {
                return null;
            }

            return requestedFile;
        } catch (Exception e) {
            return null;
        }
    }

    public String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }

    public String encodeFileName(String fileName) throws UnsupportedEncodingException {
        return URLEncoder.encode(fileName, "UTF-8");
    }

    public void notifyApprovers(Contract contract) {
        List<User> approvers = userMapper.selectList(
            new QueryWrapper<User>().in("role", Arrays.asList("ADMIN", "LEGAL"))
        );

        for (User approver : approvers) {
            notificationService.sendApprovalRequest(approver.getId(), contract.getId(), 
                contract.getContractNo(), contract.getTitle());
        }
    }

    public void saveApprovalRecord(Long contractId, String action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setContractId(contractId);
        record.setApproverId(SecurityUtils.getCurrentUserId());
        record.setApproverName(SecurityUtils.getCurrentUserName());
        record.setStatus(action);
        record.setComment(comment);
        record.setCreateTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    public Map<String, Object> getContractAdditionalData(Long contractId) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> dynamicFields = new HashMap<>();
        List<ContractFieldValue> fieldValues = fieldValueMapper.selectByContractId(contractId);
        for (ContractFieldValue fv : fieldValues) {
            dynamicFields.put(fv.getFieldKey(), fv.getFieldValue());
        }
        data.put("dynamicFields", dynamicFields);

        return data;
    }

    public String getContractTypeName(String type) {
        if (type == null) return "其他";
        switch (type.toUpperCase()) {
            case "PURCHASE": return "采购合同";
            case "SALES": return "销售合同";
            case "SERVICE": return "服务合同";
            case "LEASE": return "租赁合同";
            case "EMPLOYMENT": return "雇佣合同";
            case "OTHER": return "其他";
            default: return "其他";
        }
    }

    public String getContractStatusName(String status) {
        if (status == null) return "其他";
        switch (status) {
            case "DRAFT": return "草稿";
            case "PENDING": return "待审批";
            case "APPROVED": return "已审批";
            case "SIGNED": return "已签署";
            case "ARCHIVED": return "已归档";
            case "TERMINATED": return "已终止";
            default: return "其他";
        }
    }

    public Map<String, Object> buildContractResponse(Contract contract) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", contract.getId());
        result.put("contractNo", contract.getContractNo());
        result.put("title", contract.getTitle());
        result.put("type", contract.getType());
        result.put("counterparty", contract.getCounterparty());
        result.put("amount", contract.getAmount());
        result.put("startDate", contract.getStartDate());
        result.put("endDate", contract.getEndDate());
        result.put("status", contract.getStatus());
        result.put("content", contract.getContent());
        result.put("attachment", contract.getAttachment());
        result.put("remark", contract.getRemark());
        result.put("createdBy", SecurityUtils.getCurrentUserName());
        result.put("createdAt", contract.getCreateTime());
        result.put("updatedAt", contract.getUpdateTime());
        return result;
    }
}
