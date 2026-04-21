package com.contracthub.service;

import com.contracthub.entity.Contract;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContractWorkflowService {
    private final ContractService contractService;

    public ContractWorkflowService(ContractService contractService) {
        this.contractService = contractService;
    }

    public void batchDelete(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Object> idsObj = (List<Object>) request.get("ids");
        if (idsObj == null) {
            return;
        }
        List<Long> ids = idsObj.stream()
                .filter(id -> id instanceof Number)
                .map(id -> ((Number) id).longValue())
                .collect(Collectors.toList());
        contractService.batchDeleteContracts(ids);
    }

    public Map<String, Object> batchUpdateStatus(Map<String, Object> request) {
        return contractService.batchUpdateStatus(request);
    }

    public Map<String, Object> batchApprove(Map<String, Object> request) {
        request.put("status", "APPROVED");
        return contractService.batchUpdateStatus(request);
    }

    public Map<String, Object> batchSubmit(Map<String, Object> request) {
        request.put("status", "PENDING");
        return contractService.batchUpdateStatus(request);
    }

    public Map<String, Object> batchEdit(Map<String, Object> request) {
        return contractService.batchEditContracts(request);
    }

    public Map<String, Object> submit(Long id) {
        Contract contract = contractService.submitForApproval(id);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> approve(Long id, String comment) {
        Contract contract = contractService.approveContract(id, comment);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> reject(Long id, String comment) {
        Contract contract = contractService.rejectContract(id, comment);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> withdraw(Long id, String reason) {
        Contract contract = contractService.withdrawApproval(id, reason);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> sign(Long id) {
        Contract contract = contractService.signContract(id);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> archive(Long id) {
        Contract contract = contractService.archiveContract(id);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> terminate(Long id, String reason) {
        Contract contract = contractService.terminateContract(id, reason);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> startRenewal(Long id, String reason) {
        Contract contract = contractService.startRenewal(id, reason);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> completeRenewal(Long id, String reason) {
        Contract contract = contractService.completeRenewal(id, reason);
        return contractService.buildContractResponse(contract);
    }

    public Map<String, Object> declineRenewal(Long id, String reason) {
        Contract contract = contractService.markNotRenewed(id, reason);
        return contractService.buildContractResponse(contract);
    }
}
