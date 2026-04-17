package com.contracthub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.entity.ContractChangeLog;
import com.contracthub.mapper.ContractChangeLogMapper;
import com.contracthub.service.ContractChangeLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

@Service
public class ContractChangeLogServiceImpl implements ContractChangeLogService {
    private static final Set<String> CRITICAL_FIELDS = Set.of(
        "title", "type", "counterparty", "amount", "currency", "startDate", "endDate", "status",
        "parentContractId", "relationType", "content"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ContractChangeLogMapper changeLogMapper;

    @Override
    @Transactional
    public ContractChangeLog logChange(Long contractId, String changeType, String fieldName,
                                        String oldValue, String newValue, Long operatorId,
                                        String operatorName, String remark) {
        return logChange(contractId, changeType, fieldName, oldValue, newValue, operatorId, operatorName, remark, null);
    }

    @Override
    @Transactional
    public ContractChangeLog logChange(Long contractId, String changeType, String fieldName,
                                       String oldValue, String newValue, Long operatorId,
                                       String operatorName, String remark, Map<String, Object> diffExtras) {
        ContractChangeLog changeLog = new ContractChangeLog();
        changeLog.setContractId(contractId);
        changeLog.setChangeType(changeType);
        changeLog.setFieldName(fieldName);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(newValue);
        changeLog.setDiffJson(buildDiffJson(fieldName, oldValue, newValue, operatorId, operatorName, diffExtras));
        changeLog.setOperatorId(operatorId);
        changeLog.setOperatorName(operatorName);
        changeLog.setRemark(remark);
        changeLog.setCreatedAt(LocalDateTime.now());
        
        changeLogMapper.insert(changeLog);
        return changeLog;
    }

    @Override
    public List<ContractChangeLog> getChangeHistory(Long contractId) {
        QueryWrapper<ContractChangeLog> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId)
               .orderByDesc("created_at");
        return changeLogMapper.selectList(wrapper);
    }

    @Override
    public ContractChangeLog getChangeLog(Long changeLogId) {
        return changeLogMapper.selectById(changeLogId);
    }

    private String buildDiffJson(String fieldName, String oldValue, String newValue, Long operatorId,
                               String operatorName, Map<String, Object> diffExtras) {
        if (fieldName == null || fieldName.isBlank()) {
            return null;
        }
        Map<String, Object> diff = new HashMap<>();
        boolean critical = CRITICAL_FIELDS.contains(fieldName);
        diff.put("field", fieldName);
        diff.put("critical", critical);
        diff.put("changed", !java.util.Objects.equals(oldValue, newValue));
        diff.put("oldValue", oldValue);
        diff.put("newValue", newValue);
        diff.put("operatorId", operatorId);
        diff.put("operatorName", operatorName);
        diff.put("changedAt", LocalDateTime.now().toString());
        if (diffExtras != null && !diffExtras.isEmpty()) {
            diff.putAll(diffExtras);
        }
        try {
            return objectMapper.writeValueAsString(diff);
        } catch (Exception e) {
            return null;
        }
    }
}
