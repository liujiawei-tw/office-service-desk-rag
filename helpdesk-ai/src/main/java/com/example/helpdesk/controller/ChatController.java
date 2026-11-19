package com.example.helpdesk.controller;

import com.example.helpdesk.dto.ChatRequest;
import com.example.helpdesk.dto.ChatResponse;
import com.example.helpdesk.service.HelpdeskChatService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** 提供員工使用的聊天 REST API。 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final HelpdeskChatService chatService;

    public ChatController(HelpdeskChatService chatService) {
        this.chatService = chatService;
    }

    /** 接收自然語言問題並回傳 RAG/MCP 整合後的客服答案。 */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不可空白");
        }
        if (request.conversationId() != null && request.conversationId().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId 不可超過 100 個字元");
        }
        return chatService.answerWithMetadata(request.question().trim(), request.conversationId());
    }
}
