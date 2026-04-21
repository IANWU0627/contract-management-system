package com.contracthub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contracthub.dto.ApiResponse;
import com.contracthub.entity.Contract;
import com.contracthub.mapper.UserMapper;
import com.contracthub.mapper.ContractMapper;
import com.contracthub.service.ContractDataScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirationWarningControllerTest {

    @Mock
    private ContractMapper contractMapper;

    @Mock
    private UserMapper userMapper;

    private ExpirationWarningController controller;

    @BeforeEach
    void setUp() {
        ContractDataScopeService contractDataScopeService = new ContractDataScopeService(userMapper);
        controller = new ExpirationWarningController(contractMapper, contractDataScopeService);
    }

    @Test
    void getExpiringWorkbenchSummaryShouldBucketAndExposeOwnerFields() {
        Contract c1 = new Contract();
        c1.setId(1L);
        c1.setContractNo("C-001");
        c1.setTitle("Signed Contract");
        c1.setStatus("SIGNED");
        c1.setAmount(BigDecimal.valueOf(100));
        c1.setEndDate(LocalDate.now().plusDays(1));
        c1.setCreatorId(7L);
        c1.setCurrentApproverName("Alice");

        Contract c2 = new Contract();
        c2.setId(2L);
        c2.setContractNo("C-002");
        c2.setTitle("Draft Contract");
        c2.setStatus("DRAFT");
        c2.setAmount(BigDecimal.valueOf(300));
        c2.setEndDate(LocalDate.now().plusDays(8));
        c2.setCreatorId(9L);

        when(contractMapper.selectList(ArgumentMatchers.any(QueryWrapper.class))).thenReturn(List.of(c1, c2));

        ApiResponse<Map<String, Object>> response = controller.getExpiringWorkbenchSummary();
        Map<String, Object> data = response.getData();

        assertNotNull(data);
        Map<String, Object> overview = (Map<String, Object>) data.get("overview");
        assertEquals(1, overview.get("today"));
        assertEquals(1, overview.get("within7Days"));
        assertEquals(2, overview.get("within30Days"));

        List<Map<String, Object>> oneDay = (List<Map<String, Object>>) data.get("oneDay");
        assertEquals("startRenewal", oneDay.get(0).get("recommendedAction"));
        assertEquals("Alice", oneDay.get(0).get("currentApproverName"));
        assertEquals(7L, oneDay.get(0).get("creatorId"));

        List<Map<String, Object>> thirtyDays = (List<Map<String, Object>>) data.get("thirtyDays");
        Map<String, Object> draftItem = thirtyDays.stream()
                .filter(item -> Long.valueOf(2L).equals(item.get("id")))
                .findFirst()
                .orElseThrow();
        assertEquals("submit", draftItem.get("recommendedAction"));
    }

}
