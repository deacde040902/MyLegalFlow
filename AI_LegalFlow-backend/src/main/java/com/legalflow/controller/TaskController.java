package com.legalflow.controller;

import com.legalflow.dto.TaskResult;
import com.legalflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResult> createTask(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String taskType = request.getOrDefault("taskType", "CONTRACT_REVIEW");
        log.info("收到任务创建请求: {}", request);

        TaskResult result = taskService.createTask(message, taskType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResult> getTask(@PathVariable String taskId) {
        log.info("获取任务: {}", taskId);
        return taskService.getTaskResult(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<TaskResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskType", defaultValue = "CONTRACT_REVIEW") String taskType) {
        log.info("收到文件上传: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        // TODO: 实现文件上传处理逻辑
        TaskResult result = new TaskResult();
        result.setStatus("UPLOADED");
        result.setMessage("文件已上传，等待处理");
        return ResponseEntity.ok(result);
    }
}
