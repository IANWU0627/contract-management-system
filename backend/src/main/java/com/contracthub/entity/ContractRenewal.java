package com.contracthub.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("contract_renewal")
public class ContractRenewal {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long contractId;
    private LocalDate oldEndDate;
    private LocalDate newEndDate;
    private String renewalType;
    private String status;
    private String remark;
    private Long operatorId;
    private String operatorName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    
    public LocalDate getOldEndDate() { return oldEndDate; }
    public void setOldEndDate(LocalDate oldEndDate) { this.oldEndDate = oldEndDate; }
    
    public LocalDate getNewEndDate() { return newEndDate; }
    public void setNewEndDate(LocalDate newEndDate) { this.newEndDate = newEndDate; }
    
    public String getRenewalType() { return renewalType; }
    public void setRenewalType(String renewalType) { this.renewalType = renewalType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
