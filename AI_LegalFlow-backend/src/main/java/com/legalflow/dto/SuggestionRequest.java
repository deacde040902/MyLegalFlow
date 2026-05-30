package com.legalflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class SuggestionRequest {
    private Clause clause;
    private List<Clause> clauses;
    private List<Risk> risks;
}
