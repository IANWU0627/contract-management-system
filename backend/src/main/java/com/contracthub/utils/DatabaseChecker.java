package com.contracthub.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.ContractManagementApplication;
import com.contracthub.entity.Contract;
import com.contracthub.entity.ReminderRule;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ReminderRuleMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DatabaseChecker {
    public static void main(String[] args) {
        // 创建Spring Boot上下文
        ApplicationContext context = SpringApplication.run(ContractManagementApplication.class, args);
        
        // 获取Mapper
        ContractMapper contractMapper = context.getBean(ContractMapper.class);
        ReminderRuleMapper ruleMapper = context.getBean(ReminderRuleMapper.class);
        
        // 检查合同
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(3); // 3天内到期
        
        QueryWrapper<Contract> contractWrapper = new QueryWrapper<>();
        contractWrapper.between("end_date", today, maxDate);
        contractWrapper.in("status", List.of("APPROVED", "SIGNED", "ARCHIVED"));
        
        List<Contract> contracts = contractMapper.selectList(contractWrapper);
        System.out.println("找到 " + contracts.size() + " 份即将到期的合同：");
        
        for (Contract contract : contracts) {
            System.out.println("合同ID: " + contract.getId());
            System.out.println("合同编号: " + contract.getContractNo());
            System.out.println("合同标题: " + contract.getTitle());
            System.out.println("合同类型: " + contract.getType());
            System.out.println("到期日期: " + contract.getEndDate());
            System.out.println("合同状态: " + contract.getStatus());
            System.out.println("剩余天数: " + ChronoUnit.DAYS.between(today, contract.getEndDate()));
            System.out.println("------------------------");
        }
        
        // 检查提醒规则
        List<ReminderRule> rules = ruleMapper.selectEnabledRules();
        System.out.println("找到 " + rules.size() + " 条启用的提醒规则：");
        
        for (ReminderRule rule : rules) {
            System.out.println("规则ID: " + rule.getId());
            System.out.println("规则名称: " + rule.getName());
            System.out.println("适用类型: " + rule.getContractType());
            System.out.println("提醒天数: " + rule.getRemindDays());
            System.out.println("------------------------");
        }
        
        // 退出应用
        System.exit(0);
    }
}