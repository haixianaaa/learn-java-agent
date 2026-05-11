package com.claudecode.agent.s12.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String content;
    private String priority;
    private String status;
    private String summary;
    private Instant createdAt;
    private Instant updatedAt;

    public static Task create(String content, String priority) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Instant now = Instant.now();
        return new Task(id, content, priority, "pending", null, now, now);
    }

    public Task withStatus(String newStatus) {
        return new Task(id, content, priority, newStatus, summary, createdAt, Instant.now());
    }

    public Task withSummary(String newSummary) {
        return new Task(id, content, priority, status, newSummary, createdAt, Instant.now());
    }

    @Override
    public String toString() {
        String marker = switch (status) {
            case "completed" -> "[x]";
            case "in_progress" -> "[>]";
            default -> "[ ]";
        };
        return String.format("%s %s (%s) - %s", marker, content, priority, id);
    }
}
