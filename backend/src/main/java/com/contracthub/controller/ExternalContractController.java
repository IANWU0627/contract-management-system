package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.service.ContractService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统对接API控制器
 * 专门用于与其他系统对接的合同管理接口
 * 
 * 使用说明：
 * 1. 所有接口路径以 /api/external 开头
 * 2. 建议使用API Key进行认证（可在SecurityConfig中配置）
 * 3. 支持JSON格式请求/响应
 * 4. 提供批量操作接口提高效率
 */
@RestController
@RequestMapping("/api/external/contracts")
public class ExternalContractController {

    private final ContractService contractService;
    private final String externalApiKey;

    public ExternalContractController(
            ContractService contractService,
            @Value("${external.api-key:${EXTERNAL_API_KEY:}}") String externalApiKey) {
        this.contractService = contractService;
        this.externalApiKey = externalApiKey;
    }

    /**
     * 外部系统创建合同接口
     * 
     * 请求示例：
     * {
     *   "title": "采购合同20260410",
     *   "contractNo": "TC20260410-0001",
     *   "type": "PURCHASE",
     *   "status": "DRAFT",
     *   "amount": 100000.00,
     *   "startDate": "2026-04-10",
     *   "endDate": "2027-04-09",
     *   "content": "合同内容...",
     *   "remark": "备注信息",
     *   "counterparties": [
     *     {
     *       "type": "partyA",
     *       "name": "甲方公司",
     *       "contact": "张三",
     *       "phone": "13800138000",
     *       "email": "zhangsan@example.com",
     *       "creditCode": "91110000MA001ABC12",
     *       "address": "北京市朝阳区",
     *       "bank": "中国银行",
     *       "account": "6222021234567890"
     *     }
     *   ],
     *   "dynamicFields": {
     *     "productName": "产品A",
     *     "quantity": 100,
     *     "unitPrice": 1000.00
     *   },
     *   "folderId": 1,
     *   "tagIds": [1, 2, 3],
     *   "creatorId": 1,
     *   "creatorName": "系统管理员"
     * }
     * 
     * 字段说明：
     * - title: 合同标题（必填）
     * - contractNo: 合同编号（可选，不填则自动生成）
     * - type: 合同类型（必填）PURCHASE/SALES/SERVICE/LEASE/EMPLOYMENT/OTHER
     * - status: 合同状态（可选，默认DRAFT）DRAFT/PENDING/APPROVING/APPROVED/SIGNED/ARCHIVED/TERMINATED
     * - amount: 合同金额（可选）
     * - startDate: 开始日期（可选，格式：yyyy-MM-dd）
     * - endDate: 结束日期（可选，格式：yyyy-MM-dd）
     * - content: 合同内容（可选）
     * - remark: 备注（可选）
     * - folderId: 文件夹ID（可选）
     * - tagIds: 标签ID列表（可选）
     * - creatorId: 创建人ID（可选）
     * - creatorName: 创建人姓名（可选）
     * - dynamicFields: 动态字段（可选，根据合同类型）
     * * - counterparties: 相对方列表（可选）
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createContract(
            @RequestBody Map<String, Object> contractMap,
            HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            // 如果没有提供合同编号，自动生成
            if (!contractMap.containsKey("contractNo") || contractMap.get("contractNo") == null) {
                // 可以在这里调用合同编号生成服务
                // contractMap.put("contractNo", contractNumberService.generateNextContractNo());
            }

            // 如果没有提供创建人，使用系统默认
            if (!contractMap.containsKey("creatorId")) {
                contractMap.put("creatorId", 1L);
            }
            if (!contractMap.containsKey("creatorName")) {
                contractMap.put("creatorName", "系统对接");
            }

            // 如果没有提供状态，默认草稿
            if (!contractMap.containsKey("status")) {
                contractMap.put("status", "DRAFT");
            }

            // 调用Service层创建合同
            Contract contract = contractService.createContract(contractMap);

            // 构建响应
            Map<String, Object> result = contractService.buildContractResponse(contract);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("创建合同失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建合同接口
     * 提高系统对接效率，一次调用创建多个合同
     * 
     * 请求示例：
     * {
     *   "contracts": [
     *     { "title": "合同1", "type": "PURCHASE", ... },
     *     { "title": "合同2", "type": "SALES", ... }
     *   ]
     * }
     */
    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batchCreateContracts(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpServletRequest) {
        if (!isExternalRequestAuthorized(httpServletRequest)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> contractsData = 
                (List<Map<String, Object>>) request.get("contracts");

            if (contractsData == null || contractsData.isEmpty()) {
                return ApiResponse.error("合同列表不能为空");
            }

            List<Map<String, Object>> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < contractsData.size(); i++) {
                Map<String, Object> contractMap = contractsData.get(i);
                try {
                    // 如果没有提供创建人，使用系统默认
                    if (!contractMap.containsKey("creatorId")) {
                        contractMap.put("creatorId", 1L);
                    }
                    if (!contractMap.containsKey("creatorName")) {
                        contractMap.put("creatorName", "系统对接");
                    }
                    if (!contractMap.containsKey("status")) {
                        contractMap.put("status", "DRAFT");
                    }

                    Contract contract = contractService.createContract(contractMap);
                    results.add(contractService.buildContractResponse(contract));
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "个合同创建失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("results", results);
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("批量创建合同失败: " + e.getMessage());
        }
    }

    /**
     * 查询合同接口
     * 支持按条件查询合同
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> queryContracts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("contractNo", contractNo);
            params.put("type", type);
            params.put("status", status);

            com.baomidou.mybatisplus.core.metadata.IPage<Contract> resultPage = 
                contractService.listContracts(params, page, pageSize);

            List<Map<String, Object>> pageList = new ArrayList<>();
            for (Contract contract : resultPage.getRecords()) {
                pageList.add(contractService.buildContractResponse(contract));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageList);
            result.put("total", (int) resultPage.getTotal());
            result.put("page", (int) resultPage.getCurrent());
            result.put("pageSize", (int) resultPage.getSize());

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询合同失败: " + e.getMessage());
        }
    }

    /**
     * 查询单个合同详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getContractDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) {
                return ApiResponse.error("合同不存在");
            }

            Map<String, Object> result = contractService.buildContractDetailResponse(contract);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询合同详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新合同接口
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateContract(
            @PathVariable Long id,
            @RequestBody Map<String, Object> contractMap,
            HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            Contract contract = contractService.updateContract(id, contractMap);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("更新合同失败: " + e.getMessage());
        }
    }

    /**
     * 删除合同接口
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContract(
            @PathVariable Long id,
            HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            contractService.deleteContract(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("删除合同失败: " + e.getMessage());
        }
    }

    /**
     * 获取下一个合同编号
     */
    @GetMapping("/next-number")
    public ApiResponse<Map<String, Object>> getNextContractNumber(HttpServletRequest request) {
        if (!isExternalRequestAuthorized(request)) {
            return ApiResponse.error("未授权访问外部合同接口");
        }
        try {
            // 这里可以调用合同编号生成服务
            // String contractNo = contractNumberService.generateNextContractNo();
            Map<String, Object> result = new HashMap<>();
            // result.put("contractNo", contractNo);
            result.put("message", "合同编号生成功能待实现");
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取合同编号失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 用于外部系统检查API是否可用
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("timestamp", System.currentTimeMillis());
        result.put("service", "External Contract API");
        result.put("version", "1.0.0");
        return ApiResponse.success(result);
    }

    private boolean isExternalRequestAuthorized(HttpServletRequest request) {
        if (!StringUtils.hasText(externalApiKey)) {
            return false;
        }
        String requestApiKey = request.getHeader("X-API-Key");
        if (!StringUtils.hasText(requestApiKey)) {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                requestApiKey = authHeader.substring(7);
            }
        }
        return StringUtils.hasText(requestApiKey) && externalApiKey.equals(requestApiKey);
    }
}
