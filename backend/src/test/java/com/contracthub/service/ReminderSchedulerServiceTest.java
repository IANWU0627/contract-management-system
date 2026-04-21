package com.contracthub.service;

import com.contracthub.entity.Contract;
import com.contracthub.entity.ContractReminder;
import com.contracthub.entity.ReminderRule;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.mapper.ContractReminderMapper;
import com.contracthub.mapper.ReminderRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private ContractMapper contractMapper;
    @Mock
    private ContractReminderMapper reminderMapper;
    @Mock
    private ReminderRuleMapper ruleMapper;

    private CapturingNotificationService notificationService;

    private ReminderSchedulerService service;

    @BeforeEach
    void setUp() {
        notificationService = new CapturingNotificationService();
        service = new ReminderSchedulerService(contractMapper, reminderMapper, ruleMapper, notificationService);
    }

    @Test
    void checkContractExpiringShouldUpdateLatestReminderWhenDuplicateRowsExist() {
        Contract contract = new Contract();
        contract.setId(100L);
        contract.setCreatorId(200L);
        contract.setContractNo("C-100");
        contract.setTitle("Demo");
        contract.setStatus("SIGNED");
        contract.setEndDate(LocalDate.now().plusDays(7));

        ContractReminder latest = new ContractReminder();
        latest.setId(9L);
        latest.setContractId(100L);
        latest.setRecipientUserId(null); // should be backfilled from creatorId

        ContractReminder older = new ContractReminder();
        older.setId(5L);
        older.setContractId(100L);
        older.setRecipientUserId(200L);

        when(ruleMapper.selectEnabledRules()).thenReturn(List.of());
        when(contractMapper.selectList(any())).thenReturn(List.of(contract));
        when(reminderMapper.selectList(any())).thenReturn(List.of(latest, older));

        service.checkContractExpiring();

        ArgumentCaptor<ContractReminder> captor = ArgumentCaptor.forClass(ContractReminder.class);
        verify(reminderMapper, times(1)).updateById(captor.capture());
        verify(reminderMapper, never()).insert(any(ContractReminder.class));
        ContractReminder updated = captor.getValue();
        assertEquals(9L, updated.getId());
        assertEquals(200L, updated.getRecipientUserId());
        assertEquals(0, updated.getStatus());
        assertEquals(7, updated.getRemindDays());

        assertEquals(200L, notificationService.userId);
        assertEquals(100L, notificationService.contractId);
        assertEquals("C-100", notificationService.contractNo);
        assertNotNull(notificationService.message);
    }

    @Test
    void checkContractExpiringShouldCreateReminderWhenNoExistingAndSendTodayMessage() {
        Contract contract = new Contract();
        contract.setId(101L);
        contract.setCreatorId(201L);
        contract.setContractNo("C-101");
        contract.setTitle("Today Expire");
        contract.setStatus("APPROVED");
        contract.setEndDate(LocalDate.now());

        when(ruleMapper.selectEnabledRules()).thenReturn(List.of());
        when(contractMapper.selectList(any())).thenReturn(List.of(contract));
        when(reminderMapper.selectList(any())).thenReturn(List.of());
        when(reminderMapper.insert(any(ContractReminder.class))).thenReturn(1);

        service.checkContractExpiring();

        ArgumentCaptor<ContractReminder> insertCaptor = ArgumentCaptor.forClass(ContractReminder.class);
        verify(reminderMapper, times(1)).insert(insertCaptor.capture());
        ContractReminder inserted = insertCaptor.getValue();
        assertEquals(101L, inserted.getContractId());
        assertEquals(201L, inserted.getRecipientUserId());
        assertEquals(0, inserted.getRemindDays());
        assertEquals(0, inserted.getStatus());
        assertNotNull(inserted.getExpireDate());
        assertNotNull(inserted.getCreatedAt());

        assertEquals(201L, notificationService.userId);
        assertEquals(101L, notificationService.contractId);
        assertEquals("C-101", notificationService.contractNo);
        assertTrue(notificationService.message.contains("今天到期"));
        verify(reminderMapper, never()).updateById(any(ContractReminder.class));
    }

    @Test
    void checkContractExpiringShouldOnlyCreateForContractsMatchingEnabledRules() {
        ReminderRule rule = new ReminderRule();
        rule.setName("采购高金额10天提醒");
        rule.setContractType("PURCHASE");
        rule.setMinAmount(new BigDecimal("1000"));
        rule.setRemindDays(10);

        Contract matched = new Contract();
        matched.setId(201L);
        matched.setCreatorId(301L);
        matched.setContractNo("C-201");
        matched.setTitle("Matched");
        matched.setStatus("SIGNED");
        matched.setType("PURCHASE");
        matched.setAmount(new BigDecimal("1500"));
        matched.setEndDate(LocalDate.now().plusDays(8));

        Contract unmatched = new Contract();
        unmatched.setId(202L);
        unmatched.setCreatorId(302L);
        unmatched.setContractNo("C-202");
        unmatched.setTitle("Unmatched");
        unmatched.setStatus("SIGNED");
        unmatched.setType("SERVICE");
        unmatched.setAmount(new BigDecimal("500"));
        unmatched.setEndDate(LocalDate.now().plusDays(8));

        when(ruleMapper.selectEnabledRules()).thenReturn(List.of(rule));
        when(contractMapper.selectList(any())).thenReturn(List.of(matched, unmatched));
        when(reminderMapper.selectList(any())).thenReturn(List.of());
        when(reminderMapper.insert(any(ContractReminder.class))).thenReturn(1);

        service.checkContractExpiring();

        ArgumentCaptor<ContractReminder> captor = ArgumentCaptor.forClass(ContractReminder.class);
        verify(reminderMapper, times(1)).insert(captor.capture());
        ContractReminder inserted = captor.getValue();
        assertEquals(201L, inserted.getContractId());
        assertEquals(301L, inserted.getRecipientUserId());
        assertEquals(8, inserted.getRemindDays());

        assertEquals(301L, notificationService.userId);
        assertEquals(201L, notificationService.contractId);
        assertEquals("C-201", notificationService.contractNo);
    }

    @Test
    void checkContractExpiringShouldNotDuplicateWhenContractMatchesMultipleRules() {
        ReminderRule broadRule = new ReminderRule();
        broadRule.setName("通用30天");
        broadRule.setRemindDays(30);

        ReminderRule specificRule = new ReminderRule();
        specificRule.setName("采购10天");
        specificRule.setContractType("PURCHASE");
        specificRule.setRemindDays(10);

        Contract contract = new Contract();
        contract.setId(301L);
        contract.setCreatorId(401L);
        contract.setContractNo("C-301");
        contract.setTitle("Overlap");
        contract.setStatus("SIGNED");
        contract.setType("PURCHASE");
        contract.setAmount(new BigDecimal("2000"));
        contract.setEndDate(LocalDate.now().plusDays(7));

        when(ruleMapper.selectEnabledRules()).thenReturn(List.of(broadRule, specificRule));
        when(contractMapper.selectList(any())).thenReturn(List.of(contract));
        when(reminderMapper.selectList(any())).thenReturn(List.of());
        when(reminderMapper.insert(any(ContractReminder.class))).thenReturn(1);

        service.checkContractExpiring();

        verify(reminderMapper, times(1)).insert(any(ContractReminder.class));
        assertEquals(1, notificationService.sendCount);
        assertEquals(401L, notificationService.userId);
        assertEquals(301L, notificationService.contractId);
        assertEquals("C-301", notificationService.contractNo);
    }

    private static class CapturingNotificationService extends NotificationService {
        Long userId;
        Long contractId;
        String contractNo;
        String message;
        int sendCount;

        CapturingNotificationService() {
            super(null, null, null);
        }

        @Override
        public void sendReminder(Long userId, Long contractId, String contractNo, String message) {
            this.sendCount++;
            this.userId = userId;
            this.contractId = contractId;
            this.contractNo = contractNo;
            this.message = message;
        }
    }
}
