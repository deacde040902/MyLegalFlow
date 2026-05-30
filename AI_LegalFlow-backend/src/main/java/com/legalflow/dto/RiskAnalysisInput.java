package com.legalflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class RiskAnalysisInput {
    private List<Clause> clauses;
    private List<LegalReference> legalReferences;
}
