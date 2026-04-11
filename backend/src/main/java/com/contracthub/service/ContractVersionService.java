package com.contracthub.service;

import com.contracthub.entity.ContractVersion;
import java.util.List;
import java.util.Map;

public interface ContractVersionService {
    
    /**
     * 创建新版本
     */
    ContractVersion createVersion(Long contractId, String content, String changeDesc, 
                                   Long operatorId, String operatorName);
    
    /**
     * 获取版本历史
     */
    List<ContractVersion> getVersionHistory(Long contractId);
    
    /**
     * 获取版本详情
     */
    ContractVersion getVersionDetail(Long contractId, Long versionId);
    
    /**
     * 获取最新版本号
     */
    String getLatestVersion(Long contractId);
    
    /**
     * 恢复指定版本
     */
    Map<String, Object> restoreVersion(Long contractId, Long versionId, 
                                        Long operatorId, String operatorName);
    
    /**
     * 对比两个版本
     */
    Map<String, Object> compareVersions(Long contractId, Long versionId1, Long versionId2);
    
    /**
     * 生成下一个版本号
     */
    String generateNextVersion(Long contractId);
}
