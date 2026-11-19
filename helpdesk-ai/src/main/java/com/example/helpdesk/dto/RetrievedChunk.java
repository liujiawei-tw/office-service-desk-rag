package com.example.helpdesk.dto;

/** A retrieved knowledge chunk and whether it was included in the model context. */
public record RetrievedChunk(String sourceFile, String chunkId, Double score, boolean selectedForContext) {
}
