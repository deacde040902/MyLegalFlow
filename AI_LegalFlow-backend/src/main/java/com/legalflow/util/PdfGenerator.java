package com.legalflow.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PdfGenerator {

    public byte[] generatePdf(String htmlContent) {
        log.info("正在从HTML内容生成PDF，内容长度: {} 字符", htmlContent.length());

        // TODO: iText PDF生成功能已暂时禁用
        // 重新启用时请在pom.xml中添加iText依赖
        
        // 临时返回空数组
        byte[] pdfBytes = new byte[0];
        log.info("PDF生成已跳过(iText未启用)，返回空数组");
        return pdfBytes;
    }

    public byte[] generatePdfFromMarkdown(String markdown) {
        String html = MarkdownConverter.toHtml(markdown);
        return generatePdf(html);
    }
}
