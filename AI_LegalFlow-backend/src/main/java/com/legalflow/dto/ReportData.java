package com.legalflow.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportData {
    private String taskId;
    private String taskType;
    private List<Clause> clauses;
    private List<LegalReference> legalReferences;
    private List<Risk> risks;
    private List<Suggestion> suggestions;
    private LocalDateTime generatedAt;
}
