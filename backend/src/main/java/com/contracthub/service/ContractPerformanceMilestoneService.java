package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractPayload;
import com.contracthub.entity.ContractPerformanceMilestone;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractPayloadMapper;
import com.contracthub.mapper.ContractPerformanceMilestoneMapper;
import com.contracthub.util.PerformanceMilestonePatterns;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ContractPerformanceMilestoneService {

    private static final Set<String> SIGNED_OR_ACTIVE = Set.of(
        "SIGNED", "RENEWING", "RENEWED", "NOT_RENEWED", "ARCHIVED"
    );

    private final ContractPerformanceMilestoneMapper milestoneMapper;
    private final ContractMapper contractMapper;
    private final ContractPayloadMapper payloadMapper;
    private final NotificationService notificationService;

    public ContractPerformanceMilestoneService(
        ContractPerformanceMilestoneMapper milestoneMapper,
        ContractMapper contractMapper,
        ContractPayloadMapper payloadMapper,
        NotificationService notificationService
    ) {
        this.milestoneMapper = milestoneMapper;
        this.contractMapper = contractMapper;
        this.payloadMapper = payloadMapper;
        this.notificationService = notificationService;
    }

    public List<ContractPerformanceMilestone> listByContract(Long contractId) {
        return milestoneMapper.selectList(
            new QueryWrapper<ContractPerformanceMilestone>()
                .eq("contract_id", contractId)
                .orderByAsc("due_date", "id")
        );
    }

    /**
     * Parse contract payload content and replace EXTRACTED rows for this contract.
     */
    @Transactional
    public int extractFromPayload(Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return 0;
        }
        ContractPayload payload = payloadMapper.selectOne(
            new QueryWrapper<ContractPayload>().eq("contract_id", contractId)
        );
        if (payload == null || payload.getContent() == null || payload.getContent().isBlank()) {
            return 0;
        }
        milestoneMapper.delete(
            new QueryWrapper<ContractPerformanceMilestone>()
                .eq("contract_id", contractId)
                .eq("source", "EXTRACTED")
        );
        List<PerformanceMilestonePatterns.Extracted> found =
            PerformanceMilestonePatterns.extractAll(payload.getContent());
        LocalDate anchor = contract.getStartDate() != null
            ? contract.getStartDate()
            : LocalDate.now();
        int inserted = 0;
        for (PerformanceMilestonePatterns.Extracted ex : found) {
            LocalDate due = anchor.plusDays(ex.offsetDays);
            ContractPerformanceMilestone row = new ContractPerformanceMilestone();
            row.setContractId(contractId);
            row.setTitle(ex.title);
            row.setDueDate(due);
            row.setOffsetDays(ex.offsetDays);
            row.setAnchorNote(ex.anchorNote);
            row.setRawSnippet(ex.rawSnippet);
            row.setStatus("PENDING");
            row.setSource("EXTRACTED");
            row.setCreatedAt(LocalDateTime.now());
            row.setUpdatedAt(LocalDateTime.now());
            milestoneMapper.insert(row);
            inserted++;
        }
        return inserted;
    }

    /**
     * Notify creators about milestones due within the next 7 days (once per local day per row).
     */
    public void scanDueAndNotify() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(7);
        List<ContractPerformanceMilestone> due = milestoneMapper.selectList(
            new QueryWrapper<ContractPerformanceMilestone>()
                .eq("status", "PENDING")
                .isNotNull("due_date")
                .between("due_date", today, horizon)
        );
        for (ContractPerformanceMilestone m : due) {
            if (m.getLastRemindedAt() != null
                && m.getLastRemindedAt().toLocalDate().equals(today)) {
                continue;
            }
            Contract c = contractMapper.selectById(m.getContractId());
            if (c == null || c.getCreatorId() == null) {
                continue;
            }
            // 签后履约：仅对已签署及后续生命周期提醒
            String st = c.getStatus();
            if (st == null || !SIGNED_OR_ACTIVE.contains(st)) {
                continue;
            }
            String msg = String.format(
                "履约节点「%s」将于 %s 到期（合同 %s）",
                m.getTitle(),
                m.getDueDate(),
                c.getContractNo()
            );
            notificationService.sendReminder(
                c.getCreatorId(),
                c.getId(),
                c.getContractNo(),
                msg
            );
            m.setLastRemindedAt(LocalDateTime.now());
            milestoneMapper.updateById(m);
        }
    }

    @Scheduled(cron = "0 30 9 * * ?")
    public void scheduledPerformanceReminders() {
        scanDueAndNotify();
    }

    public List<Map<String, Object>> listAsMaps(Long contractId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ContractPerformanceMilestone m : listByContract(contractId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", m.getId());
            row.put("title", m.getTitle());
            row.put("dueDate", m.getDueDate() != null ? m.getDueDate().toString() : null);
            row.put("offsetDays", m.getOffsetDays());
            row.put("anchorNote", m.getAnchorNote());
            row.put("status", m.getStatus());
            row.put("source", m.getSource());
            row.put("rawSnippet", m.getRawSnippet());
            row.put("lastRemindedAt", m.getLastRemindedAt() != null ? m.getLastRemindedAt().toString() : null);
            list.add(row);
        }
        return list;
    }
}
