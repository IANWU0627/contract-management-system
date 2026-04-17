package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Map;

public interface ContractService {

    /**
     * 分页查询合同列表
     */
    IPage<Contract> listContracts(Map<String, Object> params, int page, int pageSize);

    /**
     * 根据ID获取合同详情
     */
    Contract getContractById(Long id);

    /**
     * 创建合同
     */
    Contract createContract(Map<String, Object> contractMap);

    /**
     * 更新合同
     */
    Contract updateContract(Long id, Map<String, Object> contractMap);

    /**
     * 删除合同
     */
    void deleteContract(Long id);

    /**
     * 批量删除合同
     */
    void batchDeleteContracts(java.util.List<Long> ids);

    /**
     * 批量更新状态
     */
    Map<String, Object> batchUpdateStatus(Map<String, Object> request);

    /**
     * 批量编辑合同
     */
    Map<String, Object> batchEditContracts(Map<String, Object> request);

    /**
     * 提交审批
     */
    Contract submitForApproval(Long id);

    /**
     * 审批通过
     */
    Contract approveContract(Long id, String comment);

    /**
     * 审批拒绝
     */
    Contract rejectContract(Long id, String comment);

    /**
     * 撤回审批
     */
    Contract withdrawApproval(Long id, String reason);

    /**
     * 签署合同
     */
    Contract signContract(Long id);

    /**
     * 归档合同
     */
    Contract archiveContract(Long id);

    /**
     * 终止合同
     */
    Contract terminateContract(Long id, String reason);

    /**
     * 发起续签流程
     */
    Contract startRenewal(Long id, String reason);

    /**
     * 续签完成
     */
    Contract completeRenewal(Long id, String reason);

    /**
     * 标记不续签
     */
    Contract markNotRenewed(Long id, String reason);

    /**
     * 复制合同
     */
    Contract copyContract(Long id);

    /**
     * 构建合同响应Map
     */
    Map<String, Object> buildContractResponse(Contract contract);

    /**
     * 构建合同列表响应Map（轻量，不含正文大字段）
     */
    Map<String, Object> buildContractListResponse(Contract contract);

    /**
     * 构建合同详情响应Map（包含fieldDefinitions）
     */
    Map<String, Object> buildContractDetailResponse(Contract contract);

    /**
     * 获取合同 payload（正文/变量/附件等大字段）
     */
    Map<String, Object> buildContractPayloadResponse(Long id);

    /**
     * 获取合同快照列表（按时间倒序）
     */
    java.util.List<Map<String, Object>> listContractSnapshots(Long id);

    /**
     * 获取指定快照详情
     */
    Map<String, Object> getContractSnapshot(Long id, Long snapshotId);

    /**
     * 对比两个审批快照并返回风险解释
     */
    Map<String, Object> compareContractSnapshots(Long id, Long baseSnapshotId, Long targetSnapshotId);

    /**
     * 生成审批摘要卡片数据
     */
    Map<String, Object> generateApprovalSummary(Long id, boolean forceRefresh);

    /**
     * 获取审批摘要卡片数据
     */
    Map<String, Object> getApprovalSummary(Long id);
}