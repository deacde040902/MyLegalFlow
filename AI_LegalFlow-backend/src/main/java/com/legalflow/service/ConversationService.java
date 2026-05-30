package com.legalflow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ConversationService {

    private static class Conversation {
        String conversationId;
        List<Message> messages = new ArrayList<>();
        long createdAt;
        long lastUpdatedAt;

        Conversation(String conversationId) {
            this.conversationId = conversationId;
            this.createdAt = System.currentTimeMillis();
            this.lastUpdatedAt = this.createdAt;
        }

        void addMessage(Message message) {
            messages.add(message);
            lastUpdatedAt = System.currentTimeMillis();
        }

        String getContext() {
            StringBuilder context = new StringBuilder();
            int maxMessages = Math.min(messages.size(), 5);
            int startIndex = messages.size() - maxMessages;
            
            for (int i = startIndex; i < messages.size(); i++) {
                Message msg = messages.get(i);
                context.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            
            return context.toString().trim();
        }

        List<Message> getMessages() {
            return messages;
        }
    }

    public static class Message {
        private String role;
        private String content;
        private long timestamp;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }

    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private static final long MAX_AGE_MS = 30 * 60 * 1000;

    public String createConversation() {
        String conversationId = UUID.randomUUID().toString();
        conversations.put(conversationId, new Conversation(conversationId));
        log.info("创建新对话: {}", conversationId);
        return conversationId;
    }

    public void addMessage(String conversationId, String role, String content) {
        cleanupOldConversations();
        
        Conversation conversation = conversations.computeIfAbsent(conversationId, Conversation::new);
        conversation.addMessage(new Message(role, content));
        log.debug("添加消息到对话 {}: {} - {}", conversationId, role, content.length());
    }

    public String getContext(String conversationId) {
        cleanupOldConversations();
        
        Conversation conversation = conversations.get(conversationId);
        if (conversation == null) {
            return "";
        }
        return conversation.getContext();
    }

    public List<Message> getMessages(String conversationId) {
        cleanupOldConversations();
        
        Conversation conversation = conversations.get(conversationId);
        if (conversation == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(conversation.getMessages());
    }

    public boolean conversationExists(String conversationId) {
        cleanupOldConversations();
        return conversations.containsKey(conversationId);
    }

    private void cleanupOldConversations() {
        long now = System.currentTimeMillis();
        conversations.entrySet().removeIf(entry -> 
            now - entry.getValue().lastUpdatedAt > MAX_AGE_MS
        );
    }

    public void deleteConversation(String conversationId) {
        conversations.remove(conversationId);
        log.info("删除对话: {}", conversationId);
    }
}
