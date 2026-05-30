package com.legalflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    @Value("${milvus.collection-name:legal-documents}")
    private String collectionName;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
