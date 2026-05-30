package com.legalflow.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LawDatabaseTool {

    public List<Map<String, Object>> searchLaws(String query, int topK) {
        log.info("正在搜索法律法规，查询词: {}, topK: {}", query, topK);

        // TODO: Milvus向量数据库集成已暂时禁用
        // 1. 将查询文本转换为向量
        // 2. 在Milvus中执行相似度搜索
        // 3. 返回相关法律条文

        return List.of();
    }

    public boolean isConnected() {
        return false;
    }
}
