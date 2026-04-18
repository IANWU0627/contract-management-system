package com.contracthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TemplateVariableCreateRequest {

    @NotBlank(message = "变量编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "变量编码格式不正确")
    @Size(max = 64, message = "变量编码长度不能超过64")
    private String code;

    @NotBlank(message = "变量名称不能为空")
    @Size(max = 128, message = "变量名称长度不能超过128")
    private String name;

    @Size(max = 128, message = "变量英文名称长度不能超过128")
    private String nameEn;

    @Size(max = 128, message = "显示标签长度不能超过128")
    private String label;

    @NotBlank(message = "变量类型不能为空")
    @Size(max = 32, message = "变量类型长度不能超过32")
    private String type = "text";

    @Size(max = 64, message = "快速代码长度不能超过64")
    private String quickCodeCode;

    @NotBlank(message = "分类不能为空")
    @Size(max = 64, message = "分类长度不能超过64")
    private String category = "custom";

    @Size(max = 255, message = "默认值长度不能超过255")
    private String defaultValue;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    private Boolean required = false;

    private Integer sortOrder = 0;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuickCodeCode() {
        return quickCodeCode;
    }

    public void setQuickCodeCode(String quickCodeCode) {
        this.quickCodeCode = quickCodeCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
