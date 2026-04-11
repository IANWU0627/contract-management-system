package com.contracthub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.entity.ContractChangeLog;
import com.contracthub.mapper.ContractChangeLogMapper;
import com.contracthub.service.ContractChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContractChangeLogServiceImpl implements ContractChangeLogService {

    @Autowired
    private ContractChangeLogMapper changeLogMapper;

    @Override
    @Transactional
    public ContractChangeLog logChange(Long contractId, String changeType, String fieldName,
                                        String oldValue, String newValue, Long operatorId,
                                        String operatorName, String remark) {
        ContractChangeLog changeLog = new ContractChangeLog();
        changeLog.setContractId(contractId);
        changeLog.setChangeType(changeType);
        changeLog.setFieldName(fieldName);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(newValue);
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
}
