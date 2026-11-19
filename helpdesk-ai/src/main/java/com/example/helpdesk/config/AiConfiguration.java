package com.example.helpdesk.config;

import java.util.Arrays;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.helpdesk.service.ToolCallTracker;

/** 設定聊天模型及遠端 MCP 工具的整合方式。 */
@Configuration
public class AiConfiguration {

    private final double temperature;

    public AiConfiguration(@Value("${helpdesk.chat.temperature:0.4}") double temperature) {
        this.temperature = temperature;
    }

    /**
     * 建立具備 MCP 報修查詢工具的聊天用戶端。
     * MCP 工具由獨立的報修 MCP Server 提供，透過 HTTP 連線。
     */
    @Bean
    ChatClient helpdeskChatClient(
            ChatClient.Builder builder,
            ToolCallbackProvider ticketServerTools,
            ToolCallTracker tracker) {
        ToolCallbackProvider trackedTools = () -> Arrays.stream(ticketServerTools.getToolCallbacks())
                .filter(tool -> tool != null)
                .map(tool -> new TrackingToolCallback(tool, tracker))
                .toArray(ToolCallback[]::new);

        return builder
                .defaultOptions(GoogleGenAiChatOptions.builder().temperature(temperature))
                .defaultTools(trackedTools)
                .build();
    }
}


