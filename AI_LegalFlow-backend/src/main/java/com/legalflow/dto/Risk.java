package com.legalflow.dto;

import lombok.Data;

@Data
public class Risk {
    private String id;
    private String description;
    private String severity;
    private String clauseReference;
    private String legalBasis;
    private String recommendation;
}
