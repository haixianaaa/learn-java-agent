package com.claudecode.agent.s15.team;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InboxMessage {
    private String id;
    private String from;
    private String to;
    private String content;
    private String messageType;
    private Instant timestamp;

    public static InboxMessage create(String from, String to, String content) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new InboxMessage(id, from, to, content, "text", Instant.now());
    }

    public static InboxMessage shutdownRequest(String from, String to) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new InboxMessage(id, from, to, "shutdown", "shutdown_request", Instant.now());
    }
}
