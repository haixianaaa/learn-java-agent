package com.claudecode.agent.s21.rag;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Document {
    private String id;
    private String content;
    private Map<String, String> metadata;
}
