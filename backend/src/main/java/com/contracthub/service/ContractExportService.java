package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.mapper.ContractMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ContractExportService {
    private final ContractMapper contractMapper;
    private final ContractService contractService;
    private final ContractFieldValueMapper fieldValueMapper;
    private final WordExportService wordExportService;
    private final ExcelExportService excelExportService;
    private final ContractDataScopeService contractDataScopeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractExportService(
            ContractMapper contractMapper,
            ContractService contractService,
            ContractFieldValueMapper fieldValueMapper,
            WordExportService wordExportService,
            ExcelExportService excelExportService,
            ContractDataScopeService contractDataScopeService) {
        this.contractMapper = contractMapper;
        this.contractService = contractService;
        this.fieldValueMapper = fieldValueMapper;
        this.wordExportService = wordExportService;
        this.excelExportService = excelExportService;
        this.contractDataScopeService = contractDataScopeService;
    }

    public void exportContracts(HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("合同数据");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"合同编号", "合同名称", "合同类型", "交易对方", "合同金额", "开始日期", "结束日期", "状态", "创建人", "创建时间", "更新时间"};

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

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            List<Contract> contractList = getVisibleContracts();
            for (int i = 0; i < contractList.size(); i++) {
                Contract contract = contractList.get(i);
                Row dataRow = sheet.createRow(i + 1);
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                dataRow.createCell(0).setCellValue(defaultString(contract.getContractNo()));
                dataRow.createCell(1).setCellValue(defaultString(contract.getTitle()));
                dataRow.createCell(2).setCellValue(getContractTypeName(contract.getType()));
                dataRow.createCell(3).setCellValue(resolveCounterpartySummary(contract));
                dataRow.createCell(4).setCellValue(contract.getAmount() != null ? contract.getAmount().doubleValue() : 0);
                dataRow.createCell(5).setCellValue(contract.getStartDate() != null ? contract.getStartDate().toString() : "");
                dataRow.createCell(6).setCellValue(contract.getEndDate() != null ? contract.getEndDate().toString() : "");
                dataRow.createCell(7).setCellValue(getContractStatusName(contract.getStatus()));
                dataRow.createCell(8).setCellValue("admin");
                dataRow.createCell(9).setCellValue(contract.getCreateTime() != null ? contract.getCreateTime().toString() : "");
                dataRow.createCell(10).setCellValue(contract.getUpdateTime() != null ? contract.getUpdateTime().toString() : "");

                for (int j = 0; j < headers.length; j++) {
                    dataRow.getCell(j).setCellStyle(dataStyle);
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=contracts.xlsx");
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            } finally {
                workbook.close();
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void downloadPdf(Long id, HttpServletResponse response) throws IOException {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=contract_" + contract.getContractNo() + ".pdf");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {
            Paragraph title = new Paragraph(contract.getTitle() != null ? contract.getTitle() : "合同")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            Table table = new Table(2);
            table.setWidth(UnitValue.createPercentValue(100));
            addTableCell(table, "合同编号", defaultString(contract.getContractNo()));
            addTableCell(table, "合同类型", getContractTypeName(contract.getType()));
            addTableCell(table, "交易对方", resolveCounterpartySummary(contract));
            addTableCell(table, "合同金额", contract.getAmount() != null ? contract.getAmount().toString() : "0");
            addTableCell(table, "开始日期", contract.getStartDate() != null ? contract.getStartDate().toString() : "");
            addTableCell(table, "结束日期", contract.getEndDate() != null ? contract.getEndDate().toString() : "");
            addTableCell(table, "状态", getContractStatusName(contract.getStatus()));
            addTableCell(table, "创建人", "admin");
            addTableCell(table, "创建时间", contract.getCreateTime() != null ? contract.getCreateTime().toString() : "");
            addTableCell(table, "更新时间", contract.getUpdateTime() != null ? contract.getUpdateTime().toString() : "");
            document.add(table);
            document.add(new Paragraph("\n"));

            if (contract.getContent() != null) {
                document.add(new Paragraph("合同内容").setFontSize(14).setBold());
                String content = contract.getContent().replaceAll("<[^>]*>", "");
                document.add(new Paragraph(content).setFontSize(12));
            }
        }
    }

    public void downloadWord(Long id, HttpServletResponse response) throws IOException {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            byte[] wordBytes = wordExportService.exportContractToWord(contract, getContractAdditionalData(id));
            String fileName = "合同_" + contract.getContractNo() + ".docx";
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentLength(wordBytes.length);
            response.getOutputStream().write(wordBytes);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void exportExcel(Map<String, Object> params, HttpServletResponse response) throws IOException {
        List<Contract> filteredContracts = new ArrayList<>();
        int currentPage = 1;
        int pageSize = 1000;
        while (true) {
            IPage<Contract> pageData = contractService.listContracts(params, currentPage, pageSize);
            if (pageData.getRecords().isEmpty()) {
                break;
            }
            filteredContracts.addAll(pageData.getRecords());
            if (pageData.getCurrent() >= pageData.getPages()) {
                break;
            }
            currentPage++;
        }

        List<String> exportFields = List.of("contractNo", "title", "type", "counterparty", "amount", "startDate", "endDate", "status");
        byte[] excelBytes = excelExportService.exportContractsToExcel(filteredContracts, exportFields);

        String fileName = "合同列表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
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

    private List<Contract> getVisibleContracts() {
        if (contractDataScopeService.canViewAllContracts()) {
            return contractMapper.selectList(null);
        }
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        contractDataScopeService.applyContractVisibilityFilter(wrapper);
        return contractMapper.selectList(wrapper);
    }

    private String resolveCounterpartySummary(Contract contract) {
        if (contract == null || contract.getCounterparties() == null || contract.getCounterparties().isBlank()) {
            return "";
        }
        try {
            List<Map<String, Object>> counterparties = objectMapper.readValue(
                    contract.getCounterparties(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            return counterparties.stream()
                    .map(item -> item.get("name"))
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(" / "));
        } catch (Exception e) {
            return "";
        }
    }

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

    private String getContractTypeName(String type) {
        if (type == null) return "其他";
        return switch (type.toUpperCase()) {
            case "PURCHASE" -> "采购合同";
            case "SALES" -> "销售合同";
            case "SERVICE" -> "服务合同";
            case "LEASE" -> "租赁合同";
            case "EMPLOYMENT" -> "雇佣合同";
            default -> "其他";
        };
    }

    private String getContractStatusName(String status) {
        if (status == null) return "其他";
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "PENDING" -> "待审批";
            case "APPROVED" -> "已审批";
            case "SIGNED" -> "已签署";
            case "RENEWING" -> "续签中";
            case "RENEWED" -> "已续签";
            case "NOT_RENEWED" -> "不续签";
            case "ARCHIVED" -> "已归档";
            case "TERMINATED" -> "已终止";
            default -> "其他";
        };
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }
}
