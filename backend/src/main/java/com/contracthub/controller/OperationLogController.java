package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.OperationLog;
import com.contracthub.mapper.OperationLogMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        
        if (module != null && !module.isEmpty()) {
            wrapper.eq("module", module);
        }
        if (targetId != null) {
            wrapper.eq("target_id", targetId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("description", keyword)
                             .or()
                             .like("operator_name", keyword));
        }
        
        Page<OperationLog> pagination = new Page<>(page, pageSize);
        Page<OperationLog> resultPage = operationLogMapper.selectPage(pagination, wrapper);
        
        List<Map<String, Object>> logs = new ArrayList<>();
        for (OperationLog log : resultPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("module", log.getModule());
            map.put("operation", log.getOperation());
            map.put("targetId", log.getTargetId());
            map.put("description", log.getDescription());
            map.put("operatorId", log.getOperatorId());
            map.put("operatorName", log.getOperatorName());
            map.put("ip", log.getIp());
            map.put("detail", log.getDetail());
            map.put("createdAt", log.getCreatedAt());
            logs.add(map);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", resultPage.getTotal());
        result.put("page", resultPage.getCurrent());
        result.put("pageSize", resultPage.getSize());
        result.put("pages", resultPage.getPages());
        
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getLogDetail(@PathVariable Long id) {
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            return ApiResponse.error("日志不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", log.getId());
        result.put("module", log.getModule());
        result.put("operation", log.getOperation());
        result.put("targetId", log.getTargetId());
        result.put("description", log.getDescription());
        result.put("operatorId", log.getOperatorId());
        result.put("operatorName", log.getOperatorName());
        result.put("ip", log.getIp());
        result.put("detail", log.getDetail());
        result.put("createdAt", log.getCreatedAt());
        
        return ApiResponse.success(result);
    }

    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<String>> getModules() {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT module")
              .isNotNull("module")
              .orderByAsc("module");
        List<Map<String, Object>> result = operationLogMapper.selectMaps(wrapper);
        
        List<String> modules = new ArrayList<>();
        for (Map<String, Object> row : result) {
            if (row.get("module") != null) {
                modules.add(row.get("module").toString());
            }
        }
        return ApiResponse.success(modules);
    }
}
