package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractAttachment;
import com.contracthub.mapper.ContractAttachmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contract-attachments")
@PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
public class ContractAttachmentController {
    
    @Autowired
    private ContractAttachmentMapper attachmentMapper;
    
    @GetMapping("/contract/{contractId}")
    public ApiResponse<List<ContractAttachment>> getByContractId(@PathVariable Long contractId) {
        List<ContractAttachment> list = attachmentMapper.selectByContractId(contractId);
        return ApiResponse.success(list);
    }
    
    @PostMapping
    public ApiResponse<ContractAttachment> create(@RequestBody ContractAttachment attachment) {
        attachmentMapper.insert(attachment);
        return ApiResponse.success(attachment);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<ContractAttachment> update(@PathVariable Long id, @RequestBody ContractAttachment attachment) {
        attachment.setId(id);
        attachmentMapper.updateById(attachment);
        return ApiResponse.success(attachment);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        attachmentMapper.deleteById(id);
        return ApiResponse.success("删除成功");
    }
    
    @PostMapping("/batch")
    public ApiResponse<String> saveBatch(@RequestBody SaveBatchRequest request) {
        if (request.getContractId() != null) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContractAttachment> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("contract_id", request.getContractId());
            attachmentMapper.delete(wrapper);
            
            if (request.getAttachments() != null) {
                for (ContractAttachment att : request.getAttachments()) {
                    att.setContractId(request.getContractId());
                    attachmentMapper.insert(att);
                }
            }
        }
        return ApiResponse.success("保存成功");
    }
    
    public static class SaveBatchRequest {
        private Long contractId;
        private List<ContractAttachment> attachments;
        
        public Long getContractId() { return contractId; }
        public void setContractId(Long contractId) { this.contractId = contractId; }
        public List<ContractAttachment> getAttachments() { return attachments; }
        public void setAttachments(List<ContractAttachment> attachments) { this.attachments = attachments; }
    }
}
