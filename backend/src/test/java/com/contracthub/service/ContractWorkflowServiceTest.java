package com.contracthub.service;

import com.contracthub.entity.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractWorkflowServiceTest {

    @Mock
    private ContractService contractService;

    private ContractWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new ContractWorkflowService(contractService);
    }

    @Test
    void batchDeleteShouldIgnoreWhenIdsMissing() {
        workflowService.batchDelete(new HashMap<>());
        verify(contractService, never()).batchDeleteContracts(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void batchDeleteShouldConvertNumericIdsOnly() {
        Map<String, Object> request = new HashMap<>();
        request.put("ids", List.of(1, 2L, "3", 4.0));

        workflowService.batchDelete(request);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(contractService).batchDeleteContracts(idsCaptor.capture());
        assertEquals(List.of(1L, 2L, 4L), idsCaptor.getValue());
    }

    @Test
    void batchApproveShouldForceApprovedStatus() {
        Map<String, Object> request = new HashMap<>();
        request.put("ids", List.of(1L, 2L));
        when(contractService.batchUpdateStatus(anyMap())).thenReturn(Map.of("successCount", 2));

        Map<String, Object> result = workflowService.batchApprove(request);

        assertEquals(2, result.get("successCount"));
        assertEquals("APPROVED", request.get("status"));
        verify(contractService).batchUpdateStatus(request);
    }

    @Test
    void batchSubmitShouldForcePendingStatus() {
        Map<String, Object> request = new HashMap<>();
        request.put("ids", List.of(1L));
        when(contractService.batchUpdateStatus(anyMap())).thenReturn(Map.of("successCount", 1));

        Map<String, Object> result = workflowService.batchSubmit(request);

        assertEquals(1, result.get("successCount"));
        assertEquals("PENDING", request.get("status"));
        verify(contractService).batchUpdateStatus(request);
    }

    @Test
    void submitShouldReturnBuiltResponse() {
        Contract contract = new Contract();
        contract.setId(99L);
        Map<String, Object> built = Map.of("id", 99L, "status", "PENDING");

        when(contractService.submitForApproval(99L)).thenReturn(contract);
        when(contractService.buildContractResponse(contract)).thenReturn(built);

        Map<String, Object> result = workflowService.submit(99L);

        assertEquals(99L, result.get("id"));
        assertEquals("PENDING", result.get("status"));
        verify(contractService).submitForApproval(99L);
    }

    @Test
    void declineRenewalShouldUseMarkNotRenewedPath() {
        Contract contract = new Contract();
        contract.setId(8L);
        when(contractService.markNotRenewed(8L, "not needed")).thenReturn(contract);
        when(contractService.buildContractResponse(contract)).thenReturn(Map.of("ok", true));

        Map<String, Object> result = workflowService.declineRenewal(8L, "not needed");

        assertTrue((Boolean) result.get("ok"));
        verify(contractService).markNotRenewed(8L, "not needed");
    }

    @Test
    void approveShouldPropagateServiceException() {
        when(contractService.approveContract(3L, "ok")).thenThrow(new RuntimeException("invalid status"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> workflowService.approve(3L, "ok"));
        assertEquals("invalid status", ex.getMessage());
    }

    @Test
    void batchDeleteShouldCallServiceWithEmptyListWhenNoNumericIds() {
        Map<String, Object> request = new HashMap<>();
        request.put("ids", List.of("x", "y"));

        workflowService.batchDelete(request);

        verify(contractService).batchDeleteContracts(List.of());
    }
}
