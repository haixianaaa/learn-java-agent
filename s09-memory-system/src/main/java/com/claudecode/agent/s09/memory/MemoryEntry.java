package com.claudecode.agent.s09.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryEntry {
    private String name;
    private String description;
    private MemoryType memoryType;
    private String content;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(name).append(": ").append(description).append("\n");
        if (content != null && !content.trim().isEmpty()) {
            sb.append(content.trim()).append("\n");
        }
        return sb.toString();
    }
}
