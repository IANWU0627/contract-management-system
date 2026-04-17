package com.contracthub.service;

import com.contracthub.entity.ContractChangeLog;
import java.util.List;
import java.util.Map;

public interface ContractChangeLogService {
    
    /**
     * 记录合同变更
     */
    ContractChangeLog logChange(Long contractId, String changeType, String fieldName, 
                                 String oldValue, String newValue, Long operatorId, 
                                 String operatorName, String remark);

    /**
     * 记录合同变更（附加 diff 元数据，如长文本长度、自定义标记等，将合并入 diff_json）
     */
    ContractChangeLog logChange(Long contractId, String changeType, String fieldName,
                                String oldValue, String newValue, Long operatorId,
                                String operatorName, String remark, Map<String, Object> diffExtras);
    
    /**
     * 获取合同变更历史
     */
    List<ContractChangeLog> getChangeHistory(Long contractId);
    
    /**
     * 获取指定变更记录
     */
    ContractChangeLog getChangeLog(Long changeLogId);
}
