package com.contracthub.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("template_variable")
public class TemplateVariable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String code;
    
    private String name;

    /** 显式映射列名；ALWAYS 避免部分环境下更新时未写入 name_en */
    @TableField(value = "name_en", insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.ALWAYS)
    private String nameEn;

    private String label;
    
    private String type;
    
    private String quickCodeCode;
    
    private String category;
    
    private String defaultValue;
    
    private String description;
    
    private Integer required;
    
    private Integer sortOrder;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuickCodeCode() { return quickCodeCode; }
    public void setQuickCodeCode(String quickCodeCode) { this.quickCodeCode = quickCodeCode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRequired() { return required; }
    public void setRequired(Integer required) { this.required = required; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
