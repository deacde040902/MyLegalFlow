package com.legalflow.service;

import com.legalflow.dto.KnowledgeDocument;
import com.legalflow.dto.KnowledgeSearchRequest;
import com.legalflow.dto.KnowledgeSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VectorSearchService {

    private final KnowledgeBaseService knowledgeBaseService;

    public VectorSearchService(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    public List<KnowledgeSearchResult> search(KnowledgeSearchRequest request) {
        log.info("执行知识库检索: query={}, category={}", 
                request.getQuery(), request.getCategory());

        List<KnowledgeDocument> allDocuments = knowledgeBaseService.getAllDocuments();
        
        // 简单的关键词匹配搜索
        List<KnowledgeSearchResult> results = new ArrayList<>();
        
        for (KnowledgeDocument doc : allDocuments) {
            // 检查分类过滤
            if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                if (!request.getCategory().equalsIgnoreCase(doc.getCategory())) {
                    continue;
                }
            }

            // 简单的关键词匹配
            boolean matches = false;
            if (doc.getTitle() != null && doc.getTitle().toLowerCase().contains(request.getQuery().toLowerCase())) {
                matches = true;
            }
            if (doc.getContent() != null && doc.getContent().toLowerCase().contains(request.getQuery().toLowerCase())) {
                matches = true;
            }

            if (matches) {
                KnowledgeSearchResult result = new KnowledgeSearchResult();
                result.setDocumentId(doc.getId());
                result.setTitle(doc.getTitle());
                result.setContent(doc.getContent());
                result.setCategory(doc.getCategory());
                result.setTags(doc.getTags());
                result.setScore(0.8); // 模拟分数
                result.setSource(doc.getSource());
                results.add(result);
            }
        }

        // 限制返回数量
        int topK = request.getTopK() > 0 ? request.getTopK() : 10;
        results = results.stream()
                .limit(topK)
                .collect(Collectors.toList());

        log.info("检索完成，返回 {} 条结果", results.size());
        return results;
    }

    public List<KnowledgeSearchResult> searchByCategory(String category, int topK) {
        KnowledgeSearchRequest request = new KnowledgeSearchRequest();
        request.setQuery("");
        request.setCategory(category);
        request.setTopK(topK);
        return search(request);
    }
}
