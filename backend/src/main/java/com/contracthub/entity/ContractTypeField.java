package com.contracthub.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("contract_type_field")
public class ContractTypeField {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String contractType;
    private String fieldKey;
    private String fieldLabel;
    private String fieldLabelEn;
    private String fieldType;
    private String quickCodeId;
    private Boolean required;
    private Boolean showInList;
    private Boolean showInForm;
    private Integer fieldOrder;
    private String placeholder;
    private String placeholderEn;
    private String defaultValue;
    private String options;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    
    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }
    
    public String getFieldLabelEn() { return fieldLabelEn; }
    public void setFieldLabelEn(String fieldLabelEn) { this.fieldLabelEn = fieldLabelEn; }
    
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    
    public String getQuickCodeId() { return quickCodeId; }
    public void setQuickCodeId(String quickCodeId) { this.quickCodeId = quickCodeId; }
    
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    
    public Boolean getShowInList() { return showInList; }
    public void setShowInList(Boolean showInList) { this.showInList = showInList; }
    
    public Boolean getShowInForm() { return showInForm; }
    public void setShowInForm(Boolean showInForm) { this.showInForm = showInForm; }
    
    public Integer getFieldOrder() { return fieldOrder; }
    public void setFieldOrder(Integer fieldOrder) { this.fieldOrder = fieldOrder; }
    
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    
    public String getPlaceholderEn() { return placeholderEn; }
    public void setPlaceholderEn(String placeholderEn) { this.placeholderEn = placeholderEn; }
    
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
