package com.claudecode.agent.s17.autonomous;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AgentTask {
    private String id;
    private String subject;
    private String description;
    private String status;
    private String assignee;
    private Instant createdAt;

    public static AgentTask create(String subject, String description) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new AgentTask(id, subject, description, "pending", null, Instant.now());
    }

    public AgentTask withAssignee(String newAssignee) {
        return new AgentTask(id, subject, description, "assigned", newAssignee, createdAt);
    }

    public AgentTask complete() {
        return new AgentTask(id, subject, description, "completed", assignee, createdAt);
    }
}
