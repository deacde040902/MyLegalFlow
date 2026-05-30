package com.legalflow.controller;

import com.legalflow.dto.KnowledgeDocument;
import com.legalflow.dto.KnowledgeSearchRequest;
import com.legalflow.dto.KnowledgeSearchResult;
import com.legalflow.service.KnowledgeBaseService;
import com.legalflow.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final VectorSearchService vectorSearchService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", knowledgeBaseService.getDocumentCount());
        stats.put("documents", knowledgeBaseService.getAllDocuments().stream()
                .map(doc -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", doc.getId());
                    info.put("title", doc.getTitle());
                    info.put("category", doc.getCategory());
                    info.put("source", doc.getSource());
                    return info;
                })
                .toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/documents")
    public ResponseEntity<List<KnowledgeDocument>> getAllDocuments() {
        return ResponseEntity.ok(knowledgeBaseService.getAllDocuments());
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<KnowledgeDocument> getDocument(@PathVariable String id) {
        Optional<KnowledgeDocument> doc = knowledgeBaseService.getDocument(id);
        return doc.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeSearchResult>> search(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(defaultValue = "0.01") double threshold) {

        KnowledgeSearchRequest request = new KnowledgeSearchRequest();
        request.setQuery(query);
        request.setCategory(category);
        request.setTopK(topK);
        request.setThreshold(threshold);

        List<KnowledgeSearchResult> results = vectorSearchService.search(request);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/search")
    public ResponseEntity<List<KnowledgeSearchResult>> searchPost(@RequestBody KnowledgeSearchRequest request) {
        if (request.getTopK() <= 0) {
            request.setTopK(10);
        }
        if (request.getThreshold() <= 0) {
            request.setThreshold(0.01);
        }
        List<KnowledgeSearchResult> results = vectorSearchService.search(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = knowledgeBaseService.getAllDocuments().stream()
                .map(KnowledgeDocument::getCategory)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(categories);
    }
}
