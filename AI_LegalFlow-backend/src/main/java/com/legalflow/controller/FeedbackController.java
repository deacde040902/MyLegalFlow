package com.legalflow.controller;

import com.legalflow.entity.Feedback;
import com.legalflow.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @PostMapping("/{taskId}")
    public ResponseEntity<Feedback> createFeedback(
            @PathVariable String taskId,
            @RequestBody Feedback feedback) {
        log.info("收到任务反馈，任务ID: {}, 反馈: {}", taskId, feedback);

        feedback.setTaskId(taskId);
        feedback.setCreatedAt(LocalDateTime.now());
        Feedback saved = feedbackRepository.save(feedback);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<List<Feedback>> getFeedback(@PathVariable String taskId) {
        log.info("获取任务反馈，任务ID: {}", taskId);
        List<Feedback> feedback = feedbackRepository.findByTaskId(taskId);
        return ResponseEntity.ok(feedback);
    }
}
