package com.contracthub.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractFileServiceTest {

    private final ContractFileService fileService = new ContractFileService();
    private final File uploadDir = new File(System.getProperty("user.dir") + File.separator + "uploads");

    @AfterEach
    void cleanUploads() {
        if (!uploadDir.exists()) {
            return;
        }
        File[] files = uploadDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                }
            }
        }
        uploadDir.delete();
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileService.upload(file, null));
        assertEquals("文件不能为空", ex.getMessage());
    }

    @Test
    void uploadAndDownloadShouldWorkWithSafeFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.txt",
                "text/plain",
                "hello-contract".getBytes(StandardCharsets.UTF_8)
        );

        String uploadedName = (String) fileService.upload(file, 5L).get("fileName");
        assertNotNull(uploadedName);
        assertTrue(uploadedName.endsWith("_contract.txt"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        fileService.download(uploadedName, response);

        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertTrue(response.getContentAsString().contains("hello-contract"));
    }

    @Test
    void deleteAttachmentShouldRejectPathTraversal() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileService.deleteAttachment("../passwd")
        );
        assertEquals("文件不存在", ex.getMessage());
    }
}
