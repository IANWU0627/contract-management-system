package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractCounterparty;
import com.contracthub.mapper.ContractCounterpartyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contract-counterparties")
public class ContractCounterpartyController {
    
    @Autowired
    private ContractCounterpartyMapper counterpartyMapper;
    
    @GetMapping("/contract/{contractId}")
    public ApiResponse<List<ContractCounterparty>> getByContractId(@PathVariable Long contractId) {
        List<ContractCounterparty> list = counterpartyMapper.selectByContractId(contractId);
        return ApiResponse.success(list);
    }
    
    @PostMapping
    public ApiResponse<ContractCounterparty> create(@RequestBody ContractCounterparty counterparty) {
        counterpartyMapper.insert(counterparty);
        return ApiResponse.success(counterparty);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<ContractCounterparty> update(@PathVariable Long id, @RequestBody ContractCounterparty counterparty) {
        counterparty.setId(id);
        counterpartyMapper.updateById(counterparty);
        return ApiResponse.success(counterparty);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        counterpartyMapper.deleteById(id);
        return ApiResponse.success("删除成功");
    }
    
    @DeleteMapping("/contract/{contractId}")
    public ApiResponse<String> deleteByContractId(@PathVariable Long contractId) {
        counterpartyMapper.delete(null);
        return ApiResponse.success("删除成功");
    }
    
    @PostMapping("/batch")
    public ApiResponse<String> saveBatch(@RequestBody SaveBatchRequest request) {
        if (request.getContractId() != null) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContractCounterparty> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("contract_id", request.getContractId());
            counterpartyMapper.delete(wrapper);
            
            if (request.getCounterparties() != null) {
                for (int i = 0; i < request.getCounterparties().size(); i++) {
                    ContractCounterparty cp = request.getCounterparties().get(i);
                    cp.setContractId(request.getContractId());
                    cp.setSortOrder(i);
                    counterpartyMapper.insert(cp);
                }
            }
        }
        return ApiResponse.success("保存成功");
    }
    
    public static class SaveBatchRequest {
        private Long contractId;
        private List<ContractCounterparty> counterparties;
        
        public Long getContractId() { return contractId; }
        public void setContractId(Long contractId) { this.contractId = contractId; }
        public List<ContractCounterparty> getCounterparties() { return counterparties; }
        public void setCounterparties(List<ContractCounterparty> counterparties) { this.counterparties = counterparties; }
    }
}
