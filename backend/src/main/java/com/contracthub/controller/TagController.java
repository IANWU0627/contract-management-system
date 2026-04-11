package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.mapper.ContractTagMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.entity.ContractTag;
import com.contracthub.entity.Contract;
import com.contracthub.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/tags")
@PreAuthorize("hasAuthority('TAG_MANAGE')")
public class TagController {

    private final ContractTagMapper tagMapper;
    private final ContractMapper contractMapper;

    public TagController(ContractTagMapper tagMapper, ContractMapper contractMapper) {
        this.tagMapper = tagMapper;
        this.contractMapper = contractMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getTags() {
        List<Map<String, Object>> tags = tagMapper.selectTagsWithUsageCount();
        
        List<Map<String, Object>> formattedTags = new ArrayList<>();
        for (Map<String, Object> tag : tags) {
            Map<String, Object> formattedTag = new HashMap<>();
            formattedTag.put("id", getValue(tag, "ID", "id"));
            formattedTag.put("name", getValue(tag, "NAME", "name"));
            formattedTag.put("color", getValue(tag, "COLOR", "color"));
            formattedTag.put("description", getValue(tag, "DESCRIPTION", "description"));
            formattedTag.put("usageCount", getValue(tag, "USAGE_COUNT", "usage_count"));
            formattedTag.put("creatorId", getValue(tag, "CREATOR_ID", "creator_id"));
            formattedTag.put("isPublic", getValue(tag, "IS_PUBLIC", "is_public"));
            formattedTag.put("createdAt", getValue(tag, "CREATED_AT", "created_at"));
            formattedTags.add(formattedTag);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedTags);
        result.put("total", formattedTags.size());
        
        return ApiResponse.success(result);
    }
    
    private Object getValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyTags() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> tags = tagMapper.selectTagsWithUsageCount();
        
        List<Map<String, Object>> filteredTags = new ArrayList<>();
        for (Map<String, Object> tag : tags) {
            Long creatorIdVal = getValue(tag, "CREATOR_ID", "creator_id") != null ? 
                ((Number) getValue(tag, "CREATOR_ID", "creator_id")).longValue() : null;
            Boolean isPublic = getValue(tag, "IS_PUBLIC", "is_public") != null && 
                (Boolean) getValue(tag, "IS_PUBLIC", "is_public");
            
            if (isPublic || (creatorIdVal != null && creatorIdVal.equals(userId))) {
                Map<String, Object> formattedTag = new HashMap<>();
                formattedTag.put("id", getValue(tag, "ID", "id"));
                formattedTag.put("name", getValue(tag, "NAME", "name"));
                formattedTag.put("color", getValue(tag, "COLOR", "color"));
                formattedTag.put("description", getValue(tag, "DESCRIPTION", "description"));
                formattedTag.put("usageCount", getValue(tag, "USAGE_COUNT", "usage_count"));
                formattedTag.put("creatorId", getValue(tag, "CREATOR_ID", "creator_id"));
                formattedTag.put("isPublic", getValue(tag, "IS_PUBLIC", "is_public"));
                formattedTag.put("createdAt", getValue(tag, "CREATED_AT", "created_at"));
                filteredTags.add(formattedTag);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", filteredTags);
        result.put("total", filteredTags.size());
        
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createTag(@RequestBody Map<String, Object> tagData) {
        String name = (String) tagData.get("name");
        String color = (String) tagData.getOrDefault("color", "#667eea");
        String description = (String) tagData.getOrDefault("description", "");
        Boolean isPublic = tagData.containsKey("isPublic") ? (Boolean) tagData.get("isPublic") : true;
        
        QueryWrapper<ContractTag> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        ContractTag existing = tagMapper.selectOne(wrapper);
        if (existing != null) {
            return ApiResponse.error("标签已存在");
        }
        
        ContractTag tag = new ContractTag();
        tag.setName(name);
        tag.setColor(color);
        tag.setDescription(description);
        tag.setCreatorId(SecurityUtils.getCurrentUserId());
        tag.setIsPublic(isPublic);
        tagMapper.insert(tag);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", tag.getId());
        result.put("name", tag.getName());
        result.put("color", tag.getColor());
        result.put("description", tag.getDescription());
        result.put("creatorId", tag.getCreatorId());
        result.put("isPublic", tag.getIsPublic());
        result.put("usageCount", 0);
        
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateTag(@PathVariable Long id, @RequestBody Map<String, Object> tagData) {
        ContractTag tag = tagMapper.selectById(id);
        if (tag == null) {
            return ApiResponse.error("标签不存在");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = tag.getIsPublic() != null && tag.getIsPublic();
        
        // 只有创建者或管理员可以修改
        if (!isPublic && tag.getCreatorId() != null && !tag.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限修改此标签");
        }
        
        if (tagData.containsKey("name")) {
            tag.setName((String) tagData.get("name"));
        }
        if (tagData.containsKey("color")) {
            tag.setColor((String) tagData.get("color"));
        }
        if (tagData.containsKey("description")) {
            tag.setDescription((String) tagData.get("description"));
        }
        
        tagMapper.updateById(tag);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        ContractTag tag = tagMapper.selectById(id);
        if (tag == null) {
            return ApiResponse.error("标签不存在");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Boolean isPublic = tag.getIsPublic() != null && tag.getIsPublic();
        
        // 只有创建者可以删除私有标签
        if (!isPublic && tag.getCreatorId() != null && !tag.getCreatorId().equals(userId)) {
            return ApiResponse.error("您没有权限删除此标签");
        }
        
        tagMapper.deleteTagRelationsByTagId(id);
        tagMapper.deleteById(id);
        
        return ApiResponse.success(null);
    }

    @GetMapping("/contract/{contractId}")
    public ApiResponse<Map<String, Object>> getTagsByContract(@PathVariable Long contractId) {
        List<Long> tagIds = tagMapper.selectTagIdsByContractId(contractId);
        List<Map<String, Object>> tags = new ArrayList<>();
        
        for (Long tagId : tagIds) {
            ContractTag tag = tagMapper.selectById(tagId);
            if (tag != null) {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("id", tag.getId());
                tagMap.put("name", tag.getName());
                tagMap.put("color", tag.getColor());
                tags.add(tagMap);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", tags);
        result.put("total", tags.size());
        
        return ApiResponse.success(result);
    }

    @PostMapping("/contract/{contractId}")
    public ApiResponse<Void> addTagToContract(@PathVariable Long contractId, @RequestBody Map<String, Object> tagData) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return ApiResponse.error("合同不存在");
        }
        
        Long tagId = ((Number) tagData.get("tagId")).longValue();
        ContractTag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            return ApiResponse.error("标签不存在");
        }
        
        tagMapper.insertTagRelation(contractId, tagId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/contract/{contractId}/{tagId}")
    public ApiResponse<Void> removeTagFromContract(@PathVariable Long contractId, @PathVariable Long tagId) {
        tagMapper.deleteTagRelation(contractId, tagId);
        return ApiResponse.success(null);
    }

    @PutMapping("/contract/{contractId}")
    public ApiResponse<Void> updateContractTags(@PathVariable Long contractId, @RequestBody Map<String, Object> data) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return ApiResponse.error("合同不存在");
        }
        
        tagMapper.deleteTagRelationsByContractId(contractId);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tags = (List<Map<String, Object>>) data.get("tags");
        if (tags != null) {
            for (Map<String, Object> tagData : tags) {
                Long tagId = ((Number) tagData.get("id")).longValue();
                tagMapper.insertTagRelation(contractId, tagId);
            }
        }
        
        return ApiResponse.success(null);
    }

    @GetMapping("/{tagId}/contracts")
    public ApiResponse<Map<String, Object>> getContractsByTag(@PathVariable Long tagId) {
        List<Map<String, Object>> contracts = tagMapper.selectContractsByTagId(tagId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", contracts);
        result.put("total", contracts.size());
        
        return ApiResponse.success(result);
    }
}
