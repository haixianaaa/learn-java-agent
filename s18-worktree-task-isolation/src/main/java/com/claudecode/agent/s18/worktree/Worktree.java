package com.claudecode.agent.s18.worktree;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Worktree {
    private String id;
    private String name;
    private Path path;
    private String branch;
    private String status;
    private Instant createdAt;

    public static Worktree create(String name, Path basePath) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Path worktreePath = basePath.resolve(name);
        return new Worktree(id, name, worktreePath, name, "active", Instant.now());
    }

    public Worktree withStatus(String newStatus) {
        return new Worktree(id, name, path, branch, newStatus, createdAt);
    }
}
