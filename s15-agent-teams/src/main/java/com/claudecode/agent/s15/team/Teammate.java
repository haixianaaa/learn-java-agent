package com.claudecode.agent.s15.team;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Teammate {
    private String id;
    private String name;
    private String role;
    private String status;
    private Instant createdAt;

    public static Teammate create(String name, String role) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new Teammate(id, name, role, "idle", Instant.now());
    }

    public Teammate withStatus(String newStatus) {
        return new Teammate(id, name, role, newStatus, createdAt);
    }
}
