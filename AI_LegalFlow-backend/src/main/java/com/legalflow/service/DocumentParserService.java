package com.legalflow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DocumentParserService {

    public String parseDocument(MultipartFile file) {
        log.info("解析文档: {}", file.getOriginalFilename());
        
        try {
            String filename = file.getOriginalFilename().toLowerCase();
            
            if (filename.endsWith(".txt")) {
                return parseTextFile(file);
            } else {
                // 对于其他类型的文件，先尝试简单的文本读取
                return parseTextFile(file);
            }
        } catch (Exception e) {
            log.error("文档解析失败: {}", e.getMessage(), e);
            return "";
        }
    }

    public String parseDocument(byte[] content) {
        log.info("解析字节数组文档，大小: {} 字节", content.length);
        
        try {
            String text = new String(content, StandardCharsets.UTF_8);
            log.info("从文档中解析到 {} 个字符", text.length());
            return text;
        } catch (Exception e) {
            log.error("文档解析失败: {}", e.getMessage(), e);
            return "";
        }
    }

    private String parseTextFile(MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String result = content.toString();
        log.info("从文本文件中解析到 {} 个字符", result.length());
        return result;
    }

    public String extractTextFromPdf(byte[] pdfContent) {
        log.info("PDF解析功能待实现");
        return "";
    }
}
