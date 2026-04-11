package com.contracthub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractReminder;
import com.contracthub.entity.ReminderRule;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractReminderMapper;
import com.contracthub.mapper.ReminderRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderSchedulerService {
    
    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);
    
    private final ContractMapper contractMapper;
    private final ContractReminderMapper reminderMapper;
    private final ReminderRuleMapper ruleMapper;
    private final NotificationService notificationService;
    
    public ReminderSchedulerService(ContractMapper contractMapper, ContractReminderMapper reminderMapper, 
                                   ReminderRuleMapper ruleMapper, NotificationService notificationService) {
        this.contractMapper = contractMapper;
        this.reminderMapper = reminderMapper;
        this.ruleMapper = ruleMapper;
        this.notificationService = notificationService;
    }
    
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkContractExpiring() {
        log.info("开始检查即将到期的合同");
        
        LocalDate today = LocalDate.now();
        // 获取所有启用的提醒规则
        List<ReminderRule> enabledRules = ruleMapper.selectEnabledRules();
        
        // 计算最大提醒天数
        int maxRemindDays = 0;
        for (ReminderRule rule : enabledRules) {
            if (rule.getRemindDays() > maxRemindDays) {
                maxRemindDays = rule.getRemindDays();
            }
        }
        // 如果没有规则，使用默认值30天
        if (maxRemindDays == 0) {
            maxRemindDays = 30;
        }
        
        LocalDate maxDate = today.plusDays(maxRemindDays);
        
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        wrapper.between("end_date", today, maxDate);
        wrapper.in("status", List.of("APPROVED", "SIGNED", "ARCHIVED"));
        
        List<Contract> expiringContracts = contractMapper.selectList(wrapper);
        
        for (Contract contract : expiringContracts) {
            LocalDate endDate = contract.getEndDate();
            int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
            
            log.info("检查合同: {} (类型: {}, 到期日期: {}, 剩余天数: {})", contract.getContractNo(), contract.getType(), endDate, daysRemaining);
            
            // 检查是否符合任何提醒规则
            boolean matched = false;
            for (ReminderRule rule : enabledRules) {
                log.info("检查规则: {} (类型: {}, 提醒天数: {})", rule.getName(), rule.getContractType(), rule.getRemindDays());
                if (matchesRule(contract, rule, daysRemaining)) {
                    log.info("合同匹配规则: {}", rule.getName());
                    createOrUpdateReminder(contract, daysRemaining);
                    matched = true;
                } else {
                    log.info("合同不匹配规则: {}", rule.getName());
                }
            }
            
            // 保留默认提醒规则（如果没有配置规则或规则不匹配）
            if (enabledRules.isEmpty() && (daysRemaining <= 7 || daysRemaining == 0 || daysRemaining == 30)) {
                log.info("使用默认提醒规则");
                createOrUpdateReminder(contract, daysRemaining);
                matched = true;
            }
            
            if (!matched) {
                log.info("合同没有匹配到任何提醒规则");
            }
        }
        
        log.info("检查完成，共发现 {} 份即将到期的合同", expiringContracts.size());
    }
    
    private boolean matchesRule(Contract contract, ReminderRule rule, int daysRemaining) {
        // 检查提醒天数是否匹配（剩余天数小于等于提醒天数）
        if (rule.getRemindDays() < daysRemaining) {
            return false;
        }
        
        // 检查合同类型是否匹配
        if (rule.getContractType() != null && !rule.getContractType().isEmpty()) {
            boolean typeMatch = false;
            String[] contractTypes = rule.getContractType().split(",");
            for (String type : contractTypes) {
                if (type.equals(contract.getType())) {
                    typeMatch = true;
                    break;
                }
            }
            if (!typeMatch) {
                return false;
            }
        }
        // 如果合同类型字符串为空，则匹配所有合同类型
        
        // 检查金额范围是否匹配
        if (rule.getMinAmount() != null) {
            if (contract.getAmount() == null || contract.getAmount().compareTo(rule.getMinAmount()) < 0) {
                return false;
            }
        }
        
        if (rule.getMaxAmount() != null) {
            if (contract.getAmount() == null || contract.getAmount().compareTo(rule.getMaxAmount()) > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private void createOrUpdateReminder(Contract contract, int daysRemaining) {
        // 检查是否已经存在针对该合同的提醒（不考虑具体天数）
        QueryWrapper<ContractReminder> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contract.getId());
        
        ContractReminder existing = reminderMapper.selectOne(wrapper);
        
        if (existing == null) {
            ContractReminder reminder = new ContractReminder();
            reminder.setContractId(contract.getId());
            reminder.setContractNo(contract.getContractNo());
            reminder.setContractTitle(contract.getTitle());
            reminder.setExpireDate(contract.getEndDate().atStartOfDay());
            reminder.setRemindDays(daysRemaining);
            reminder.setStatus(0);
            reminder.setCreatedAt(LocalDateTime.now());
            reminderMapper.insert(reminder);
            
            log.info("创建提醒: 合同 {} 还有 {} 天到期", contract.getContractNo(), daysRemaining);
        } else {
            // 如果已存在提醒，更新提醒天数为当前剩余天数
            existing.setRemindDays(daysRemaining);
            existing.setStatus(0);
            reminderMapper.updateById(existing);
            
            log.info("更新提醒: 合同 {} 还有 {} 天到期", contract.getContractNo(), daysRemaining);
        }
        
        // 发送通知给合同创建者
        if (contract.getCreatorId() != null) {
            String message;
            if (daysRemaining == 0) {
                message = "合同 " + contract.getContractNo() + " (" + contract.getTitle() + ") 今天到期！";
            } else {
                message = "合同 " + contract.getContractNo() + " (" + contract.getTitle() + ") 还有 " + daysRemaining + " 天到期";
            }
            notificationService.sendReminder(contract.getCreatorId(), contract.getId(), contract.getContractNo(), message);
        }
    }
}
