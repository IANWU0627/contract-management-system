package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractComment;
import com.contracthub.entity.ApprovalRecord;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractCommentMapper;
import com.contracthub.mapper.ApprovalRecordMapper;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.service.WordExportService;
import com.contracthub.service.ExcelExportService;
import com.contracthub.service.ContractNumberService;
import com.contracthub.service.ContractService;
import com.contracthub.service.NotificationService;
import com.contracthub.service.UserConfigService;
import com.contracthub.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    
    private final ContractMapper contractMapper;
    private final ContractCommentMapper commentMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final ContractFieldValueMapper fieldValueMapper;
    private final WordExportService wordExportService;
    private final ExcelExportService excelExportService;
    private final ContractNumberService contractNumberService;
    private final ContractService contractService;
    private final NotificationService notificationService;
    private final UserConfigService userConfigService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ContractController(ContractMapper contractMapper, ContractCommentMapper commentMapper, 
                              ApprovalRecordMapper approvalRecordMapper, ContractFieldValueMapper fieldValueMapper,
                              WordExportService wordExportService, ExcelExportService excelExportService,
                              ContractNumberService contractNumberService,
                              ContractService contractService,
                              NotificationService notificationService,
                              UserConfigService userConfigService,
                              RestTemplate restTemplate) {
        this.contractMapper = contractMapper;
        this.commentMapper = commentMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.fieldValueMapper = fieldValueMapper;
        this.wordExportService = wordExportService;
        this.excelExportService = excelExportService;
        this.contractNumberService = contractNumberService;
        this.contractService = contractService;
        this.notificationService = notificationService;
        this.userConfigService = userConfigService;
        this.restTemplate = restTemplate;
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
            pageList.add(contractService.buildContractResponse(contract));
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
            return ApiResponse.error("合同不存在");
        }
        
        Map<String, Object> result = contractService.buildContractDetailResponse(contract);
        return ApiResponse.success(result);
    }
    
    private static final String UPLOAD_DIR;
    
    static {
        UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "contractId", required = false) Long contractId) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return ApiResponse.error("文件名无效");
        }
        
        // 不再限制文件类型，允许所有格式上传
        
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFileName;
        
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File dest = new File(dir, uniqueFileName);
            file.transferTo(dest);
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
        
        result.put("fileName", uniqueFileName);
        result.put("originalName", originalFileName);
        result.put("fileUrl", "/api/contracts/download/" + uniqueFileName);
        result.put("fileSize", file.getSize());
        result.put("uploadTime", LocalDate.now().toString());
        if (contractId != null) {
            result.put("contractId", contractId);
        }
        return ApiResponse.success(result);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        if (fileName.contains("\0")) {
            return false;
        }
        return true;
    }
    
    private File getSecureFile(String fileName) {
        if (!isValidFileName(fileName)) {
            return null;
        }
        
        File uploadDir = new File(UPLOAD_DIR);
        File requestedFile = new File(uploadDir, fileName);
        
        try {
            String canonicalDir = uploadDir.getCanonicalPath();
            String canonicalFile = requestedFile.getCanonicalPath();
            
            if (!canonicalFile.startsWith(canonicalDir + File.separator)) {
                return null;
            }
            
            return requestedFile;
        } catch (Exception e) {
            return null;
        }
    }
    
    @DeleteMapping("/attachments/{fileName}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> deleteAttachment(@PathVariable String fileName) {
        File file = getSecureFile(fileName);
        
        if (file == null || !file.exists()) {
            return ApiResponse.error("文件不存在");
        }
        
        if (!file.delete()) {
            return ApiResponse.error("文件删除失败");
        }
        
        return ApiResponse.success(null);
    }
    
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }

    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        File file = getSecureFile(fileName);
        
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        try {
            String contentType = getContentType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            response.setContentLength((int) file.length());
            
            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> contractMap) {
        // 调用Service层创建合同
        Contract contract = contractService.createContract(contractMap);
        
        // 构建响应
        Map<String, Object> result = contractService.buildContractResponse(contract);
        
        // 添加相对方列表（从contractMap获取）
        if (contractMap.containsKey("counterparties")) {
            result.put("counterparties", contractMap.get("counterparties"));
        } else {
            result.put("counterparties", new ArrayList<>());
        }
        
        // 添加时区
        if (contractMap.containsKey("timezone")) {
            result.put("timezone", contractMap.get("timezone"));
        } else {
            result.put("timezone", java.time.ZoneId.systemDefault().getId());
        }
        
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> contractMap) {
        try {
            // 调用Service层更新合同
            Contract contract = contractService.updateContract(id, contractMap);
            
            // 构建响应
            Map<String, Object> result = contractService.buildContractResponse(contract);
            
            // 添加相对方列表（从contractMap获取）
            if (contractMap.containsKey("counterparties")) {
                result.put("counterparties", contractMap.get("counterparties"));
            } else if (contract.getCounterparties() != null && !contract.getCounterparties().isEmpty()) {
                try {
                    List<Map<String, Object>> cpList = objectMapper.readValue(contract.getCounterparties(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                    result.put("counterparties", cpList);
                } catch (Exception e) {
                    result.put("counterparties", new ArrayList<>());
                }
            } else {
                result.put("counterparties", new ArrayList<>());
            }
            
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, Object> request) {
        List<Object> idsObj = (List<Object>) request.get("ids");
        if (idsObj != null) {
            List<Long> ids = idsObj.stream()
                .filter(id -> id instanceof Number)
                .map(id -> ((Number) id).longValue())
                .collect(java.util.stream.Collectors.toList());
            contractService.batchDeleteContracts(ids);
        }
        return ApiResponse.success(null);
    }
    
    @PostMapping("/batch-status")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = contractService.batchUpdateStatus(request);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/batch-approve")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> batchApprove(@RequestBody Map<String, Object> request) {
        request.put("status", "APPROVED");
        return batchUpdateStatus(request);
    }
    
    @PostMapping("/batch-submit")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> batchSubmit(@RequestBody Map<String, Object> request) {
        request.put("status", "PENDING");
        return batchUpdateStatus(request);
    }
    
    @PostMapping("/batch-edit")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> batchEdit(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = contractService.batchEditContracts(request);
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
            Contract contract = contractService.submitForApproval(id);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
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
            Contract contract = contractService.approveContract(id, comment);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
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
            Contract contract = contractService.rejectContract(id, comment);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 签署
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> sign(@PathVariable Long id) {
        try {
            Contract contract = contractService.signContract(id);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 归档
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> archive(@PathVariable Long id) {
        try {
            Contract contract = contractService.archiveContract(id);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 终止
    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('CONTRACT_APPROVE')")
    public ApiResponse<Map<String, Object>> terminate(@PathVariable Long id) {
        try {
            Contract contract = contractService.terminateContract(id);
            Map<String, Object> result = contractService.buildContractResponse(contract);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    

    
    // AI 分析 - 支持自定义 AI 配置
    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> analyze(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> aiConfig) {
        
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在");
        }
        
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, String> configs = userConfigService.getUserConfigValues(userId);
        String apiUrl = getConfigValue(aiConfig, "apiUrl", userConfigService.getStringConfig(configs, "ai_api_url", ""));
        String apiKey = getConfigValue(aiConfig, "apiKey", userConfigService.getStringConfig(configs, "ai_api_key", ""));
        String model = getConfigValue(aiConfig, "model", userConfigService.getStringConfig(configs, "ai_model", "gpt-3.5-turbo"));
        double temperature = getDoubleConfigValue(aiConfig, "temperature", userConfigService.getDoubleConfig(configs, "bd_temperature", 0.7));
        int maxTokens = getIntConfigValue(aiConfig, "maxTokens", userConfigService.getIntConfig(configs, "bd_max_tokens", 1000));

        // 如果有 AI 配置，调用 AI 服务
        if (!apiUrl.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (apiKey != null && !apiKey.isEmpty()) {
                    headers.set("Authorization", "Bearer " + apiKey);
                }
                
                String prompt = buildAnalysisPrompt(contract);
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", "You are a professional contract analyst. Analyze contracts and provide structured feedback in JSON format."),
                    Map.of("role", "user", "content", prompt)
                ));
                requestBody.put("temperature", temperature);
                requestBody.put("max_tokens", maxTokens);
                
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    String responseBody = response.getBody();
                    Map<String, Object> parsed = objectMapper.readValue(responseBody, Map.class);
                    
                    List<?> choices = (List<?>) parsed.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                        Map<?, ?> message = (Map<?, ?>) choice.get("message");
                        String aiContent = (String) message.get("content");
                        
                        return ApiResponse.success(parseAiResponse(aiContent));
                    }
                }
            } catch (Exception e) {
                return ApiResponse.error("AI 分析失败: " + e.getMessage());
            }
        }
        
        // 默认返回模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("summary", "本合同为标准的采购合同，主要条款清晰。合同期限为12个月，金额合理。建议关注付款条款和违约责任。");
        result.put("risks", Arrays.asList(
            "违约金比例偏高（合同金额的30%），建议协商降低至15%",
            "付款周期较长（90天），建议缩短至60天",
            "缺少不可抗力条款，建议补充",
            "知识产权归属条款不够明确"
        ));
        result.put("score", 78);
        result.put("suggestions", Arrays.asList("建议在签署前与法务团队确认风险条款，特别是违约金和付款周期部分。"));
        return ApiResponse.success(result);
    }
    
    private String buildAnalysisPrompt(Contract contract) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下合同并提供结构化的风险评估报告：\n\n");
        sb.append("合同名称：").append(contract.getTitle()).append("\n");
        sb.append("合同编号：").append(contract.getContractNo()).append("\n");
        sb.append("合同类型：").append(contract.getType()).append("\n");
        sb.append("相对方：").append(contract.getCounterparty()).append("\n");
        sb.append("金额：").append(contract.getAmount()).append("\n");
        sb.append("开始日期：").append(contract.getStartDate()).append("\n");
        sb.append("结束日期：").append(contract.getEndDate()).append("\n\n");
        
        if (contract.getContent() != null && !contract.getContent().isEmpty()) {
            sb.append("合同内容：\n").append(contract.getContent().substring(0, Math.min(2000, contract.getContent().length())));
        }
        
        sb.append("\n\n请按照以下JSON格式返回分析结果（只返回JSON，不要其他内容）：\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"合同概述（50字以内）\",\n");
        sb.append("  \"score\": 风险评分（0-100的整数）,\n");
        sb.append("  \"risks\": [\"风险点1\", \"风险点2\", \"风险点3\"],\n");
        sb.append("  \"suggestions\": [\"建议1\", \"建议2\"]\n");
        sb.append("}");
        
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAiResponse(String content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String jsonStr = extractJson(content);
            if (jsonStr != null) {
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                result.put("summary", parsed.getOrDefault("summary", "分析完成"));
                result.put("score", parsed.getOrDefault("score", 75));
                result.put("risks", parsed.getOrDefault("risks", Arrays.asList("未发现明显风险")));
                result.put("suggestions", parsed.getOrDefault("suggestions", Arrays.asList("建议仔细阅读合同条款")));
                return result;
            }
        } catch (Exception e) {
            // 解析失败
        }
        
        result.put("summary", "AI 分析完成");
        result.put("score", 75);
        result.put("risks", Arrays.asList("请人工审核合同条款"));
        result.put("suggestions", Arrays.asList("建议与法务团队确认关键条款"));
        return result;
    }
    
    private String extractJson(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return null;
    }

    private String getConfigValue(Map<String, Object> configMap, String key, String defaultValue) {
        if (configMap == null) {
            return defaultValue;
        }
        Object value = configMap.get(key);
        if (value == null) {
            return defaultValue;
        }
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? defaultValue : result;
    }

    private double getDoubleConfigValue(Map<String, Object> configMap, String key, double defaultValue) {
        if (configMap == null || configMap.get(key) == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(configMap.get(key)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int getIntConfigValue(Map<String, Object> configMap, String key, int defaultValue) {
        if (configMap == null || configMap.get(key) == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(configMap.get(key)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // 评论列表
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> getComments(@PathVariable Long id) {
        List<ContractComment> commentList = commentMapper.selectByContractId(id);
        List<Map<String, Object>> comments = new ArrayList<>();
        
        for (ContractComment comment : commentList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("userId", comment.getUserId());
            map.put("userName", comment.getUsername());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("createdAt", comment.getCreatedAt());
            comments.add(map);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", comments);
        result.put("total", comments.size());
        return ApiResponse.success(result);
    }
    
    // 审批历史
    @GetMapping("/{id}/approvals")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<List<Map<String, Object>>> getApprovalHistory(@PathVariable Long id) {
        List<ApprovalRecord> records = approvalRecordMapper.selectList(
            new QueryWrapper<ApprovalRecord>().eq("contract_id", id).orderByAsc("create_time")
        );
        
        List<Map<String, Object>> history = new ArrayList<>();
        for (ApprovalRecord record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("approverId", record.getApproverId());
            map.put("approverName", record.getApproverName());
            map.put("action", record.getStatus());
            map.put("comment", record.getComment());
            map.put("timestamp", record.getCreateTime());
            history.add(map);
        }
        
        return ApiResponse.success(history);
    }
    
    // 添加评论
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> addComment(@PathVariable Long id, @RequestBody Map<String, Object> commentData) {
        Contract contract = contractMapper.selectById(id);
        ContractComment comment = new ContractComment();
        comment.setContractId(id);
        comment.setUserId(SecurityUtils.getCurrentUserId());
        comment.setUsername(SecurityUtils.getCurrentUserName());
        comment.setContent((String) commentData.get("content"));
        comment.setParentId(commentData.containsKey("parentId") ? (Long) commentData.get("parentId") : null);
        comment.setCreatedAt(LocalDateTime.now());
        
        commentMapper.insert(comment);

        if (contract != null) {
            Set<Long> recipients = new LinkedHashSet<>();
            Long currentUserId = comment.getUserId();

            if (contract.getCreatorId() != null && !contract.getCreatorId().equals(currentUserId)) {
                recipients.add(contract.getCreatorId());
            }

            if (comment.getParentId() != null) {
                ContractComment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null && parentComment.getUserId() != null && !parentComment.getUserId().equals(currentUserId)) {
                    recipients.add(parentComment.getUserId());
                }
            }

            for (Long recipientId : recipients) {
                notificationService.sendCommentNotification(
                        recipientId,
                        id,
                        contract.getContractNo(),
                        comment.getUsername(),
                        comment.getContent()
                );
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("userId", comment.getUserId());
        result.put("userName", comment.getUsername());
        result.put("content", comment.getContent());
        result.put("parentId", comment.getParentId());
        result.put("createdAt", comment.getCreatedAt());
        result.put("userAvatar", "");
        
        return ApiResponse.success(result);
    }
    
    // 删除评论
    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Void> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        int result = commentMapper.deleteById(commentId);
        if (result == 0) {
            return ApiResponse.error("评论不存在");
        }
        return ApiResponse.success(null);
    }
    
    private Map<String, Object> createComment(Long id, String username, String nickname, String content, String time) {
        Map<String, Object> c = new HashMap<>();
        c.put("id", id);
        c.put("username", username);
        c.put("userName", nickname);
        c.put("content", content);
        c.put("createdAt", time);
        return c;
    }
    
    private Map<String, Object> createVersion(Long id, String version, String operator, String description, String time) {
        Map<String, Object> v = new HashMap<>();
        v.put("id", id);
        v.put("version", version);
        v.put("operatorName", operator);
        v.put("changeDescription", description);
        v.put("createdAt", time);
        return v;
    }
    
    // 导出
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public void export(HttpServletResponse response) {
        try {
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("合同数据");
            
            // 设置表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "合同编号", "合同名称", "合同类型", "交易对方", "合同金额", 
                "开始日期", "结束日期", "状态", "创建人", "创建时间", "更新时间"
            };
            
            // 创建表头单元格样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            
            // 填充表头
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // 从数据库获取合同数据
            List<Contract> contractList = contractMapper.selectList(null);
            
            // 填充数据
            for (int i = 0; i < contractList.size(); i++) {
                Contract contract = contractList.get(i);
                Row dataRow = sheet.createRow(i + 1);
                
                // 创建数据单元格样式
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                
                // 填充数据
                org.apache.poi.ss.usermodel.Cell cell0 = dataRow.createCell(0);
                cell0.setCellValue(contract.getContractNo() != null ? contract.getContractNo() : "");
                
                org.apache.poi.ss.usermodel.Cell cell1 = dataRow.createCell(1);
                cell1.setCellValue(contract.getTitle() != null ? contract.getTitle() : "");
                
                // 合同类型转换
                String type = contract.getType();
                String typeName = "其他";
                if (type != null) {
                    switch (type.toUpperCase()) {
                        case "PURCHASE": typeName = "采购合同"; break;
                        case "SALES": typeName = "销售合同"; break;
                        case "SERVICE": typeName = "服务合同"; break;
                        case "LEASE": typeName = "租赁合同"; break;
                        case "EMPLOYMENT": typeName = "雇佣合同"; break;
                        case "OTHER": typeName = "其他"; break;
                    }
                }
                org.apache.poi.ss.usermodel.Cell cell2 = dataRow.createCell(2);
                cell2.setCellValue(typeName);
                
                org.apache.poi.ss.usermodel.Cell cell3 = dataRow.createCell(3);
                cell3.setCellValue(contract.getCounterparty() != null ? contract.getCounterparty() : "");
                
                // 合同金额
                org.apache.poi.ss.usermodel.Cell cell4 = dataRow.createCell(4);
                if (contract.getAmount() != null) {
                    cell4.setCellValue(contract.getAmount().doubleValue());
                } else {
                    cell4.setCellValue(0);
                }
                
                org.apache.poi.ss.usermodel.Cell cell5 = dataRow.createCell(5);
                cell5.setCellValue(contract.getStartDate() != null ? contract.getStartDate().toString() : "");
                
                org.apache.poi.ss.usermodel.Cell cell6 = dataRow.createCell(6);
                cell6.setCellValue(contract.getEndDate() != null ? contract.getEndDate().toString() : "");
                
                // 状态转换
                String status = contract.getStatus();
                String statusName = "其他";
                if (status != null) {
                    switch (status) {
                        case "DRAFT": statusName = "草稿"; break;
                        case "PENDING": statusName = "待审批"; break;
                        case "APPROVED": statusName = "已审批"; break;
                        case "SIGNED": statusName = "已签署"; break;
                        case "ARCHIVED": statusName = "已归档"; break;
                        case "TERMINATED": statusName = "已终止"; break;
                    }
                }
                org.apache.poi.ss.usermodel.Cell cell7 = dataRow.createCell(7);
                cell7.setCellValue(statusName);
                
                org.apache.poi.ss.usermodel.Cell cell8 = dataRow.createCell(8);
                cell8.setCellValue("admin"); // 简化处理
                
                org.apache.poi.ss.usermodel.Cell cell9 = dataRow.createCell(9);
                cell9.setCellValue(contract.getCreateTime() != null ? contract.getCreateTime().toString() : "");
                
                org.apache.poi.ss.usermodel.Cell cell10 = dataRow.createCell(10);
                cell10.setCellValue(contract.getUpdateTime() != null ? contract.getUpdateTime().toString() : "");
                
                // 应用样式
                for (int j = 0; j < headers.length; j++) {
                    dataRow.getCell(j).setCellStyle(dataStyle);
                }
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=contracts.xlsx");
            
            // 写入响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            } finally {
                workbook.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    // 下载PDF
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void downloadPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        // 从数据库查找合同
        Contract contract = contractMapper.selectById(id);
        
        if (contract == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // 设置响应头
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=contract_" + contract.getContractNo() + ".pdf");
        
        // 创建PDF
        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {
            
            // 添加标题
            Paragraph title = new Paragraph(contract.getTitle() != null ? contract.getTitle() : "合同").setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            document.add(new Paragraph("\n"));
            
            // 添加合同信息表格
            Table table = new Table(2);
            table.setWidth(UnitValue.createPercentValue(100));
            
            // 表格数据
            addTableCell(table, "合同编号", contract.getContractNo() != null ? contract.getContractNo() : "");
            addTableCell(table, "合同类型", getContractTypeName(contract.getType()));
            addTableCell(table, "交易对方", contract.getCounterparty() != null ? contract.getCounterparty() : "");
            addTableCell(table, "合同金额", contract.getAmount() != null ? contract.getAmount().toString() : "0");
            addTableCell(table, "开始日期", contract.getStartDate() != null ? contract.getStartDate().toString() : "");
            addTableCell(table, "结束日期", contract.getEndDate() != null ? contract.getEndDate().toString() : "");
            addTableCell(table, "状态", getContractStatusName(contract.getStatus()));
            addTableCell(table, "创建人", "admin"); // 简化处理
            addTableCell(table, "创建时间", contract.getCreateTime() != null ? contract.getCreateTime().toString() : "");
            addTableCell(table, "更新时间", contract.getUpdateTime() != null ? contract.getUpdateTime().toString() : "");
            
            document.add(table);
            
            document.add(new Paragraph("\n"));
            
            // 添加合同内容
            if (contract.getContent() != null) {
                Paragraph contentTitle = new Paragraph("合同内容").setFontSize(14).setBold();
                document.add(contentTitle);
                
                // 简单处理HTML内容，实际项目中可能需要更复杂的处理
                String content = contract.getContent();
                content = content.replaceAll("<[^>]*>", ""); // 移除HTML标签
                Paragraph contentPara = new Paragraph(content).setFontSize(12);
                document.add(contentPara);
            }
        }
    }
    
    // 辅助方法：添加表格单元格
    private void addTableCell(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label)).setBold();
        labelCell.setTextAlignment(TextAlignment.RIGHT);
        labelCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(labelCell);
        
        Cell valueCell = new Cell().add(new Paragraph(value));
        valueCell.setTextAlignment(TextAlignment.LEFT);
        valueCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(valueCell);
    }
    
    // 辅助方法：获取合同类型名称
    private String getContractTypeName(String type) {
        if (type == null) return "其他";
        switch (type.toUpperCase()) {
            case "PURCHASE": return "采购合同";
            case "SALES": return "销售合同";
            case "SERVICE": return "服务合同";
            case "LEASE": return "租赁合同";
            case "EMPLOYMENT": return "雇佣合同";
            case "OTHER": return "其他";
            default: return "其他";
        }
    }
    
    // 辅助方法：获取合同状态名称
    private String getContractStatusName(String status) {
        if (status == null) return "其他";
        switch (status) {
            case "DRAFT": return "草稿";
            case "PENDING": return "待审批";
            case "APPROVED": return "已审批";
            case "SIGNED": return "已签署";
            case "ARCHIVED": return "已归档";
            case "TERMINATED": return "已终止";
            default: return "其他";
        }
    }
    
    // 下载Word
    @GetMapping("/{id}/word")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public void downloadWord(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Contract contract = contractMapper.selectById(id);
        
        if (contract == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        try {
            byte[] wordBytes = wordExportService.exportContractToWord(contract, getContractAdditionalData(id));
            
            String fileName = "合同_" + contract.getContractNo() + ".docx";
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLength(wordBytes.length);
            response.getOutputStream().write(wordBytes);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private Map<String, Object> getContractAdditionalData(Long contractId) {
        Map<String, Object> data = new HashMap<>();
        
        Map<String, Object> dynamicFields = new HashMap<>();
        List<ContractFieldValue> fieldValues = fieldValueMapper.selectByContractId(contractId);
        for (ContractFieldValue fv : fieldValues) {
            dynamicFields.put(fv.getFieldKey(), fv.getFieldValue());
        }
        data.put("dynamicFields", dynamicFields);
        
        return data;
    }
    
    // 导出Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public void exportExcel(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counterparty,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) String endDateFrom,
            @RequestParam(required = false) String endDateTo,
            @RequestParam(required = false) Double amountMin,
            @RequestParam(required = false) Double amountMax,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) throws IOException {
        
        List<Contract> allContracts = contractMapper.selectList(null);
        List<Contract> filteredContracts = new ArrayList<>();
        
        for (Contract contract : allContracts) {
            if (title != null && !title.isEmpty() && 
                (contract.getTitle() == null || !contract.getTitle().toLowerCase().contains(title.toLowerCase()))) {
                continue;
            }
            if (type != null && !type.isEmpty() && !type.equals(contract.getType())) {
                continue;
            }
            if (status != null && !status.isEmpty() && !status.equals(contract.getStatus())) {
                continue;
            }
            if (counterparty != null && !counterparty.isEmpty() && 
                (contract.getCounterparty() == null || !contract.getCounterparty().toLowerCase().contains(counterparty.toLowerCase()))) {
                continue;
            }
            if (startDateFrom != null && !startDateFrom.isEmpty() && contract.getStartDate() != null && 
                contract.getStartDate().isBefore(LocalDate.parse(startDateFrom))) {
                continue;
            }
            if (startDateTo != null && !startDateTo.isEmpty() && contract.getStartDate() != null && 
                contract.getStartDate().isAfter(LocalDate.parse(startDateTo))) {
                continue;
            }
            if (endDateFrom != null && !endDateFrom.isEmpty() && contract.getEndDate() != null && 
                contract.getEndDate().isBefore(LocalDate.parse(endDateFrom))) {
                continue;
            }
            if (endDateTo != null && !endDateTo.isEmpty() && contract.getEndDate() != null && 
                contract.getEndDate().isAfter(LocalDate.parse(endDateTo))) {
                continue;
            }
            if (amountMin != null && contract.getAmount() != null && contract.getAmount().doubleValue() < amountMin) {
                continue;
            }
            if (amountMax != null && contract.getAmount() != null && contract.getAmount().doubleValue() > amountMax) {
                continue;
            }
            if (keyword != null && !keyword.isEmpty()) {
                boolean match = false;
                String kwLower = keyword.toLowerCase();
                if (contract.getTitle() != null && contract.getTitle().toLowerCase().contains(kwLower)) match = true;
                if (contract.getContractNo() != null && contract.getContractNo().toLowerCase().contains(kwLower)) match = true;
                if (contract.getCounterparty() != null && contract.getCounterparty().toLowerCase().contains(kwLower)) match = true;
                if (contract.getContent() != null && contract.getContent().toLowerCase().contains(kwLower)) match = true;
                if (contract.getRemark() != null && contract.getRemark().toLowerCase().contains(kwLower)) match = true;
                String typeLower = contract.getType() != null ? contract.getType().toLowerCase() : "";
                if (typeLower.contains(kwLower)) match = true;
                String statusLower = contract.getStatus() != null ? contract.getStatus().toLowerCase() : "";
                if (statusLower.contains(kwLower)) match = true;
                if (!match) continue;
            }
            
            filteredContracts.add(contract);
        }
        
        List<String> exportFields = List.of("contractNo", "title", "type", "counterparty", "amount", "startDate", "endDate", "status");
        
        byte[] excelBytes = excelExportService.exportContractsToExcel(filteredContracts, exportFields);
        
        String fileName = "合同列表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
    }
    
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAuthority('CONTRACT_MANAGE')")
    public ApiResponse<Map<String, Object>> copyContract(@PathVariable Long id) {
        try {
            Contract contract = contractService.copyContract(id);
            Map<String, Object> result = new HashMap<>();
            result.put("id", contract.getId());
            result.put("contractNo", contract.getContractNo());
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/generate-pdf")
    public void generatePdf(@RequestBody Map<String, Object> data, HttpServletResponse response) throws Exception {
        String content = (String) data.get("content");
        String contractNo = (String) data.getOrDefault("contractNo", "contract");
        String watermark = (String) data.getOrDefault("watermark", "");
        
        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + contractNo + ".pdf");
        
        if (content == null || content.isEmpty()) {
            content = "<p>No content</p>";
        }
        
        String watermarkHtml = "";
        if (watermark != null && !watermark.isEmpty()) {
            watermarkHtml = "<div style='position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); opacity: 0.15; font-size: 48px; font-weight: bold; color: #ccc; pointer-events: none; z-index: 9999;'>" + watermark + "</div>";
        }
        
        String fullHtml = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC&amp;display=swap'); " +
            "body { font-family: 'Noto Sans SC', 'Microsoft YaHei', 'SimSun', sans-serif; font-size: 14px; line-height: 1.8; padding: 40px; margin: 0; color: #333; } " +
            "h1 { font-size: 24px; text-align: center; color: #333; margin: 20px 0; } h2 { font-size: 18px; color: #333; margin: 16px 0; } h3 { font-size: 16px; color: #333; } " +
            "p { margin: 12px 0; text-indent: 2em; } table { border-collapse: collapse; width: 100%; margin: 16px 0; page-break-inside: avoid; } " +
            "th, td { border: 1px solid #333; padding: 10px; text-align: left; } " +
            "th { background-color: #f5f5f5; font-weight: bold; text-align: center; } " +
            "img { max-width: 100%; height: auto; } " +
            "ul, ol { margin: 12px 0; padding-left: 30px; } " +
            "li { margin: 6px 0; } " +
            "strong, b { font-weight: bold; } " +
            "em, i { font-style: italic; } " +
            "u { text-decoration: underline; } " +
            "s, strike { text-decoration: line-through; } " +
            ".uploaded-file-preview { text-align: center; margin: 20px 0; } " +
            ".watermark-fixed { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); opacity: 0.15; font-size: 48px; font-weight: bold; color: #ccc; pointer-events: none; z-index: 9999; } " +
            "</style>" +
            "</head>" +
            "<body>" +
            watermarkHtml +
            content +
            "</body>" +
            "</html>";
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.itextpdf.html2pdf.HtmlConverter.convertToPdf(fullHtml, baos);
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }
}
