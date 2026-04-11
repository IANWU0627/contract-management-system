package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.contracthub.mapper.ContractMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ContractNumberService {

    private final ContractMapper contractMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ContractNumberService(ContractMapper contractMapper) {
        this.contractMapper = contractMapper;
    }

    @Transactional
    public String generateNextContractNo() {
        String date = LocalDate.now().format(formatter);
        String prefix = "TC" + date + "-";

        // 查询今天最大的合同编号
        QueryWrapper<Contract> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("contract_no", prefix);
        queryWrapper.orderByDesc("contract_no");
        queryWrapper.last("LIMIT 1");

        Contract lastContract = contractMapper.selectOne(queryWrapper);

        int nextSeq = 1;
        if (lastContract != null && lastContract.getContractNo() != null) {
            String contractNo = lastContract.getContractNo();
            // 提取流水号部分
            int lastDashIndex = contractNo.lastIndexOf("-");
            if (lastDashIndex > 0 && lastDashIndex < contractNo.length() - 1) {
                try {
                    String seqPart = contractNo.substring(lastDashIndex + 1);
                    nextSeq = Integer.parseInt(seqPart) + 1;
                } catch (NumberFormatException e) {
                    nextSeq = 1;
                }
            }
        }

        return prefix + String.format("%04d", nextSeq);
    }
}
