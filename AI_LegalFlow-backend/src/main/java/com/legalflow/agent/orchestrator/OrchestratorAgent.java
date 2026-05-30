package com.legalflow.agent.orchestrator;

import com.legalflow.agent.BaseAgent;
import com.legalflow.agent.clauseextractor.ClauseExtractorAgent;
import com.legalflow.agent.lawretriever.LawRetrieverAgent;
import com.legalflow.agent.reportbuilder.ReportBuilderAgent;
import com.legalflow.agent.riskanalyzer.RiskAnalyzerAgent;
import com.legalflow.agent.suggestiongenerator.SuggestionGeneratorAgent;
import com.legalflow.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class OrchestratorAgent extends BaseAgent<TaskPlan, TaskResult> {

    private final ClauseExtractorAgent clauseExtractorAgent;
    private final LawRetrieverAgent lawRetrieverAgent;
    private final RiskAnalyzerAgent riskAnalyzerAgent;
    private final SuggestionGeneratorAgent suggestionGeneratorAgent;
    private final ReportBuilderAgent reportBuilderAgent;

    public OrchestratorAgent(ClauseExtractorAgent clauseExtractorAgent, 
                            LawRetrieverAgent lawRetrieverAgent,
                            RiskAnalyzerAgent riskAnalyzerAgent,
                            SuggestionGeneratorAgent suggestionGeneratorAgent,
                            ReportBuilderAgent reportBuilderAgent) {
        super("协调者Agent");
        this.clauseExtractorAgent = clauseExtractorAgent;
        this.lawRetrieverAgent = lawRetrieverAgent;
        this.riskAnalyzerAgent = riskAnalyzerAgent;
        this.suggestionGeneratorAgent = suggestionGeneratorAgent;
        this.reportBuilderAgent = reportBuilderAgent;
    }

    @Override
    protected TaskResult doExecute(TaskPlan taskPlan) {
        log.info("协调者正在解析任务计划: {}", taskPlan.getTaskId());
        return coordinateExecution(taskPlan);
    }

    public TaskResult coordinateExecution(TaskPlan taskPlan) {
        log.info("开始协调执行任务: {}", taskPlan.getTaskId());

        TaskResult finalResult = new TaskResult();
        finalResult.setTaskId(taskPlan.getTaskId());

        try {
            Map<String, Object> executionContext = new HashMap<>();
            executionContext.put("taskId", taskPlan.getTaskId());
            executionContext.put("userMessage", taskPlan.getUserMessage());
            executionContext.put("taskType", taskPlan.getTaskType());

            if ("LEGAL_QA".equals(taskPlan.getTaskType()) || 
                "LAW_RETRIEVAL".equals(taskPlan.getTaskType())) {
                log.info("执行简单问答流程");
                
                LawRetrievalQuery lawQuery = new LawRetrievalQuery();
                lawQuery.setQueryText(taskPlan.getUserMessage());
                lawQuery.setMaxResults(5);
                
                List<LegalReference> references = lawRetrieverAgent.execute(lawQuery);
                
                ReportData reportData = new ReportData();
                reportData.setTaskId(taskPlan.getTaskId());
                reportData.setTaskType(taskPlan.getTaskType());
                reportData.setLegalReferences(references);
                reportData.setClauses(Collections.emptyList());
                reportData.setRisks(Collections.emptyList());
                reportData.setSuggestions(Collections.emptyList());
                
                Map<String, Object> report = reportBuilderAgent.execute(reportData);
                
                finalResult.setStatus("COMPLETED");
                finalResult.setMessage("法律问答完成");
                finalResult.setData(report);
                
            } else if ("CONTRACT_REVIEW".equals(taskPlan.getTaskType())) {
                log.info("执行合同审查流程");
                
                LawRetrievalQuery lawQuery = new LawRetrievalQuery();
                lawQuery.setQueryText(taskPlan.getUserMessage());
                lawQuery.setMaxResults(5);
                
                List<LegalReference> references = lawRetrieverAgent.execute(lawQuery);
                
                ReportData reportData = new ReportData();
                reportData.setTaskId(taskPlan.getTaskId());
                reportData.setTaskType(taskPlan.getTaskType());
                reportData.setLegalReferences(references);
                reportData.setClauses(Collections.emptyList());
                reportData.setRisks(Collections.emptyList());
                reportData.setSuggestions(Collections.emptyList());
                
                Map<String, Object> report = reportBuilderAgent.execute(reportData);
                
                finalResult.setStatus("COMPLETED");
                finalResult.setMessage("合同审查完成（基础版本）");
                finalResult.setData(report);
                
            } else {
                log.info("执行默认流程");
                
                LawRetrievalQuery lawQuery = new LawRetrievalQuery();
                lawQuery.setQueryText(taskPlan.getUserMessage());
                lawQuery.setMaxResults(5);
                
                List<LegalReference> references = lawRetrieverAgent.execute(lawQuery);
                
                ReportData reportData = new ReportData();
                reportData.setTaskId(taskPlan.getTaskId());
                reportData.setTaskType(taskPlan.getTaskType());
                reportData.setLegalReferences(references);
                
                Map<String, Object> report = reportBuilderAgent.execute(reportData);
                
                finalResult.setStatus("COMPLETED");
                finalResult.setMessage("任务完成");
                finalResult.setData(report);
            }
            
            log.info("任务 {} 执行完成", taskPlan.getTaskId());
            return finalResult;

        } catch (Exception e) {
            log.error("任务执行失败: {}", taskPlan.getTaskId(), e);
            finalResult.setStatus("FAILED");
            finalResult.setMessage("任务执行失败: " + e.getMessage());
            return finalResult;
        }
    }
}
