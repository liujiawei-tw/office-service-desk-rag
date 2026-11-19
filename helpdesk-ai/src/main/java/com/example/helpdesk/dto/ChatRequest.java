package com.example.helpdesk.dto;

/** POST /api/chat 的請求格式。 */
public record ChatRequest(String question, String conversationId) {
}
