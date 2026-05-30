package com.legalflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeSearchResult {
    private String documentId;
    private String title;
    private String content;
    private String category;
    private List<String> tags;
    private double score;
    private String source;
}
