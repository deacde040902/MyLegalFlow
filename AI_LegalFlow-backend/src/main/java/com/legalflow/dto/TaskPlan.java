package com.legalflow.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskPlan {
    private String taskId;
    private String userMessage;
    private String taskType;
    private List<TaskStep> steps;
    private LocalDateTime createdAt;
    private String status;

    @Data
    public static class TaskStep {
        private int stepId;
        private String agent;
        private Object input;
        private List<Integer> dependsOn;
        private String status;
    }
}
