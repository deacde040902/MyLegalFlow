package com.legalflow.dto;

import lombok.Data;

@Data
public class Suggestion {
    private String clauseName;
    private String originalText;
    private String suggestedText;
    private String alternativeText;
    private String reason;
}
