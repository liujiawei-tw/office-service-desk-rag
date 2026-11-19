package com.example.helpdesk.config;

import com.example.helpdesk.service.ToolCallTracker;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/** Delegates a tool callback and records its name when it is actually invoked. */
final class TrackingToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final ToolCallTracker tracker;

    TrackingToolCallback(ToolCallback delegate, ToolCallTracker tracker) {
        this.delegate = delegate;
        this.tracker = tracker;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        tracker.record(getToolDefinition().name());
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        tracker.record(getToolDefinition().name());
        return delegate.call(toolInput, toolContext);
    }
}
