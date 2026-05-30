package com.legalflow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeDocument {
    private String id;
    private String title;
    private String content;
    private String category;
    private String source;
    private String sourceType;
    private List<String> tags;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private float[] embedding;
}
