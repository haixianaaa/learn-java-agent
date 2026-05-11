package com.claudecode.agent.s15.team;

import java.util.*;
import java.util.concurrent.*;

public class TeammateManager {
    private final Map<String, Teammate> teammates = new ConcurrentHashMap<>();
    private final MessageBus messageBus;

    public TeammateManager(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public Teammate spawn(String role, String name) {
        Teammate teammate = Teammate.create(name, role);
        teammates.put(teammate.getId(), teammate);
        return teammate;
    }

    public Teammate get(String id) {
        return teammates.get(id);
    }

    public List<Teammate> list() {
        return new ArrayList<>(teammates.values());
    }

    public void setStatus(String id, String status) {
        Teammate teammate = teammates.get(id);
        if (teammate != null) {
            teammates.put(id, teammate.withStatus(status));
        }
    }

    public void sendMessage(String from, String to, String content) {
        InboxMessage message = InboxMessage.create(from, to, content);
        messageBus.sendMessage(message);
    }

    public void sendShutdownRequest(String from, String to) {
        InboxMessage message = InboxMessage.shutdownRequest(from, to);
        messageBus.sendMessage(message);
    }

    public List<InboxMessage> readInbox(String teammateId) {
        return messageBus.readInbox(teammateId);
    }

    public String renderTeam() {
        if (teammates.isEmpty()) {
            return "No teammates.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Team members:\n");
        for (Teammate t : teammates.values()) {
            sb.append(String.format("  [%s] %s (%s): %s%n", 
                    t.getStatus(), t.getName(), t.getRole(), t.getId()));
        }
        return sb.toString();
    }
}
