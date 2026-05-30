package com.legalflow.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DocumentReaderTool {

    public String readDocument(MultipartFile file) {
        log.info("读取文档: {}", file.getOriginalFilename());
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取文档失败: {}", e.getMessage());
            throw new RuntimeException("文档读取失败", e);
        }
    }

    public String readDocument(byte[] content, String fileName) {
        log.info("从字节数组读取文档: {}", fileName);
        try {
            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文档失败: {}", e.getMessage());
            throw new RuntimeException("文档读取失败", e);
        }
    }
}
