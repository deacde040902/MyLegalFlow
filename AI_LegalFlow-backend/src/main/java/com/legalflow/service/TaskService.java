package com.legalflow.service;

import com.legalflow.agent.orchestrator.OrchestratorAgent;
import com.legalflow.dto.TaskPlan;
import com.legalflow.dto.TaskResult;
import com.legalflow.entity.Task;
import com.legalflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final OrchestratorAgent orchestratorAgent;

    public TaskResult createTask(String userMessage, String taskType) {
        log.info("创建新任务: {}, 类型: {}", userMessage, taskType);

        String taskId = UUID.randomUUID().toString();

        Task task = new Task();
        task.setTaskId(taskId);
        task.setUserMessage(userMessage);
        task.setTaskType(taskType);
        task.setStatus("CREATED");
        task.setCreatedAt(LocalDateTime.now());
        
        taskRepository.save(task);

        TaskPlan taskPlan = createTaskPlan(taskId, userMessage, taskType);
        
        TaskResult result = new TaskResult();
        result.setTaskId(taskId);
        result.setStatus("EXECUTING");
        result.setMessage("任务正在执行中...");
        result.setData(taskPlan);

        try {
            TaskResult executionResult = executeTask(taskId);
            return executionResult;
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            TaskResult failedResult = new TaskResult();
            failedResult.setTaskId(taskId);
            failedResult.setStatus("FAILED");
            failedResult.setMessage("任务执行失败: " + e.getMessage());
            return failedResult;
        }
    }

    private TaskPlan createTaskPlan(String taskId, String userMessage, String taskType) {
        TaskPlan taskPlan = new TaskPlan();
        taskPlan.setTaskId(taskId);
        taskPlan.setUserMessage(userMessage);
        taskPlan.setTaskType(taskType);
        taskPlan.setCreatedAt(LocalDateTime.now());
        taskPlan.setStatus("CREATED");

        List<TaskPlan.TaskStep> steps = generateTaskSteps(taskType, userMessage);
        taskPlan.setSteps(steps);

        return taskPlan;
    }

    private List<TaskPlan.TaskStep> generateTaskSteps(String taskType, String userMessage) {
        List<TaskPlan.TaskStep> steps = new java.util.ArrayList<>();

        switch (taskType) {
            case "CONTRACT_REVIEW":
                steps.add(createStep(1, "ClauseExtractorAgent", 
                        java.util.Map.of("message", userMessage), null));
                steps.add(createStep(2, "LawRetrieverAgent", 
                        java.util.Map.of("query", extractKeywords(userMessage)), java.util.List.of(1)));
                steps.add(createStep(3, "RiskAnalyzerAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(1, 2)), java.util.List.of(1, 2)));
                steps.add(createStep(4, "SuggestionGeneratorAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(3)), java.util.List.of(3)));
                steps.add(createStep(5, "ReportBuilderAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(4)), java.util.List.of(4)));
                break;
                
            case "LEGAL_QA":
                steps.add(createStep(1, "LawRetrieverAgent", 
                        java.util.Map.of("query", userMessage), null));
                steps.add(createStep(2, "ReportBuilderAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(1)), java.util.List.of(1)));
                break;
                
            case "LAW_RETRIEVAL":
                steps.add(createStep(1, "LawRetrieverAgent", 
                        java.util.Map.of("query", userMessage), null));
                steps.add(createStep(2, "ReportBuilderAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(1)), java.util.List.of(1)));
                break;
                
            default:
                steps.add(createStep(1, "LawRetrieverAgent", 
                        java.util.Map.of("query", userMessage), null));
                steps.add(createStep(2, "ReportBuilderAgent", 
                        java.util.Map.of("dependsOn", java.util.List.of(1)), java.util.List.of(1)));
                break;
        }

        return steps;
    }

    private TaskPlan.TaskStep createStep(int stepId, String agent, Object input, List<Integer> dependsOn) {
        TaskPlan.TaskStep step = new TaskPlan.TaskStep();
        step.setStepId(stepId);
        step.setAgent(agent);
        step.setInput(input);
        step.setDependsOn(dependsOn);
        step.setStatus("PENDING");
        return step;
    }

    private String extractKeywords(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        String[] keywords = {"竞业限制", "违约金", "试用期", "解除合同", "赔偿", 
                           "加班", "工资", "保险", "保密", "培训"};
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return keyword;
            }
        }
        
        return message.length() > 20 ? message.substring(0, 20) : message;
    }

    public TaskResult executeTask(String taskId) {
        log.info("执行任务: {}", taskId);
        
        Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        Task task = taskOpt.get();
        
        TaskPlan taskPlan = createTaskPlan(taskId, task.getUserMessage(), task.getTaskType());
        
        task.setStatus("EXECUTING");
        taskRepository.save(task);
        
        try {
            TaskResult result = orchestratorAgent.coordinateExecution(taskPlan);
            
            task.setStatus("COMPLETED");
            task.setResultMessage(result.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            
            return result;
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            
            task.setStatus("FAILED");
            task.setResultMessage("任务执行失败: " + e.getMessage());
            taskRepository.save(task);
            
            throw e;
        }
    }

    public Optional<TaskResult> getTaskResult(String taskId) {
        log.info("查询任务结果: {}", taskId);
        
        Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        
        return taskOpt.map(task -> {
            TaskResult result = new TaskResult();
            result.setTaskId(task.getTaskId());
            result.setStatus(task.getStatus());
            result.setMessage(task.getResultMessage());
            result.setCompletedAt(task.getCompletedAt());
            return result;
        });
    }

    public List<TaskResult> getAllTasks() {
        log.info("查询所有任务");
        
        return taskRepository.findAll().stream()
                .map(task -> {
                    TaskResult result = new TaskResult();
                    result.setTaskId(task.getTaskId());
                    result.setStatus(task.getStatus());
                    result.setMessage(task.getUserMessage());
                    result.setCompletedAt(task.getCompletedAt());
                    return result;
                })
                .collect(Collectors.toList());
    }

    public void deleteTask(String taskId) {
        log.info("删除任务: {}", taskId);
        
        Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        taskOpt.ifPresent(task -> {
            taskRepository.delete(task);
            log.info("任务已删除: {}", taskId);
        });
    }
}
