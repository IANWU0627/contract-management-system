package com.contracthub.controller;

import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.ContractReminder;
import com.contracthub.mapper.ContractReminderMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    @Mock
    private ContractReminderMapper contractReminderMapper;

    private ReminderController controller;

    @BeforeEach
    void setUp() {
        controller = new ReminderController(contractReminderMapper, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createShouldAcceptIntegerContractIdWithoutClassCastException() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractId", 123); // Integer (from JSON number)
        payload.put("contractNo", "TC-001");
        payload.put("contractTitle", "Demo");
        payload.put("expireDate", "2030-01-01");
        payload.put("remindDays", 7);
        payload.put("reminderType", 0);
        payload.put("status", 0);

        when(contractReminderMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(contractReminderMapper.insert(org.mockito.ArgumentMatchers.any(ContractReminder.class))).thenReturn(1);

        ApiResponse<Map<String, Object>> response = controller.create(payload);

        assertEquals(200, response.getCode());
        ArgumentCaptor<ContractReminder> captor = ArgumentCaptor.forClass(ContractReminder.class);
        verify(contractReminderMapper).insert(captor.capture());
        ContractReminder saved = captor.getValue();
        assertEquals(123L, saved.getContractId());
        assertEquals(7, saved.getRemindDays());
        assertEquals(0, saved.getReminderType());
        assertEquals(0, saved.getStatus());
        assertNotNull(saved.getExpireDate());
    }

    @Test
    void createShouldReturnErrorWhenContractIdMissing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractNo", "TC-002");
        payload.put("contractTitle", "Missing ID");
        payload.put("expireDate", "2030-02-01");

        ApiResponse<Map<String, Object>> response = controller.create(payload);

        assertEquals(500, response.getCode());
        assertEquals("error.reminder.contractIdRequired", response.getMessageKey());
        verify(contractReminderMapper, never()).insert(org.mockito.ArgumentMatchers.any(ContractReminder.class));
    }

    @Test
    void createShouldReuseExistingUnreadReminderInsteadOfInsert() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractId", 123);
        payload.put("contractNo", "TC-001");
        payload.put("contractTitle", "Demo");
        payload.put("expireDate", "2030-01-01");
        payload.put("remindDays", 7);
        payload.put("status", 0);

        ContractReminder existing = new ContractReminder();
        existing.setId(9L);
        existing.setContractId(123L);
        existing.setContractNo("TC-001");
        existing.setContractTitle("Demo");
        existing.setStatus(0);
        existing.setIsRead(false);
        when(contractReminderMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(existing));

        ApiResponse<Map<String, Object>> response = controller.create(payload);

        assertEquals(200, response.getCode());
        assertTrue((Boolean) response.getData().get("existed"));
        assertEquals(9L, response.getData().get("id"));
        verify(contractReminderMapper, never()).insert(org.mockito.ArgumentMatchers.any(ContractReminder.class));
    }

    @Test
    void getMyRemindersShouldUseSqlDeduplicatedPagination() {
        ContractReminder latestForContractA = new ContractReminder();
        latestForContractA.setId(11L);
        latestForContractA.setContractId(100L);
        latestForContractA.setContractNo("TC-100");
        latestForContractA.setContractTitle("A-New");
        latestForContractA.setStatus(0);
        latestForContractA.setIsRead(false);
        latestForContractA.setCreatedAt(LocalDateTime.now());

        ContractReminder contractB = new ContractReminder();
        contractB.setId(9L);
        contractB.setContractId(101L);
        contractB.setContractNo("TC-101");
        contractB.setContractTitle("B");
        contractB.setStatus(1);
        contractB.setIsRead(true);
        contractB.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(contractReminderMapper.selectMyDeduplicatedPage(1L, null, null, 20, 0))
                .thenReturn(List.of(latestForContractA, contractB));
        when(contractReminderMapper.countMyDeduplicated(1L, null, null)).thenReturn(2L);
        when(contractReminderMapper.countMyUnreadDeduplicated(1L)).thenReturn(1L);

        ApiResponse<Map<String, Object>> response = controller.getMyReminders(1, 20, null, null);

        assertEquals(200, response.getCode());
        Map<String, Object> data = response.getData();
        assertEquals(2, ((List<?>) data.get("list")).size());
        assertEquals(2L, data.get("total"));
        assertEquals(1L, data.get("unreadCount"));
        verify(contractReminderMapper).selectMyDeduplicatedPage(1L, null, null, 20, 0);
        verify(contractReminderMapper).countMyDeduplicated(1L, null, null);
        verify(contractReminderMapper).countMyUnreadDeduplicated(1L);
    }
}
