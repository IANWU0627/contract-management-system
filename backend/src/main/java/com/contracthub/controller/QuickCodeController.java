package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.QuickCodeHeader;
import com.contracthub.entity.QuickCodeItem;
import com.contracthub.mapper.QuickCodeHeaderMapper;
import com.contracthub.mapper.QuickCodeItemMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 快速代码管理 Controller
 */
@RestController
@RequestMapping("/api/quick-codes")
public class QuickCodeController {
    
    private final QuickCodeHeaderMapper headerMapper;
    private final QuickCodeItemMapper itemMapper;
    
    public QuickCodeController(QuickCodeHeaderMapper headerMapper, QuickCodeItemMapper itemMapper) {
        this.headerMapper = headerMapper;
        this.itemMapper = itemMapper;
    }
    
    /**
     * 获取所有快速代码头（用于下拉选择）
     */
    @GetMapping
    @Cacheable(value = "quickCodes", key = "'list:' + #acceptLanguage")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestHeader(value = "Accept-Language", defaultValue = "zh") String acceptLanguage) {
        QueryWrapper<QuickCodeHeader> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByAsc("id");
        
        List<QuickCodeHeader> headers = headerMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        boolean isEnglish = acceptLanguage != null && acceptLanguage.toLowerCase().contains("en");
        
        for (QuickCodeHeader header : headers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", header.getId());
            map.put("name", isEnglish && header.getNameEn() != null ? header.getNameEn() : header.getName());
            map.put("code", header.getCode());
            map.put("description", isEnglish && header.getDescriptionEn() != null ? header.getDescriptionEn() : header.getDescription());
            result.add(map);
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取所有快速代码（分页，用于管理页面）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<QuickCodeHeader> pagination = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<QuickCodeHeader> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        
        com.baomidou.mybatisplus.core.metadata.IPage<QuickCodeHeader> pageResult = 
            headerMapper.selectPage(pagination, wrapper);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuickCodeHeader header : pageResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", header.getId());
            map.put("name", header.getName());
            map.put("nameEn", header.getNameEn());
            map.put("code", header.getCode());
            map.put("description", header.getDescription());
            map.put("descriptionEn", header.getDescriptionEn());
            map.put("status", header.getStatus());
            map.put("createdAt", header.getCreatedAt());
            map.put("updatedAt", header.getUpdatedAt());
            
            QueryWrapper<QuickCodeItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("header_id", header.getId());
            long itemCount = itemMapper.selectCount(itemWrapper);
            map.put("itemCount", itemCount);
            
            list.add(map);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取单个快速代码头详情（包含所有选项）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long id) {
        QuickCodeHeader header = headerMapper.selectById(id);
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", header.getId());
        result.put("name", header.getName());
        result.put("nameEn", header.getNameEn());
        result.put("code", header.getCode());
        result.put("description", header.getDescription());
        result.put("descriptionEn", header.getDescriptionEn());
        result.put("status", header.getStatus());
        result.put("createdAt", header.getCreatedAt());
        result.put("updatedAt", header.getUpdatedAt());
        
        List<QuickCodeItem> items = itemMapper.selectByHeaderId(id);
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (QuickCodeItem item : items) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("headerId", item.getHeaderId());
            itemMap.put("code", item.getCode());
            itemMap.put("meaning", item.getMeaning());
            itemMap.put("meaningEn", item.getMeaningEn());
            itemMap.put("description", item.getDescription());
            itemMap.put("descriptionEn", item.getDescriptionEn());
            itemMap.put("tag", item.getTag());
            itemMap.put("validFrom", item.getValidFrom());
            itemMap.put("validTo", item.getValidTo());
            itemMap.put("enabled", item.getEnabled());
            itemMap.put("sortOrder", item.getSortOrder());
            itemList.add(itemMap);
        }
        result.put("items", itemList);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 根据编码获取快速代码选项（用于下拉）
     */
    @GetMapping("/code/{code}")
    @Cacheable(value = "quickCodes", key = "'code:' + #code + ':' + #acceptLanguage")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> getByCode(
            @PathVariable String code,
            @RequestHeader(value = "Accept-Language", defaultValue = "zh") String acceptLanguage) {
        QueryWrapper<QuickCodeHeader> headerWrapper = new QueryWrapper<>();
        headerWrapper.eq("code", code);
        headerWrapper.eq("status", 1);
        QuickCodeHeader header = headerMapper.selectOne(headerWrapper);
        
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        List<QuickCodeItem> items = itemMapper.selectEnabledByHeaderId(header.getId());
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (QuickCodeItem item : items) {
            boolean valid = true;
            if (item.getValidFrom() != null && item.getValidFrom().isAfter(today)) {
                valid = false;
            }
            if (item.getValidTo() != null && item.getValidTo().isBefore(today)) {
                valid = false;
            }
            if (!valid) continue;
            
            Map<String, Object> map = new HashMap<>();
            map.put("code", item.getCode());
            map.put("meaning", item.getMeaning());
            map.put("meaningEn", item.getMeaningEn());
            result.add(map);
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建快速代码
     */
    @PostMapping
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        String name = (String) data.get("name");
        String code = (String) data.get("code");
        
        QueryWrapper<QuickCodeHeader> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        if (headerMapper.selectCount(wrapper) > 0) {
            return ApiResponse.error("编码已存在");
        }
        
        wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (headerMapper.selectCount(wrapper) > 0) {
            return ApiResponse.error("名称已存在");
        }
        
        QuickCodeHeader header = new QuickCodeHeader();
        header.setName(name);
        header.setNameEn((String) data.getOrDefault("nameEn", ""));
        header.setCode(code.toUpperCase());
        header.setDescription((String) data.getOrDefault("description", ""));
        header.setDescriptionEn((String) data.getOrDefault("descriptionEn", ""));
        header.setStatus(1);
        header.setCreatedAt(LocalDateTime.now());
        header.setUpdatedAt(LocalDateTime.now());
        
        headerMapper.insert(header);
        
        if (data.containsKey("items")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
            
            Set<String> itemCodes = new HashSet<>();
            for (Map<String, Object> itemData : items) {
                String itemCode = (String) itemData.get("code");
                if (itemCodes.contains(itemCode)) {
                    return ApiResponse.error("选项代码【" + itemCode + "】重复");
                }
                itemCodes.add(itemCode);
            }
            
            for (Map<String, Object> itemData : items) {
                QuickCodeItem item = new QuickCodeItem();
                item.setHeaderId(header.getId());
                item.setCode((String) itemData.get("code"));
                item.setMeaning((String) itemData.get("meaning"));
                item.setMeaningEn((String) itemData.getOrDefault("meaningEn", ""));
                item.setDescription((String) itemData.getOrDefault("description", ""));
                item.setDescriptionEn((String) itemData.getOrDefault("descriptionEn", ""));
                item.setTag((String) itemData.getOrDefault("tag", ""));
                item.setSortOrder(itemData.get("sortOrder") != null ? (Integer) itemData.get("sortOrder") : 0);
                item.setEnabled(itemData.get("enabled") != null ? (Boolean) itemData.get("enabled") : true);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                itemMapper.insert(item);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", header.getId());
        result.put("name", header.getName());
        result.put("code", header.getCode());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 更新快速代码
     */
    @PutMapping("/{id}")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        QuickCodeHeader header = headerMapper.selectById(id);
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        if (data.containsKey("name")) {
            String newName = (String) data.get("name");
            if (!newName.equals(header.getName())) {
                QueryWrapper<QuickCodeHeader> wrapper = new QueryWrapper<>();
                wrapper.eq("name", newName);
                wrapper.ne("id", id);
                if (headerMapper.selectCount(wrapper) > 0) {
                    return ApiResponse.error("名称已存在");
                }
            }
            header.setName(newName);
        }
        if (data.containsKey("nameEn")) {
            header.setNameEn((String) data.get("nameEn"));
        }
        if (data.containsKey("description")) {
            header.setDescription((String) data.get("description"));
        }
        if (data.containsKey("descriptionEn")) {
            header.setDescriptionEn((String) data.get("descriptionEn"));
        }
        if (data.containsKey("status")) {
            header.setStatus((Integer) data.get("status"));
        }
        
        header.setUpdatedAt(LocalDateTime.now());
        headerMapper.updateById(header);
        
        if (data.containsKey("items")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newItems = (List<Map<String, Object>>) data.get("items");
            
            List<QuickCodeItem> currentItems = itemMapper.selectByHeaderId(id);
            Set<Long> currentItemIds = new HashSet<>();
            for (QuickCodeItem item : currentItems) {
                currentItemIds.add(item.getId());
            }
            
            Set<Long> newItemIds = new HashSet<>();
            Set<String> newItemCodes = new HashSet<>();
            
            for (Map<String, Object> itemData : newItems) {
                String itemCode = (String) itemData.get("code");
                if (newItemCodes.contains(itemCode)) {
                    return ApiResponse.error("选项代码【" + itemCode + "】重复");
                }
                newItemCodes.add(itemCode);
                
                if (itemData.get("id") != null) {
                    newItemIds.add(((Number) itemData.get("id")).longValue());
                }
            }
            
            for (Long itemId : currentItemIds) {
                if (!newItemIds.contains(itemId)) {
                    itemMapper.deleteById(itemId);
                }
            }
            
            for (Map<String, Object> itemData : newItems) {
                if (itemData.get("id") != null) {
                    Long itemId = ((Number) itemData.get("id")).longValue();
                    QuickCodeItem item = itemMapper.selectById(itemId);
                    if (item != null) {
                        item.setCode((String) itemData.get("code"));
                        item.setMeaning((String) itemData.get("meaning"));
                        item.setMeaningEn((String) itemData.getOrDefault("meaningEn", ""));
                        item.setDescription((String) itemData.getOrDefault("description", ""));
                        item.setDescriptionEn((String) itemData.getOrDefault("descriptionEn", ""));
                        item.setTag((String) itemData.getOrDefault("tag", ""));
                        item.setSortOrder(itemData.get("sortOrder") != null ? (Integer) itemData.get("sortOrder") : 0);
                        item.setEnabled(itemData.get("enabled") != null ? (Boolean) itemData.get("enabled") : true);
                        item.setUpdatedAt(LocalDateTime.now());
                        itemMapper.updateById(item);
                    }
                } else {
                    QuickCodeItem item = new QuickCodeItem();
                    item.setHeaderId(id);
                    item.setCode((String) itemData.get("code"));
                    item.setMeaning((String) itemData.get("meaning"));
                    item.setMeaningEn((String) itemData.getOrDefault("meaningEn", ""));
                    item.setDescription((String) itemData.getOrDefault("description", ""));
                    item.setDescriptionEn((String) itemData.getOrDefault("descriptionEn", ""));
                    item.setTag((String) itemData.getOrDefault("tag", ""));
                    item.setSortOrder(itemData.get("sortOrder") != null ? (Integer) itemData.get("sortOrder") : 0);
                    item.setEnabled(itemData.get("enabled") != null ? (Boolean) itemData.get("enabled") : true);
                    item.setCreatedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());
                    itemMapper.insert(item);
                }
            }
        }
        
        return ApiResponse.success(null);
    }
    
    /**
     * 删除快速代码
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        QuickCodeHeader header = headerMapper.selectById(id);
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        QueryWrapper<QuickCodeItem> wrapper = new QueryWrapper<>();
        wrapper.eq("header_id", id);
        itemMapper.delete(wrapper);
        
        headerMapper.deleteById(id);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 切换快速代码状态
     */
    @PutMapping("/{id}/toggle")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Void> toggle(@PathVariable Long id) {
        QuickCodeHeader header = headerMapper.selectById(id);
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        header.setStatus(header.getStatus() == 1 ? 0 : 1);
        header.setUpdatedAt(LocalDateTime.now());
        headerMapper.updateById(header);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 添加快速代码选项
     */
    @PostMapping("/{id}/items")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Map<String, Object>> addItem(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        QuickCodeHeader header = headerMapper.selectById(id);
        if (header == null) {
            return ApiResponse.error("快速代码不存在");
        }
        
        QuickCodeItem item = new QuickCodeItem();
        item.setHeaderId(id);
        item.setCode((String) data.get("code"));
        item.setMeaning((String) data.get("meaning"));
        item.setMeaningEn((String) data.getOrDefault("meaningEn", ""));
        item.setDescription((String) data.getOrDefault("description", ""));
        item.setDescriptionEn((String) data.getOrDefault("descriptionEn", ""));
        item.setTag((String) data.getOrDefault("tag", ""));
        item.setSortOrder(data.get("sortOrder") != null ? (Integer) data.get("sortOrder") : 0);
        item.setEnabled(true);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        itemMapper.insert(item);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", item.getId());
        result.put("code", item.getCode());
        result.put("meaning", item.getMeaning());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 更新快速代码选项
     */
    @PutMapping("/items/{itemId}")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Void> updateItem(@PathVariable Long itemId, @RequestBody Map<String, Object> data) {
        QuickCodeItem item = itemMapper.selectById(itemId);
        if (item == null) {
            return ApiResponse.error("选项不存在");
        }
        
        if (data.containsKey("code")) item.setCode((String) data.get("code"));
        if (data.containsKey("meaning")) item.setMeaning((String) data.get("meaning"));
        if (data.containsKey("meaningEn")) item.setMeaningEn((String) data.get("meaningEn"));
        if (data.containsKey("description")) item.setDescription((String) data.get("description"));
        if (data.containsKey("descriptionEn")) item.setDescriptionEn((String) data.get("descriptionEn"));
        if (data.containsKey("tag")) item.setTag((String) data.get("tag"));
        if (data.containsKey("sortOrder")) item.setSortOrder((Integer) data.get("sortOrder"));
        if (data.containsKey("enabled")) item.setEnabled((Boolean) data.get("enabled"));
        
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 删除快速代码选项
     */
    @DeleteMapping("/items/{itemId}")
    @CacheEvict(value = "quickCodes", allEntries = true)
    @PreAuthorize("hasAuthority('QUICK_CODE_MANAGE')")
    public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
        itemMapper.deleteById(itemId);
        return ApiResponse.success(null);
    }
}
