package com.claudecode.agent.s17.autonomous;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AutonomousAgent {
    private String id;
    private String name;
    private String role;
    private String status;
    private String systemPrompt;
    private int maxRounds;
    private Instant createdAt;

    public static AutonomousAgent create(String name, String role, String systemPrompt) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new AutonomousAgent(id, name, role, "idle", systemPrompt, 30, Instant.now());
    }

    public AutonomousAgent withStatus(String newStatus) {
        return new AutonomousAgent(id, name, role, newStatus, systemPrompt, maxRounds, createdAt);
    }

    public AutonomousAgent working() {
        return withStatus("working");
    }

    public AutonomousAgent idle() {
        return withStatus("idle");
    }

    public AutonomousAgent shutdown() {
        return withStatus("shutdown");
    }
}
