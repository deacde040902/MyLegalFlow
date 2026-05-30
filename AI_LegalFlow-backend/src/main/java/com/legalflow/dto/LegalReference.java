package com.legalflow.dto;

import lombok.Data;

@Data
public class LegalReference {
    private String lawId;
    private String lawName;
    private String articleNumber;
    private String content;
    private String source;
    private double relevanceScore;
    private String articleContent;
    private String category;
    private java.util.List<String> tags;
}
