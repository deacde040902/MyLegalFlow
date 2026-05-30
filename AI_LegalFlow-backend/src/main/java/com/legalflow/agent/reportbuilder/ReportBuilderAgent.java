package com.legalflow.agent.reportbuilder;

import com.legalflow.agent.BaseAgent;
import com.legalflow.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ReportBuilderAgent extends BaseAgent<ReportData, Map<String, Object>> {

    public ReportBuilderAgent() {
        super("ReportBuilderAgent");
    }

    @Override
    protected Map<String, Object> doExecute(ReportData input) {
        log.info("正在为任务生成报告: {}", input.getTaskId());

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", input.getTaskId());
        result.put("taskType", input.getTaskType());
        result.put("legalReferences", input.getLegalReferences());
        result.put("risks", input.getRisks());
        result.put("suggestions", input.getSuggestions());
        
        StringBuilder summary = new StringBuilder();
        summary.append("# 法律分析报告\n\n");
        summary.append("## 任务信息\n");
        summary.append("- 任务ID: ").append(input.getTaskId()).append("\n");
        summary.append("- 任务类型: ").append(input.getTaskType()).append("\n\n");

        if (input.getLegalReferences() != null && !input.getLegalReferences().isEmpty()) {
            summary.append("## 相关法规\n");
            for (LegalReference ref : input.getLegalReferences()) {
                summary.append("- ").append(ref.getLawName());
                if (ref.getArticleNumber() != null) {
                    summary.append(" ").append(ref.getArticleNumber());
                }
                summary.append("\n");
                if (ref.getArticleContent() != null) {
                    summary.append("  ").append(ref.getArticleContent()).append("\n");
                }
                summary.append("\n");
            }
        }

        result.put("summary", summary.toString());
        result.put("markdownContent", summary.toString());

        log.info("报告生成成功");
        return result;
    }

    public Map<String, Object> buildReport(ReportData reportData) {
        return execute(reportData);
    }
}
