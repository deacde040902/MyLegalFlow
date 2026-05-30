package com.legalflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalflow.dto.KnowledgeDocument;
import com.legalflow.dto.KnowledgeSearchResult;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeBaseService {

    private final Map<String, KnowledgeDocument> documentStore = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        loadKnowledgeBase();
    }

    public void loadKnowledgeBase() {
        log.info("开始加载法律知识库...");
        
        try {
            loadTextFiles();
            log.info("知识库加载完成，共 {} 条记录", documentStore.size());
        } catch (Exception e) {
            log.error("知识库加载失败: {}", e.getMessage(), e);
        }
    }

    private void loadTextFiles() throws Exception {
        String[] knowledgeFiles = {
            "knowledgebase/合同常见风险条款清单.txt",
            "knowledgebase/法律数字问答库.txt",
            "knowledgebase/行业经验与案例.txt",
            "knowledgebase/合同测试.txt"
        };

        for (String filePath : knowledgeFiles) {
            try {
                ClassPathResource resource = new ClassPathResource(filePath);
                if (resource.exists()) {
                    loadDocument(filePath, resource);
                } else {
                    log.warn("知识库文件不存在: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("加载知识库文件失败 {}: {}", filePath, e.getMessage());
            }
        }
    }

    private void loadDocument(String filePath, ClassPathResource resource) throws Exception {
        String fileName = extractFileName(filePath);
        String category = determineCategory(fileName);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            StringBuilder content = new StringBuilder();
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                if (isStructuredContent(fileName)) {
                    String docId = generateDocId(fileName, lineNumber);
                    KnowledgeDocument doc = createDocument(docId, trimmedLine, category, fileName);
                    documentStore.put(docId, doc);
                } else {
                    if (content.length() > 0) {
                        content.append("\n");
                    }
                    content.append(trimmedLine);
                }
            }

            if (!isStructuredContent(fileName) && content.length() > 0) {
                String docId = generateDocId(fileName, 0);
                KnowledgeDocument doc = createDocument(docId, content.toString(), category, fileName);
                documentStore.put(docId, doc);
            }
        }
        
        log.info("加载知识库文件: {} ({} 条记录)", fileName, 
                isStructuredContent(fileName) ? "多条" : "1条");
    }

    private String extractFileName(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String determineCategory(String fileName) {
        if (fileName.contains("风险")) {
            return "风险条款";
        } else if (fileName.contains("问答") || fileName.contains("法律数字")) {
            return "法律问答";
        } else if (fileName.contains("案例") || fileName.contains("经验")) {
            return "行业案例";
        } else if (fileName.contains("民法典")) {
            return "法律法规";
        } else if (fileName.contains("合同")) {
            return "合同范本";
        }
        return "其他";
    }

    private boolean isStructuredContent(String fileName) {
        return fileName.contains("问答") || fileName.contains("风险条款");
    }

    private String generateDocId(String fileName, int lineNumber) {
        return fileName + "_" + lineNumber;
    }

    private KnowledgeDocument createDocument(String docId, String content, String category, String source) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setTitle(extractTitle(content));
        doc.setContent(content);
        doc.setCategory(category);
        doc.setSource(source);
        doc.setTags(extractTags(content));
        doc.setCreatedAt(java.time.LocalDateTime.now());
        return doc;
    }

    private String extractTitle(String content) {
        if (content.length() <= 50) {
            return content;
        }
        int newlineIndex = content.indexOf('\n');
        if (newlineIndex > 0 && newlineIndex < 50) {
            return content.substring(0, newlineIndex);
        }
        return content.substring(0, Math.min(50, content.length())) + "...";
    }

    private List<String> extractTags(String content) {
        List<String> tags = new ArrayList<>();
        
        String[] keywords = {
            "竞业限制", "违约金", "试用期", "解除合同", "赔偿",
            "加班", "工资", "保险", "保密", "培训",
            "劳动合同", "保密协议", "离职", "补偿金", "劳动仲裁"
        };
        
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                tags.add(keyword);
            }
        }
        
        return tags;
    }

    public List<KnowledgeDocument> search(String query, int topK, String category) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<KnowledgeSearchResult> results = searchWithScore(query, topK, category);
        
        return results.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    public List<KnowledgeSearchResult> searchWithScore(String query, int topK, String category) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] queryTerms = query.toLowerCase().split("[\\s，。、,.!?:;]+");
        List<SearchHit> hits = new ArrayList<>();

        for (KnowledgeDocument doc : documentStore.values()) {
            if (category != null && !category.isEmpty() && 
                !doc.getCategory().equals(category)) {
                continue;
            }

            double score = calculateRelevanceScore(queryTerms, doc);
            
            if (score > 0) {
                hits.add(new SearchHit(doc, score));
            }
        }

        return hits.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK > 0 ? topK : 10)
                .map(hit -> toSearchResult(hit.getDoc(), hit.getScore()))
                .collect(Collectors.toList());
    }

    private double calculateRelevanceScore(String[] queryTerms, KnowledgeDocument doc) {
        double score = 0;
        String contentLower = doc.getContent().toLowerCase();
        String titleLower = doc.getTitle().toLowerCase();
        
        for (String term : queryTerms) {
            if (term.length() < 2) continue;
            
            if (contentLower.contains(term)) {
                score += 1.0;
                
                if (titleLower.contains(term)) {
                    score += 2.0;
                }
            }
            
            if (doc.getTags() != null) {
                for (String tag : doc.getTags()) {
                    if (tag.toLowerCase().contains(term)) {
                        score += 1.5;
                    }
                }
            }
        }

        return score;
    }

    private KnowledgeSearchResult toSearchResult(KnowledgeDocument doc, double score) {
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setDocumentId(doc.getId());
        result.setTitle(doc.getTitle());
        result.setContent(doc.getContent());
        result.setCategory(doc.getCategory());
        result.setTags(doc.getTags());
        result.setScore(score);
        result.setSource(doc.getSource());
        return result;
    }

    private KnowledgeDocument toDocument(KnowledgeSearchResult result) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(result.getDocumentId());
        doc.setTitle(result.getTitle());
        doc.setContent(result.getContent());
        doc.setCategory(result.getCategory());
        doc.setTags(result.getTags());
        doc.setSource(result.getSource());
        return doc;
    }

    public List<KnowledgeDocument> getAllDocuments() {
        return new ArrayList<>(documentStore.values());
    }

    public int getDocumentCount() {
        return documentStore.size();
    }

    public Optional<KnowledgeDocument> getDocument(String id) {
        return Optional.ofNullable(documentStore.get(id));
    }

    public Optional<KnowledgeDocument> getDocumentById(String id) {
        return Optional.ofNullable(documentStore.get(id));
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalDocuments", documentStore.size());
        
        Map<String, Long> categoryCount = documentStore.values().stream()
                .collect(Collectors.groupingBy(
                        KnowledgeDocument::getCategory,
                        Collectors.counting()
                ));
        
        stats.put("categories", categoryCount);
        
        int totalRecords = documentStore.values().stream()
                .mapToInt(doc -> {
                    String content = doc.getContent();
                    return content.split("[。.!?；;]").length;
                })
                .sum();
        stats.put("totalRecords", totalRecords);
        
        return stats;
    }

    public List<String> getCategories() {
        return documentStore.values().stream()
                .map(KnowledgeDocument::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<KnowledgeDocument> getDocumentsByCategory(String category) {
        return documentStore.values().stream()
                .filter(doc -> doc.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Data
    private static class SearchHit {
        private final KnowledgeDocument doc;
        private final double score;
        
        public SearchHit(KnowledgeDocument doc, double score) {
            this.doc = doc;
            this.score = score;
        }
        
        public double getScore() {
            return score;
        }
    }
}
