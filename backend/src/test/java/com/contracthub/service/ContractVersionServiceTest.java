package com.contracthub.service;

import com.contracthub.entity.ContractVersion;
import com.contracthub.entity.ContractVersionDiffAnalysis;
import com.contracthub.mapper.ContractVersionDiffAnalysisMapper;
import com.contracthub.mapper.ContractVersionMapper;
import com.contracthub.service.impl.ContractVersionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContractVersionServiceTest {

    @Mock
    private ContractVersionMapper versionMapper;

    @Mock
    private ContractVersionDiffAnalysisMapper diffAnalysisMapper;

    @InjectMocks
    private ContractVersionServiceImpl versionServiceImpl;

    @Test
    public void testGenerateNextVersion() {
        when(versionMapper.selectOne(any())).thenReturn(null);
        String firstVersion = versionServiceImpl.generateNextVersion(1L);
        assertEquals("v1.0", firstVersion);

        ContractVersion latest = new ContractVersion();
        latest.setVersion("v1.9");
        when(versionMapper.selectOne(any())).thenReturn(latest);
        String nextVersion = versionServiceImpl.generateNextVersion(1L);
        assertEquals("v2.0", nextVersion);
    }

    @Test
    public void testCreateVersion() {
        when(versionMapper.selectOne(any())).thenReturn(null);
        ContractVersion version = versionServiceImpl.createVersion(1L, "合同内容", "初始创建", 1L, "admin");

        assertEquals(1L, version.getContractId());
        assertEquals("合同内容", version.getContent());
        assertEquals("初始创建", version.getChangeDesc());
        assertEquals("v1.0", version.getVersion());
        verify(versionMapper).insert(any(ContractVersion.class));
    }

    @Test
    public void testCompareVersions() {
        ContractVersion v1 = new ContractVersion();
        v1.setId(1L);
        v1.setContractId(1L);
        v1.setVersion("v1.0");
        v1.setContent("第一行\n第二行");

        ContractVersion v2 = new ContractVersion();
        v2.setId(2L);
        v2.setContractId(1L);
        v2.setVersion("v1.1");
        v2.setContent("第一行\n修改后的第二行\n第三行");

        ContractVersionDiffAnalysis cached = new ContractVersionDiffAnalysis();
        cached.setOverallRisk("MEDIUM");
        cached.setDiffJson("{\"differences\":[],\"addedLines\":1,\"removedLines\":0,\"clauseChanges\":[]}");
        cached.setRiskJson("{\"riskSummary\":{},\"riskItems\":[],\"aiCommentary\":\"ok\"}");

        when(versionMapper.selectOne(any())).thenReturn(v1, v2);
        when(diffAnalysisMapper.selectOne(any())).thenReturn(cached);

        Map<String, Object> result = versionServiceImpl.compareVersions(1L, 1L, 2L);

        assertEquals("v1.0", result.get("version1"));
        assertEquals("v1.1", result.get("version2"));
        assertTrue(result.containsKey("differences"));
        assertEquals(1, result.get("addedLines"));
    }

    @Test
    public void testGetVersionHistory() {
        ContractVersion v1 = new ContractVersion();
        v1.setVersion("v1.1");
        ContractVersion v2 = new ContractVersion();
        v2.setVersion("v1.0");
        when(versionMapper.selectList(any())).thenReturn(List.of(v1, v2));

        List<ContractVersion> versions = versionServiceImpl.getVersionHistory(1L);

        assertEquals(2, versions.size());
        assertEquals("v1.1", versions.get(0).getVersion());
    }

    @Test
    public void testGetLatestVersion() {
        ContractVersion latest = new ContractVersion();
        latest.setVersion("v2.0");
        when(versionMapper.selectOne(any())).thenReturn(latest);
        assertEquals("v2.0", versionServiceImpl.getLatestVersion(1L));
    }
}
