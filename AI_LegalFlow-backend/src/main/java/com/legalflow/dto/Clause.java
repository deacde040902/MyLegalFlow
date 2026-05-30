package com.legalflow.dto;

import lombok.Data;

@Data
public class Clause {
    private String id;
    private String name;
    private String content;
    private String category;
    private int orderIndex;
    private String rawText;
}
