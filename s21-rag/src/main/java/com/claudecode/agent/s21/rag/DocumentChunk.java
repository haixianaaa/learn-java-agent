package com.claudecode.agent.s21.rag;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DocumentChunk {
    private String id;
    private String documentId;
    private String content;
    private int startIndex;
    private int endIndex;
    private Map<String, String> metadata;
}
