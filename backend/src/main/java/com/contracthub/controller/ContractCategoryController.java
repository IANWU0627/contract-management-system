package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractCategory;
import com.contracthub.entity.ContractTemplate;
import com.contracthub.mapper.ContractCategoryMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractTemplateMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 合同分类管理 Controller
 */
@RestController
@RequestMapping("/api/contract-categories")
public class ContractCategoryController {
    
    private final ContractCategoryMapper categoryMapper;
    private final ContractMapper contractMapper;
    private final ContractTemplateMapper templateMapper;
    
    public ContractCategoryController(
            ContractCategoryMapper categoryMapper,
            ContractMapper contractMapper,
            ContractTemplateMapper templateMapper) {
        this.categoryMapper = categoryMapper;
        this.contractMapper = contractMapper;
        this.templateMapper = templateMapper;
    }
    
    @PostConstruct
    public void init() {
        try {
            // 初始化默认分类
            if (categoryMapper.selectCount(null) == 0) {
                initDefaultCategories();
            }
        } catch (Exception e) {
            // 忽略数据库表不存在的错误，表会通过Flyway创建
        }
    }
    
    private void initDefaultCategories() {
        String[][][] defaults = {
            {{"采购合同"}, {"Purchase Contract"}}, {{"销售合同"}, {"Sales Contract"}},
            {{"服务合同"}, {"Service Contract"}}, {{"租赁合同"}, {"Lease Contract"}},
            {{"劳动合同"}, {"Employment Contract"}}, {{"其他合同"}, {"Other Contract"}}
        };
        String[] codes = {"PURCHASE", "SALES", "SERVICE", "LEASE", "EMPLOYMENT", "OTHER"};
        String[] colors = {"#667eea", "#11998e", "#f093fb", "#fa709a", "#764ba2", "#909399"};
        
        for (int i = 0; i < codes.length; i++) {
            ContractCategory cat = new ContractCategory();
            cat.setName(defaults[i][0][0]);
            cat.setNameEn(defaults[i][1][0]);
            cat.setCode(codes[i]);
            cat.setColor(colors[i]);
            cat.setSortOrder(i);
            cat.setActive(true);
            cat.setCreatedAt(LocalDateTime.now());
            cat.setUpdatedAt(LocalDateTime.now());
            categoryMapper.insert(cat);
        }
    }
    
    /**
     * 获取启用的分类列表（供下拉选择用）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> list() {
        QueryWrapper<ContractCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("active", true);
        wrapper.orderByAsc("sort_order");
        
        List<ContractCategory> categories = categoryMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ContractCategory cat : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cat.getId());
            map.put("name", cat.getName());
            map.put("nameEn", cat.getNameEn());
            map.put("code", cat.getCode());
            map.put("icon", cat.getIcon());
            map.put("color", cat.getColor());
            map.put("sortOrder", cat.getSortOrder());
            map.put("createdAt", cat.getCreatedAt());
            map.put("updatedAt", cat.getUpdatedAt());
            result.add(map);
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取所有分类（分页，用于管理页面）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<ContractCategory> pagination = new Page<>(page, pageSize);
        QueryWrapper<ContractCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        
        IPage<ContractCategory> pageResult = categoryMapper.selectPage(pagination, wrapper);
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (ContractCategory cat : pageResult.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cat.getId());
            map.put("name", cat.getName());
            map.put("nameEn", cat.getNameEn());
            map.put("code", cat.getCode());
            map.put("icon", cat.getIcon());
            map.put("color", cat.getColor());
            map.put("sortOrder", cat.getSortOrder());
            map.put("active", cat.getActive());
            map.put("createdAt", cat.getCreatedAt());
            map.put("updatedAt", cat.getUpdatedAt());
            
            // 计算模板数量
            QueryWrapper<ContractTemplate> templateWrapper = new QueryWrapper<>();
            templateWrapper.eq("category", cat.getCode());
            long templateCount = templateMapper.selectCount(templateWrapper);
            map.put("templateCount", templateCount);
            
            // 计算合同数量
            QueryWrapper<Contract> contractWrapper = new QueryWrapper<>();
            contractWrapper.eq("type", cat.getCode());
            long contractCount = contractMapper.selectCount(contractWrapper);
            map.put("contractCount", contractCount);
            
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
     * 获取单个分类详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long id) {
        ContractCategory cat = categoryMapper.selectById(id);
        if (cat == null) {
            return ApiResponse.error("分类不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", cat.getId());
        result.put("name", cat.getName());
        result.put("nameEn", cat.getNameEn());
        result.put("code", cat.getCode());
        result.put("icon", cat.getIcon());
        result.put("color", cat.getColor());
        result.put("sortOrder", cat.getSortOrder());
        result.put("active", cat.getActive());
        result.put("createdAt", cat.getCreatedAt());
        result.put("updatedAt", cat.getUpdatedAt());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        String name = (String) data.get("name");
        String nameEn = (String) data.get("nameEn");
        String code = (String) data.get("code");
        
        if (nameEn == null || nameEn.trim().isEmpty()) {
            return ApiResponse.error("类型名称(英)不能为空");
        }
        
        QueryWrapper<ContractCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        if (categoryMapper.selectCount(wrapper) > 0) {
            return ApiResponse.error("分类编码已存在");
        }
        
        ContractCategory cat = new ContractCategory();
        cat.setName(name);
        cat.setNameEn(nameEn);
        cat.setCode(code);
        cat.setIcon((String) data.getOrDefault("icon", ""));
        cat.setColor((String) data.getOrDefault("color", "#409eff"));
        cat.setSortOrder(data.get("sortOrder") != null ? (Integer) data.get("sortOrder") : 0);
        cat.setActive(true);
        cat.setCreatedAt(LocalDateTime.now());
        cat.setUpdatedAt(LocalDateTime.now());
        
        categoryMapper.insert(cat);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", cat.getId());
        result.put("name", cat.getName());
        result.put("nameEn", cat.getNameEn());
        result.put("code", cat.getCode());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        ContractCategory cat = categoryMapper.selectById(id);
        if (cat == null) {
            return ApiResponse.error("分类不存在");
        }
        
        if (data.containsKey("name")) {
            cat.setName((String) data.get("name"));
        }
        if (data.containsKey("nameEn")) {
            cat.setNameEn((String) data.get("nameEn"));
        }
        if (data.containsKey("icon")) {
            cat.setIcon((String) data.get("icon"));
        }
        if (data.containsKey("color")) {
            cat.setColor((String) data.get("color"));
        }
        if (data.containsKey("sortOrder")) {
            cat.setSortOrder((Integer) data.get("sortOrder"));
        }
        if (data.containsKey("active")) {
            cat.setActive((Boolean) data.get("active"));
        }
        
        cat.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(cat);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ContractCategory cat = categoryMapper.selectById(id);
        if (cat == null) {
            return ApiResponse.error("分类不存在");
        }
        
        // 系统分类只禁用，不删除
        if ("PURCHASE".equals(cat.getCode()) || "SALES".equals(cat.getCode()) ||
            "SERVICE".equals(cat.getCode()) || "LEASE".equals(cat.getCode()) ||
            "EMPLOYMENT".equals(cat.getCode()) || "OTHER".equals(cat.getCode())) {
            cat.setActive(false);
            cat.setUpdatedAt(LocalDateTime.now());
            categoryMapper.updateById(cat);
        } else {
            categoryMapper.deleteById(id);
        }
        
        return ApiResponse.success(null);
    }
}
