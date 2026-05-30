package com.legalflow.dto;

import lombok.Data;

@Data
public class DocumentInput {
    private String documentUrl;
    private byte[] documentContent;
    private String documentType;
    private String fileName;
}
