package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractCommandService {
    private final ContractService contractService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractCommandService(ContractService contractService) {
        this.contractService = contractService;
    }

    public Map<String, Object> create(Map<String, Object> contractMap) {
        Contract contract = contractService.createContract(contractMap);
        Map<String, Object> result = contractService.buildContractResponse(contract);
        if (contractMap.containsKey("counterparties")) {
            result.put("counterparties", contractMap.get("counterparties"));
        } else {
            result.put("counterparties", new ArrayList<>());
        }
        result.put("timezone", contractMap.getOrDefault("timezone", java.time.ZoneId.systemDefault().getId()));
        return result;
    }

    public Map<String, Object> update(Long id, Map<String, Object> contractMap) {
        Contract contract = contractService.updateContract(id, contractMap);
        Map<String, Object> result = contractService.buildContractResponse(contract);
        if (contractMap.containsKey("counterparties")) {
            result.put("counterparties", contractMap.get("counterparties"));
        } else if (contract.getCounterparties() != null && !contract.getCounterparties().isEmpty()) {
            try {
                List<Map<String, Object>> cpList = objectMapper.readValue(
                        contract.getCounterparties(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                result.put("counterparties", cpList);
            } catch (Exception e) {
                result.put("counterparties", new ArrayList<>());
            }
        } else {
            result.put("counterparties", new ArrayList<>());
        }
        return result;
    }

    public void delete(Long id) {
        contractService.deleteContract(id);
    }

    public Map<String, Object> copy(Long id) {
        Contract contract = contractService.copyContract(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", contract.getId());
        result.put("contractNo", contract.getContractNo());
        return result;
    }
}
