package com.claudecode.agent.s15.team;

import java.util.*;
import java.util.concurrent.*;

public class MessageBus {
    private final Map<String, List<InboxMessage>> inboxes = new ConcurrentHashMap<>();

    public void sendMessage(InboxMessage message) {
        inboxes.computeIfAbsent(message.getTo(), k -> new CopyOnWriteArrayList<>()).add(message);
    }

    public List<InboxMessage> readInbox(String teammateId) {
        List<InboxMessage> messages = new ArrayList<>(inboxes.getOrDefault(teammateId, Collections.emptyList()));
        return messages;
    }

    public void clearInbox(String teammateId) {
        inboxes.remove(teammateId);
    }

    public boolean hasMessages(String teammateId) {
        List<InboxMessage> messages = inboxes.get(teammateId);
        return messages != null && !messages.isEmpty();
    }
}
