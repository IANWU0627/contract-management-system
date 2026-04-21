package com.contracthub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractExportServiceTest {

    private ContractExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ContractExportService(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void generatePdfShouldReturnPdfWithHeaders() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractNo", "T-100");
        payload.put("content", "<h1>Hello</h1><p>World</p>");
        payload.put("watermark", "INTERNAL");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.generatePdf(payload, response);

        assertTrue(response.getContentType() != null && response.getContentType().startsWith("application/pdf"));
        String disposition = response.getHeader("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("T-100.pdf"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    @Test
    void generatePdfShouldFallbackWhenContentMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractNo", "EMPTY");

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.generatePdf(payload, response);

        assertTrue(response.getContentType() != null && response.getContentType().startsWith("application/pdf"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }
}
