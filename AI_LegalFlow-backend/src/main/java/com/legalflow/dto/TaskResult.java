package com.legalflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResult {
    private String taskId;
    private String status;
    private String message;
    private Object data;
    private LocalDateTime completedAt;
}
