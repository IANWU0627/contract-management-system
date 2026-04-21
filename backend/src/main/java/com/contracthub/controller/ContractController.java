package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.service.ContractNumberService;
import com.contracthub.service.ContractService;
import com.contracthub.service.ContractPerformanceMilestoneService;
import com.contracthub.service.ContractFileService;
import com.contracthub.service.ContractExportService;
import com.contracthub.service.ContractWorkflowService;
import com.contracthub.service.ContractCollaborationService;
import com.contracthub.service.ContractAiGatewayService;
import com.contracthub.service.ContractCommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractNumberService contractNumberService;
    private final ContractService contractService;
    private final ContractPerformanceMilestoneService performanceMilestoneService;
    private final ContractFileService contractFileService;
    private final ContractExportService contractExportService;
    private final ContractWorkflowService contractWorkflowService;
    private final ContractCollaborationService contractCollaborationService;
    private final ContractAiGatewayService contractAiGatewayService;
    private final ContractCommandService contractCommandService;
    
    public ContractController(ContractNumberService contractNumberService,
                              ContractService contractService,
                              ContractPerformanceMilestoneService performanceMilestoneService,
                              ContractFileService contractFileService,
                              ContractExportService contractExportService,
                              ContractWorkflowService contractWorkflowService,
                              ContractCollaborationService contractCollaborationService,
                              ContractAiGatewayService contractAiGatewayService,
                              ContractCommandService contractCommandService) {
        this.contractNumberService = contractNumberService;
        this.contractService = contractService;
        this.performanceMilestoneService = performanceMilestoneService;
        this.contractFileService = contractFileService;
        this.contractExportService = contractExportService;
        this.contractWorkflowService = contractWorkflowService;
        this.contractCollaborationService = contractCollaborationService;
        this.contractAiGatewayService = contractAiGatewayService;
        this.contractCommandService = contractCommandService;
    }
    
    /**
     * 获取下一个合同编号
     * 规则：TC + 年月日 + 4位流水号
     * 例如：TC20260327-0001
     */
    @GetMapping("/next-number")
    public ApiResponse<Map<String, Object>> getNextContractNo() {
        String contractNo = contractNumberService.generateNextContractNo();
        Map<String, Object> result = new HashMap<>();
        result.put("contractNo", contractNo);
        return ApiResponse.success(result);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String endDateFrom,
            @RequestParam(required = false) String endDateTo,
            @RequestParam(required = false) Double amountMin,
            @RequestParam(required = false) Double amountMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String creatorName,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 构建参数Map
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("type", type);
        params.put("status", status);
        params.put("counterparty", counterparty);
        params.put("contractNo", contractNo);
        params.put("startDateFrom", startDateFrom);
        params.put("startDateTo", startDateTo);
        params.put("endDateFrom", endDateFrom);
        params.put("endDateTo", endDateTo);
        params.put("amountMin", amountMin);
        params.put("amountMax", amountMax);
        params.put("keyword", keyword);
        params.put("folderId", folderId);
        params.put("sortBy", sortBy);
        params.put("sortOrder", sortOrder);
        
        // 调用Service层查询
        com.baomidou.mybatisplus.core.metadata.IPage<Contract> resultPage = contractService.listContracts(params, page, pageSize);
        
        // 转换为需要的格式并补充关联数据
        List<Map<String, Object>> pageList = new ArrayList<>();
        for (Contract contract : resultPage.getRecords()) {
            pageList.add(contractService.buildContractListResponse(contract));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", (int) resultPage.getTotal());
        result.put("page", (int) resultPage.getCurrent());
        result.put("pageSize", (int) resultPage.getSize());
        return ApiResponse.success(result);
    }
    

    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }
        
        Map<String, Object> result = contractService.buildContractDetailResponse(contract);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/payload")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getPayload(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractService.buildContractPayloadResponse(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/snapshots")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> listSnapshots(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractService.listContractSnapshots(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/snapshots/{snapshotId}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getSnapshot(@PathVariable Long id, @PathVariable Long snapshotId) {
        try {
            return ApiResponse.success(contractService.getContractSnapshot(id, snapshotId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/snapshots/compare")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> compareSnapshots(
            @PathVariable Long id,
            @RequestParam Long baseSnapshotId,
            @RequestParam Long targetSnapshotId) {
        try {
            return ApiResponse.success(contractService.compareContractSnapshots(id, baseSnapshotId, targetSnapshotId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/approval-summary")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getApprovalSummary(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractService.getApprovalSummary(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approval-summary/generate")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> generateApprovalSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            return ApiResponse.success(contractService.generateApprovalSummary(id, force));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/performance-milestones")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> listPerformanceMilestones(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }
        return ApiResponse.success(performanceMilestoneService.listAsMaps(id));
    }

    @PostMapping("/{id}/performance-milestones/extract")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> extractPerformanceMilestones(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }
        int n = performanceMilestoneService.extractFromPayload(id);
        Map<String, Object> data = new HashMap<>();
        data.put("inserted", n);
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}/related")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> related(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在", "error.contract.notFound");
        }
        Map<String, Object> detail = contractService.buildContractDetailResponse(contract);
        Object related = detail.get("relatedContracts");
        if (related instanceof Map<?, ?> relatedMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) relatedMap;
            return ApiResponse.success(casted);
        }
        return ApiResponse.success(new HashMap<>());
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "contractId", required = false) Long contractId) {
        try {
            return ApiResponse.success(contractFileService.upload(file, contractId));
        } catch (IllegalArgumentException e) {
            if ("文件不能为空".equals(e.getMessage())) {
                return ApiResponse.error("文件不能为空", "error.upload.fileRequired");
            }
            return ApiResponse.error(e.getMessage(), "error.upload.invalidName");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/attachments/{fileName}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> deleteAttachment(@PathVariable String fileName) {
        try {
            contractFileService.deleteAttachment(fileName);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "error.file.notFound");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage(), "error.file.deleteFailed");
        }
    }

    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        contractFileService.download(fileName, response);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> contractMap) {
        return ApiResponse.success(contractCommandService.create(contractMap));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> contractMap) {
        try {
            return ApiResponse.success(contractCommandService.update(id, contractMap));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            contractCommandService.delete(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, Object> request) {
        contractWorkflowService.batchDelete(request);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/batch-status")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = contractWorkflowService.batchUpdateStatus(request);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-approve")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> batchApprove(@RequestBody Map<String, Object> request) {
        try {
            return ApiResponse.success(contractWorkflowService.batchApprove(request));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-submit")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> batchSubmit(@RequestBody Map<String, Object> request) {
        try {
            return ApiResponse.success(contractWorkflowService.batchSubmit(request));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-edit")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> batchEdit(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = contractWorkflowService.batchEdit(request);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 提交审批
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractWorkflowService.submit(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 审批通过
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> data) {
        try {
            String comment = data != null ? (String) data.get("comment") : null;
            return ApiResponse.success(contractWorkflowService.approve(id, comment));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 审批拒绝
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> data) {
        try {
            String comment = data != null ? (String) data.get("comment") : null;
            return ApiResponse.success(contractWorkflowService.reject(id, comment));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 撤回审批
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> withdraw(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> data) {
        try {
            String reason = data != null ? (String) data.get("reason") : null;
            return ApiResponse.success(contractWorkflowService.withdraw(id, reason));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 签署
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> sign(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractWorkflowService.sign(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 归档
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> archive(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractWorkflowService.archive(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 终止
    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> terminate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            String reason = data != null ? (String) data.get("reason") : null;
            return ApiResponse.success(contractWorkflowService.terminate(id, reason));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/renewal/start")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> startRenewal(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            String reason = data != null ? (String) data.get("reason") : null;
            return ApiResponse.success(contractWorkflowService.startRenewal(id, reason));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/renewal/complete")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> completeRenewal(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            String reason = data != null ? (String) data.get("reason") : null;
            return ApiResponse.success(contractWorkflowService.completeRenewal(id, reason));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/renewal/decline")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> declineRenewal(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> data) {
        try {
            String reason = data != null ? (String) data.get("reason") : null;
            return ApiResponse.success(contractWorkflowService.declineRenewal(id, reason));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    

    
    // AI 分析 - 支持自定义 AI 配置
    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> analyze(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> aiConfig) {
        return contractAiGatewayService.analyze(id, aiConfig);
    }
    
    // 评论列表
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getComments(@PathVariable Long id) {
        return ApiResponse.success(contractCollaborationService.getComments(id));
    }
    
    // 审批历史
    @GetMapping("/{id}/approvals")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> getApprovalHistory(@PathVariable Long id) {
        return ApiResponse.success(contractCollaborationService.getApprovalHistory(id));
    }
    
    // 添加评论
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> addComment(@PathVariable Long id, @RequestBody Map<String, Object> commentData) {
        return contractCollaborationService.addComment(id, commentData);
    }
    
    // 删除评论
    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        return contractCollaborationService.deleteComment(commentId);
    }
    
    // 导出
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public void export(HttpServletResponse response) {
        contractExportService.exportContracts(response);
    }
    
    // 下载PDF
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void downloadPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        contractExportService.downloadPdf(id, response);
    }
    
    // 下载Word
    @GetMapping("/{id}/word")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void downloadWord(@PathVariable Long id, HttpServletResponse response) throws IOException {
        contractExportService.downloadWord(id, response);
    }
    
    // 导出Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public void exportExcel(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String endDateFrom,
            @RequestParam(required = false) String endDateTo,
            @RequestParam(required = false) Double amountMin,
            @RequestParam(required = false) Double amountMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            HttpServletResponse response) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("type", type);
        params.put("status", status);
        params.put("counterparty", counterparty);
        params.put("contractNo", contractNo);
        params.put("startDateFrom", startDateFrom);
        params.put("startDateTo", startDateTo);
        params.put("endDateFrom", endDateFrom);
        params.put("endDateTo", endDateTo);
        params.put("amountMin", amountMin);
        params.put("amountMax", amountMax);
        params.put("keyword", keyword);
        params.put("folderId", folderId);
        params.put("sortBy", sortBy);
        params.put("sortOrder", sortOrder);
        contractExportService.exportExcel(params, response);
    }
    
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> copyContract(@PathVariable Long id) {
        try {
            return ApiResponse.success(contractCommandService.copy(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/generate-pdf")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void generatePdf(@RequestBody Map<String, Object> data, HttpServletResponse response) throws IOException {
        contractExportService.generatePdf(data, response);
    }

}
