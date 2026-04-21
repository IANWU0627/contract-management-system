package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ApprovalRecord;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractComment;
import com.contracthub.mapper.ApprovalRecordMapper;
import com.contracthub.mapper.ContractCommentMapper;
import com.contracthub.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContractCollaborationService {
    private final ContractService contractService;
    private final ContractCommentMapper commentMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final NotificationService notificationService;

    public ContractCollaborationService(
            ContractService contractService,
            ContractCommentMapper commentMapper,
            ApprovalRecordMapper approvalRecordMapper,
            NotificationService notificationService) {
        this.contractService = contractService;
        this.commentMapper = commentMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.notificationService = notificationService;
    }

    public Map<String, Object> getComments(Long contractId) {
        List<ContractComment> commentList = commentMapper.selectByContractId(contractId);
        List<Map<String, Object>> comments = new ArrayList<>();
        for (ContractComment comment : commentList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("userId", comment.getUserId());
            map.put("userName", comment.getUsername());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("createdAt", comment.getCreatedAt());
            comments.add(map);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", comments);
        result.put("total", comments.size());
        return result;
    }

    public List<Map<String, Object>> getApprovalHistory(Long contractId) {
        List<ApprovalRecord> records = approvalRecordMapper.selectList(
                new QueryWrapper<ApprovalRecord>().eq("contract_id", contractId).orderByAsc("create_time")
        );

        List<Map<String, Object>> history = new ArrayList<>();
        for (ApprovalRecord record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("approverId", record.getApproverId());
            map.put("approverName", record.getApproverName());
            map.put("action", record.getStatus());
            map.put("comment", record.getComment());
            map.put("timestamp", record.getCreateTime());
            history.add(map);
        }
        return history;
    }

    public ApiResponse<Map<String, Object>> addComment(Long contractId, Map<String, Object> commentData) {
        Contract contract = contractService.getContractById(contractId);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }

        ContractComment comment = new ContractComment();
        comment.setContractId(contractId);
        comment.setUserId(SecurityUtils.getCurrentUserId());
        comment.setUsername(SecurityUtils.getCurrentUserName());
        comment.setContent((String) commentData.get("content"));
        comment.setParentId(commentData.containsKey("parentId") ? (Long) commentData.get("parentId") : null);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        Set<Long> recipients = new LinkedHashSet<>();
        Long currentUserId = comment.getUserId();
        if (contract.getCreatorId() != null && !contract.getCreatorId().equals(currentUserId)) {
            recipients.add(contract.getCreatorId());
        }
        if (comment.getParentId() != null) {
            ContractComment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null && parentComment.getUserId() != null && !parentComment.getUserId().equals(currentUserId)) {
                recipients.add(parentComment.getUserId());
            }
        }
        for (Long recipientId : recipients) {
            notificationService.sendCommentNotification(
                    recipientId,
                    contractId,
                    contract.getContractNo(),
                    comment.getUsername(),
                    comment.getContent()
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("userId", comment.getUserId());
        result.put("userName", comment.getUsername());
        result.put("content", comment.getContent());
        result.put("parentId", comment.getParentId());
        result.put("createdAt", comment.getCreatedAt());
        result.put("userAvatar", "");
        return ApiResponse.success(result);
    }

    public ApiResponse<Void> deleteComment(Long commentId) {
        int result = commentMapper.deleteById(commentId);
        if (result == 0) {
            return ApiResponse.error("评论不存在", "error.comment.notFound");
        }
        return ApiResponse.success(null);
    }
}
