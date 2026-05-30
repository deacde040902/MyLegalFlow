package com.legalflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeSearchRequest {
    private String query;
    private String category;
    private List<String> tags;
    private int topK;
    private double threshold;
}
