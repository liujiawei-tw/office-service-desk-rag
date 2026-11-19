package com.example.helpdesk.dto;

import java.util.List;

/** POST /api/chat 的回應格式。 */
public record ChatResponse(
        String answer,
        List<String> sources,
        List<String> toolsUsed,
        List<RetrievedChunk> retrievedChunks) {

    public ChatResponse(String answer) {
        this(answer, List.of(), List.of(), List.of());
    }

    public ChatResponse(String answer, List<String> sources, List<String> toolsUsed) {
        this(answer, sources, toolsUsed, List.of());
    }
}
