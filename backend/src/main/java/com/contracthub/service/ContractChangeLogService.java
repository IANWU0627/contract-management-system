package com.contracthub.service;

import com.contracthub.entity.ContractChangeLog;
import java.util.List;

public interface ContractChangeLogService {
    
    /**
     * 记录合同变更
     */
    ContractChangeLog logChange(Long contractId, String changeType, String fieldName, 
                                 String oldValue, String newValue, Long operatorId, 
                                 String operatorName, String remark);
    
    /**
     * 获取合同变更历史
     */
    List<ContractChangeLog> getChangeHistory(Long contractId);
    
    /**
     * 获取指定变更记录
     */
    ContractChangeLog getChangeLog(Long changeLogId);
}
