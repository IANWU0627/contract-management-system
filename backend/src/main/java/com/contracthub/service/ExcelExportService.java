package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractFieldValue;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.mapper.ContractFieldValueMapper;
import com.contracthub.mapper.ContractTypeFieldMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelExportService {

    private final ContractTypeFieldMapper typeFieldMapper;
    private final ContractFieldValueMapper fieldValueMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ExcelExportService(ContractTypeFieldMapper typeFieldMapper, ContractFieldValueMapper fieldValueMapper) {
        this.typeFieldMapper = typeFieldMapper;
        this.fieldValueMapper = fieldValueMapper;
    }
    
    public byte[] exportContractsToExcel(List<Contract> contracts, List<String> exportFields) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("合同列表");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            List<String> headers = getHeaders(exportFields);
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            Set<String> allTypes = new HashSet<>();
            for (Contract contract : contracts) {
                if (contract.getType() != null) {
                    allTypes.add(contract.getType());
                }
            }
            
            Map<String, List<ContractTypeField>> fieldsByType = new HashMap<>();
            for (String type : allTypes) {
                fieldsByType.put(type, typeFieldMapper.selectByContractType(type));
            }
            
            Map<Long, Map<String, Object>> fieldValuesMap = new HashMap<>();
            Map<Long, List<ContractTypeField>> contractFieldsMap = new HashMap<>();
            for (Contract contract : contracts) {
                if (contract.getId() != null) {
                    List<ContractFieldValue> values = fieldValueMapper.selectByContractId(contract.getId());
                    Map<String, Object> valueMap = new HashMap<>();
                    for (ContractFieldValue fv : values) {
                        valueMap.put(fv.getFieldKey(), fv.getFieldValue());
                    }
                    fieldValuesMap.put(contract.getId(), valueMap);
                    
                    if (contract.getType() != null) {
                        contractFieldsMap.put(contract.getId(), fieldsByType.getOrDefault(contract.getType(), new ArrayList<>()));
                    }
                }
            }
            
            for (int rowNum = 0; rowNum < contracts.size(); rowNum++) {
                Contract contract = contracts.get(rowNum);
                Row row = sheet.createRow(rowNum + 1);
                
                int col = 0;
                Map<String, Object> dynamicValues = fieldValuesMap.get(contract.getId());
                
                for (String field : exportFields) {
                    Cell cell = row.createCell(col);
                    cell.setCellStyle(dataStyle);
                    
                    switch (field) {
                        case "contractNo" -> cell.setCellValue(contract.getContractNo() != null ? contract.getContractNo() : "");
                        case "title" -> cell.setCellValue(contract.getTitle() != null ? contract.getTitle() : "");
                        case "type" -> cell.setCellValue(formatType(contract.getType()));
                        case "counterparty" -> cell.setCellValue(resolveCounterpartySummary(contract));
                        case "amount" -> cell.setCellValue(contract.getAmount() != null ? contract.getAmount().doubleValue() : 0);
                        case "startDate" -> cell.setCellValue(contract.getStartDate() != null ? contract.getStartDate().format(dateFormatter) : "");
                        case "endDate" -> cell.setCellValue(contract.getEndDate() != null ? contract.getEndDate().format(dateFormatter) : "");
                        case "status" -> cell.setCellValue(formatStatus(contract.getStatus()));
                        case "remark" -> cell.setCellValue(contract.getRemark() != null ? contract.getRemark() : "");
                        case "createdAt" -> cell.setCellValue(contract.getCreateTime() != null ? contract.getCreateTime().toString() : "");
                        default -> {
                            if (dynamicValues != null && dynamicValues.containsKey(field)) {
                                cell.setCellValue(dynamicValues.get(field) != null ? dynamicValues.get(field).toString() : "");
                            } else {
                                cell.setCellValue("");
                            }
                        }
                    }
                    col++;
                }
            }
            
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private List<String> getHeaders(List<String> exportFields) {
        List<String> headers = new ArrayList<>();
        for (String field : exportFields) {
            switch (field) {
                case "contractNo" -> headers.add("合同编号");
                case "title" -> headers.add("合同名称");
                case "type" -> headers.add("合同类型");
                case "counterparty" -> headers.add("相对方");
                case "amount" -> headers.add("金额");
                case "startDate" -> headers.add("开始日期");
                case "endDate" -> headers.add("结束日期");
                case "status" -> headers.add("状态");
                case "remark" -> headers.add("备注");
                case "createdAt" -> headers.add("创建时间");
                default -> headers.add(field);
            }
        }
        return headers;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private String formatType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "PURCHASE" -> "采购合同";
            case "SALES" -> "销售合同";
            case "SERVICE" -> "服务合同";
            case "LEASE" -> "租赁合同";
            case "EMPLOYMENT" -> "劳动合同";
            default -> type;
        };
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "PENDING" -> "待审批";
            case "APPROVING" -> "审批中";
            case "APPROVED" -> "已通过";
            case "SIGNED" -> "已签署";
            case "RENEWING" -> "续签中";
            case "RENEWED" -> "已续签";
            case "NOT_RENEWED" -> "不续签";
            case "ARCHIVED" -> "已归档";
            case "TERMINATED" -> "已终止";
            default -> status;
        };
    }

    private String resolveCounterpartySummary(Contract contract) {
        if (contract == null || contract.getCounterparties() == null || contract.getCounterparties().isBlank()) {
            return "";
        }
        try {
            List<Map<String, Object>> counterparties = objectMapper.readValue(
                    contract.getCounterparties(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
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
}
