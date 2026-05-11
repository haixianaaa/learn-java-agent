package com.claudecode.agent.s13.background;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BackgroundTask {
    private String id;
    private String command;
    private String status;
    private String output;
    private Instant startTime;
    private Instant endTime;

    public static BackgroundTask create(String command) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new BackgroundTask(id, command, "running", null, Instant.now(), null);
    }

    public BackgroundTask complete(String output) {
        return new BackgroundTask(id, command, "completed", output, startTime, Instant.now());
    }

    public BackgroundTask fail(String error) {
        return new BackgroundTask(id, command, "failed", error, startTime, Instant.now());
    }
}
