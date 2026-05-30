package com.legalflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class QAResponse {
    private boolean success;
    private String question;
    private String answer;
    private List<LegalReference> legalReferences;
    private String responseTime;
}
