package com.example.helpdesk.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

/** Captures the tool callbacks actually invoked during one synchronous chat request. */
@Component
public class ToolCallTracker {

    private final ThreadLocal<Set<String>> activeCalls = new ThreadLocal<>();

    public <T> Capture<T> capture(Supplier<T> operation) {
        Set<String> previousCalls = activeCalls.get();
        Set<String> requestCalls = new LinkedHashSet<>();
        activeCalls.set(requestCalls);
        try {
            return new Capture<>(operation.get(), List.copyOf(requestCalls));
        } finally {
            if (previousCalls == null) {
                activeCalls.remove();
            } else {
                activeCalls.set(previousCalls);
            }
        }
    }

    public void record(String toolName) {
        Set<String> requestCalls = activeCalls.get();
        if (requestCalls != null && toolName != null && !toolName.isBlank()) {
            requestCalls.add(toolName);
        }
    }

    public record Capture<T>(T value, List<String> toolNames) {
    }
}
