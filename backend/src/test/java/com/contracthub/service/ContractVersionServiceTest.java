package com.contracthub.service;

import com.contracthub.entity.ContractVersion;
import com.contracthub.service.impl.ContractVersionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContractVersionServiceTest {

    @Mock
    private ContractVersionService versionService;

    @InjectMocks
    private ContractVersionServiceImpl versionServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateNextVersion() {
        // 测试首次版本生成
        String firstVersion = versionServiceImpl.generateNextVersion(1L);
        assertEquals("v1.0", firstVersion);

        // 测试版本递增
        ContractVersion mockVersion = new ContractVersion();
        mockVersion.setVersion("v1.9");
        when(versionService.getVersionHistory(1L)).thenReturn(Arrays.asList(mockVersion));

        String nextVersion = versionServiceImpl.generateNextVersion(1L);
        assertEquals("v2.0", nextVersion);
    }

    @Test
    public void testCreateVersion() {
        Long contractId = 1L;
        String content = "合同内容";
        String changeDesc = "初始创建";
        Long operatorId = 1L;
        String operatorName = "admin";

        ContractVersion version = versionServiceImpl.createVersion(
            contractId, content, changeDesc, operatorId, operatorName
        );

        assertNotNull(version);
        assertEquals(contractId, version.getContractId());
        assertEquals(content, version.getContent());
        assertEquals(changeDesc, version.getChangeDesc());
        assertEquals(operatorId, version.getOperatorId());
        assertEquals(operatorName, version.getOperatorName());
        assertNotNull(version.getVersion());
    }

    @Test
    public void testCompareVersions() {
        Long contractId = 1L;
        Long versionId1 = 1L;
        Long versionId2 = 2L;

        ContractVersion v1 = new ContractVersion();
        v1.setId(versionId1);
        v1.setVersion("v1.0");
        v1.setContent("第一行\n第二行");

        ContractVersion v2 = new ContractVersion();
        v2.setId(versionId2);
        v2.setVersion("v1.1");
        v2.setContent("第一行\n修改后的第二行\n第三行");

        when(versionService.getVersionDetail(contractId, versionId1)).thenReturn(v1);
        when(versionService.getVersionDetail(contractId, versionId2)).thenReturn(v2);

        Map<String, Object> result = versionServiceImpl.compareVersions(contractId, versionId1, versionId2);

        assertNotNull(result);
        assertEquals("v1.0", result.get("version1"));
        assertEquals("v1.1", result.get("version2"));
        assertTrue(result.containsKey("differences"));
        assertTrue(result.containsKey("addedLines"));
        assertTrue(result.containsKey("removedLines"));
    }

    @Test
    public void testRestoreVersion() {
        Long contractId = 1L;
        Long versionId = 1L;
        Long operatorId = 1L;
        String operatorName = "admin";

        ContractVersion targetVersion = new ContractVersion();
        targetVersion.setId(versionId);
        targetVersion.setVersion("v1.0");
        targetVersion.setContent("原始内容");

        when(versionService.getVersionDetail(contractId, versionId)).thenReturn(targetVersion);

        Map<String, Object> result = versionServiceImpl.restoreVersion(
            contractId, versionId, operatorId, operatorName
        );

        assertNotNull(result);
        assertEquals("v1.0", result.get("restoredVersion"));
        assertNotNull(result.get("newVersion"));
        assertEquals("原始内容", result.get("content"));
    }

    @Test
    public void testGetVersionHistory() {
        Long contractId = 1L;

        ContractVersion v1 = new ContractVersion();
        v1.setId(1L);
        v1.setVersion("v1.0");

        ContractVersion v2 = new ContractVersion();
        v2.setId(2L);
        v2.setVersion("v1.1");

        when(versionService.getVersionHistory(contractId)).thenReturn(Arrays.asList(v2, v1));

        List<ContractVersion> versions = versionServiceImpl.getVersionHistory(contractId);

        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertEquals("v1.1", versions.get(0).getVersion());
        assertEquals("v1.0", versions.get(1).getVersion());
    }

    @Test
    public void testGetLatestVersion() {
        Long contractId = 1L;

        ContractVersion latest = new ContractVersion();
        latest.setVersion("v2.0");

        when(versionService.getLatestVersion(contractId)).thenReturn("v2.0");

        String version = versionServiceImpl.getLatestVersion(contractId);

        assertEquals("v2.0", version);
    }
}
