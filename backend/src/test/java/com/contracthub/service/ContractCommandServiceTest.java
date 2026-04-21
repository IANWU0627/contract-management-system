package com.contracthub.service;

import com.contracthub.entity.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractCommandServiceTest {

    @Mock
    private ContractService contractService;

    private ContractCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new ContractCommandService(contractService);
    }

    @Test
    void createShouldKeepCounterpartiesAndTimezone() {
        Contract contract = new Contract();
        Map<String, Object> request = new HashMap<>();
        request.put("counterparties", List.of(Map.of("name", "A Corp")));
        request.put("timezone", "Asia/Shanghai");

        when(contractService.createContract(request)).thenReturn(contract);
        when(contractService.buildContractResponse(contract)).thenReturn(new HashMap<>(Map.of("id", 1L)));

        Map<String, Object> result = commandService.create(request);

        assertEquals("Asia/Shanghai", result.get("timezone"));
        assertEquals(1L, result.get("id"));
        assertInstanceOf(List.class, result.get("counterparties"));
    }

    @Test
    void updateShouldParseCounterpartiesFromStoredJson() {
        Contract contract = new Contract();
        contract.setCounterparties("[{\"name\":\"Vendor A\"}]");
        Map<String, Object> request = new HashMap<>();

        when(contractService.updateContract(7L, request)).thenReturn(contract);
        when(contractService.buildContractResponse(contract)).thenReturn(new HashMap<>(Map.of("id", 7L)));

        Map<String, Object> result = commandService.update(7L, request);

        Object counterparties = result.get("counterparties");
        assertInstanceOf(List.class, counterparties);
        List<?> cpList = (List<?>) counterparties;
        assertEquals(1, cpList.size());
        assertTrue(cpList.get(0).toString().contains("Vendor A"));
    }

    @Test
    void copyShouldReturnIdAndContractNoOnly() {
        Contract copied = new Contract();
        copied.setId(22L);
        copied.setContractNo("CT-2026-0001");
        when(contractService.copyContract(22L)).thenReturn(copied);

        Map<String, Object> result = commandService.copy(22L);

        assertEquals(22L, result.get("id"));
        assertEquals("CT-2026-0001", result.get("contractNo"));
        verify(contractService).copyContract(22L);
    }

    @Test
    void updateShouldFallbackToEmptyCounterpartiesWhenStoredJsonInvalid() {
        Contract contract = new Contract();
        contract.setCounterparties("not-json");
        Map<String, Object> request = new HashMap<>();

        when(contractService.updateContract(9L, request)).thenReturn(contract);
        when(contractService.buildContractResponse(contract)).thenReturn(new HashMap<>(Map.of("id", 9L)));

        Map<String, Object> result = commandService.update(9L, request);

        assertInstanceOf(List.class, result.get("counterparties"));
        assertEquals(0, ((List<?>) result.get("counterparties")).size());
    }

    @Test
    void deleteShouldDelegateToService() {
        commandService.delete(100L);
        verify(contractService).deleteContract(100L);
    }

    @Test
    void createShouldPropagateServiceException() {
        Map<String, Object> request = new HashMap<>();
        when(contractService.createContract(request)).thenThrow(new RuntimeException("db error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> commandService.create(request));
        assertEquals("db error", ex.getMessage());
    }
}
