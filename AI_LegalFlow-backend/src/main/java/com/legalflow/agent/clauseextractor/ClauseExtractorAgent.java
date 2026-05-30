package com.legalflow.agent.clauseextractor;

import com.legalflow.agent.BaseAgent;
import com.legalflow.dto.Clause;
import com.legalflow.dto.DocumentInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClauseExtractorAgent extends BaseAgent<DocumentInput, List<Clause>> {

    public ClauseExtractorAgent() {
        super("ClauseExtractorAgent");
    }

    @Override
    protected List<Clause> doExecute(DocumentInput input) {
        log.info("正在从文档中提取条款: {}", input.getDocumentUrl());

        // TODO: 实现文档解析逻辑
        // 1. 使用Apache Tika解析文档
        // 2. 提取文本内容
        // 3. 使用LLM识别条款结构
        // 4. 返回结构化条款列表

        List<Clause> clauses = new ArrayList<>();

        // 临时返回空列表，等待后续实现
        log.info("从文档中提取到 {} 条条款", clauses.size());
        return clauses;
    }

    public List<Clause> extractClauses(String documentUrl, byte[] documentContent) {
        DocumentInput input = new DocumentInput();
        input.setDocumentUrl(documentUrl);
        input.setDocumentContent(documentContent);
        return execute(input);
    }
}
