package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractTypeField;
import com.contracthub.mapper.ContractTypeFieldMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WordExportService {

    private final ContractTypeFieldMapper typeFieldMapper;
    private final ObjectMapper objectMapper;
    
    public WordExportService(ContractTypeFieldMapper typeFieldMapper) {
        this.typeFieldMapper = typeFieldMapper;
        this.objectMapper = new ObjectMapper();
    }
    
    public byte[] exportContractToWord(Contract contract, Map<String, Object> additionalData) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            addTitle(document, contract.getTitle());
            addHorizontalLine(document);
            
            addSectionHeader(document, "基本信息");
            addInfoRow(document, "合同编号", contract.getContractNo());
            addInfoRow(document, "合同类型", formatContractType(contract.getType()));
            addInfoRow(document, "合同金额", formatAmount(contract.getAmount()));
            addInfoRow(document, "签订日期", formatDate(contract.getStartDate()));
            addInfoRow(document, "到期日期", formatDate(contract.getEndDate()));
            addInfoRow(document, "合同状态", formatStatus(contract.getStatus()));
            
            if (contract.getCounterparties() != null && !contract.getCounterparties().isEmpty()) {
                addSectionHeader(document, "相对方信息");
                List<Map<String, Object>> counterparties = parseCounterparties(contract.getCounterparties());
                for (Map<String, Object> cp : counterparties) {
                    String partyType = (String) cp.getOrDefault("type", "");
                    String partyName = (String) cp.getOrDefault("name", "");
                    addInfoRow(document, formatPartyType(partyType) + "名称", partyName);
                    
                    String contact = (String) cp.getOrDefault("contact", "");
                    if (!contact.isEmpty()) addInfoRow(document, "联系人", contact);
                    
                    String phone = (String) cp.getOrDefault("phone", "");
                    if (!phone.isEmpty()) addInfoRow(document, "联系电话", phone);
                    
                    String email = (String) cp.getOrDefault("email", "");
                    if (!email.isEmpty()) addInfoRow(document, "邮箱", email);
                }
            }
            
            if (additionalData != null && additionalData.containsKey("dynamicFields")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dynamicFields = (Map<String, Object>) additionalData.get("dynamicFields");
                if (dynamicFields != null && !dynamicFields.isEmpty()) {
                    addSectionHeader(document, "动态字段");
                    List<ContractTypeField> fieldDefs = typeFieldMapper.selectByContractType(contract.getType());
                    Map<String, ContractTypeField> fieldDefMap = new HashMap<>();
                    for (ContractTypeField field : fieldDefs) {
                        fieldDefMap.put(field.getFieldKey(), field);
                    }
                    
                    for (Map.Entry<String, Object> entry : dynamicFields.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (value != null && !String.valueOf(value).isEmpty()) {
                            ContractTypeField fieldDef = fieldDefMap.get(key);
                            String label = fieldDef != null ? fieldDef.getFieldLabel() : key;
                            addInfoRow(document, label, formatDynamicValue(value));
                        }
                    }
                }
            }
            
            if (contract.getContent() != null && !contract.getContent().isEmpty()) {
                addSectionHeader(document, "合同内容");
                addContent(document, contract.getContent());
            }
            
            if (contract.getRemark() != null && !contract.getRemark().isEmpty()) {
                addSectionHeader(document, "备注");
                addParagraph(document, contract.getRemark());
            }
            
            addFooter(document, contract);
            
            document.write(out);
            return out.toByteArray();
        }
    }
    
    private void addTitle(XWPFDocument document, String title) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.addBreak();
    }
    
    private void addHorizontalLine(XWPFDocument document) {
        XWPFParagraph linePara = document.createParagraph();
        linePara.setBorderBottom(Borders.SINGLE);
    }
    
    private void addSectionHeader(XWPFDocument document, String header) {
        XWPFParagraph sectionPara = document.createParagraph();
        sectionPara.setSpacingBefore(400);
        sectionPara.setSpacingAfter(200);
        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText(header);
        sectionRun.setBold(true);
        sectionRun.setFontSize(14);
    }
    
    private void addInfoRow(XWPFDocument document, String label, String value) {
        if (value == null || value.isEmpty()) return;
        
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBetween(50);
        
        XWPFRun labelRun = para.createRun();
        labelRun.setText(label + "：");
        labelRun.setBold(true);
        labelRun.setFontSize(11);
        
        XWPFRun valueRun = para.createRun();
        valueRun.setText(value);
        valueRun.setFontSize(11);
    }
    
    private void addContent(XWPFDocument document, String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            XWPFParagraph para = document.createParagraph();
            para.setSpacingBetween(100);
            XWPFRun run = para.createRun();
            run.setText(line);
            run.setFontSize(11);
        }
    }
    
    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBetween(100);
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontSize(11);
    }
    
    private void addFooter(XWPFDocument document, Contract contract) {
        XWPFParagraph footerPara = document.createParagraph();
        footerPara.setSpacingBefore(600);
        footerPara.setAlignment(ParagraphAlignment.RIGHT);
        
        XWPFRun footerRun = footerPara.createRun();
        footerRun.setText("导出时间：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        footerRun.setFontSize(10);
        footerRun.setColor("808080");
    }
    
    private List<Map<String, Object>> parseCounterparties(String json) {
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private String formatContractType(String type) {
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
            case "ARCHIVED" -> "已归档";
            case "TERMINATED" -> "已终止";
            default -> status;
        };
    }
    
    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
    }
    
    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "¥0.00";
        return "¥" + amount.toPlainString();
    }
    
    private String formatPartyType(String type) {
        if (type == null) return "相对方";
        return switch (type) {
            case "partyA" -> "甲方";
            case "partyB" -> "乙方";
            case "partyC" -> "丙方";
            case "partyD" -> "丁方";
            default -> "相对方";
        };
    }
    
    private String formatDynamicValue(Object value) {
        if (value == null) return "";
        return String.valueOf(value);
    }
}
