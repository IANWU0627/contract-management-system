package com.contracthub.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ContractFileService {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    public Map<String, Object> upload(MultipartFile file, Long contractId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("文件名无效");
        }

        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFileName;
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(dir, uniqueFileName));
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", uniqueFileName);
        result.put("originalName", originalFileName);
        result.put("fileUrl", "/api/contracts/download/" + uniqueFileName);
        result.put("fileSize", file.getSize());
        result.put("uploadTime", LocalDate.now().toString());
        if (contractId != null) {
            result.put("contractId", contractId);
        }
        return result;
    }

    public void deleteAttachment(String fileName) {
        File file = getSecureFile(fileName);
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        if (!file.delete()) {
            throw new RuntimeException("文件删除失败");
        }
    }

    public void download(String fileName, HttpServletResponse response) {
        File file = getSecureFile(fileName);
        if (file == null || !file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            response.setContentType(getContentType(fileName));
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
            );
            response.setContentLength((int) file.length());

            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        return !fileName.contains("\0");
    }

    private File getSecureFile(String fileName) {
        if (!isValidFileName(fileName)) {
            return null;
        }
        File uploadDir = new File(UPLOAD_DIR);
        File requestedFile = new File(uploadDir, fileName);
        try {
            String canonicalDir = uploadDir.getCanonicalPath();
            String canonicalFile = requestedFile.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalDir + File.separator)) {
                return null;
            }
            return requestedFile;
        } catch (Exception e) {
            return null;
        }
    }

    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
