package com.legalflow.dto;

import lombok.Data;

@Data
public class LawRetrievalQuery {
    private String queryText;
    private String context;
    private String lawCategory;
    private int maxResults;
}
