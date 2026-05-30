package com.legalflow.agent.suggestiongenerator;

import com.legalflow.agent.BaseAgent;
import com.legalflow.dto.Risk;
import com.legalflow.dto.Suggestion;
import com.legalflow.dto.SuggestionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SuggestionGeneratorAgent extends BaseAgent<SuggestionRequest, List<Suggestion>> {

    public SuggestionGeneratorAgent() {
        super("SuggestionGeneratorAgent");
    }

    @Override
    protected List<Suggestion> doExecute(SuggestionRequest request) {
        log.info("正在为 {} 个风险点生成修改建议", request.getRisks().size());

        List<Suggestion> suggestions = new ArrayList<>();

        // TODO: 实现修改建议生成逻辑
        // 1. 根据风险类型选择修改策略
        // 2. 生成多种备选方案
        // 3. 提供修改后的条款文本

        // 临时返回空列表，等待后续实现
        log.info("生成了 {} 条修改建议", suggestions.size());
        return suggestions;
    }

    public List<Suggestion> generateSuggestions(List<Risk> risks) {
        SuggestionRequest request = new SuggestionRequest();
        request.setRisks(risks);
        return execute(request);
    }
}
