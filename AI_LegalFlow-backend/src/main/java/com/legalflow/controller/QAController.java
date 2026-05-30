package com.legalflow.controller;

import com.legalflow.dto.QAResponse;
import com.legalflow.service.ConversationService;
import com.legalflow.service.QAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QAController {

    private final QAService qaService;
    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<QAResponse> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String taskType = request.getOrDefault("taskType", "LEGAL_QA");
        String conversationId = request.get("conversationId");
        
        log.info("收到问答请求: {}", question);
        
        String context = "";
        if (conversationId != null && !conversationId.isEmpty()) {
            context = conversationService.getContext(conversationId);
        }
        
        QAResponse response = qaService.answerQuestionWithContext(question, taskType, context);
        
        if (conversationId != null && !conversationId.isEmpty()) {
            conversationService.addMessage(conversationId, "user", question);
            conversationService.addMessage(conversationId, "assistant", response.getAnswer());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<QAResponse> askQuestionGet(
            @RequestParam String question,
            @RequestParam(defaultValue = "LEGAL_QA") String taskType) {
        
        log.info("收到问答请求(GET): {}", question);
        
        QAResponse response = qaService.answerQuestion(question, taskType);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QAResponse> askQuestionWithFiles(
            @RequestParam String question,
            @RequestParam(defaultValue = "LEGAL_QA") String taskType,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        
        log.info("收到带文件的问答请求: {}, 文件数量: {}", question, files != null ? files.length : 0);
        
        String context = "";
        if (conversationId != null && !conversationId.isEmpty()) {
            context = conversationService.getContext(conversationId);
        }
        
        QAResponse response = qaService.answerQuestionWithFilesAndContext(
                question, taskType, files != null ? List.of(files) : null, context);
        
        if (conversationId != null && !conversationId.isEmpty()) {
            conversationService.addMessage(conversationId, "user", question);
            conversationService.addMessage(conversationId, "assistant", response.getAnswer());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversation/create")
    public ResponseEntity<Map<String, String>> createConversation() {
        String conversationId = conversationService.createConversation();
        return ResponseEntity.ok(Map.of("conversationId", conversationId));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<ConversationService.Message>> getConversation(
            @PathVariable String conversationId) {
        List<ConversationService.Message> messages = conversationService.getMessages(conversationId);
        return ResponseEntity.ok(messages);
    }
}
