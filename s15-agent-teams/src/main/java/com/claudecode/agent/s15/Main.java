package com.claudecode.agent.s15;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("S15 - Agent Teams");
        
        TeammateManager teamManager = new TeammateManager();
        
        Teammate agent1 = teamManager.spawn("researcher", "Research agent");
        Teammate agent2 = teamManager.spawn("coder", "Coding agent");
        
        System.out.println("Team members:");
        teamManager.list().forEach(t -> System.out.println("  " + t));
        
        teamManager.sendMessage(agent1.id(), agent2.id(), "Please implement the feature");
        
        List<Message> inbox = teamManager.readInbox(agent2.id());
        System.out.println("\nAgent2 inbox:");
        inbox.forEach(m -> System.out.println("  From " + m.from() + ": " + m.content()));
    }
}

record Teammate(String id, String name, String role) {}

record Message(String id, String from, String to, String content, long timestamp) {}

class TeammateManager {
    private final Map<String, Teammate> teammates = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> inboxes = new ConcurrentHashMap<>();
    
    public Teammate spawn(String role, String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Teammate teammate = new Teammate(id, name, role);
        teammates.put(id, teammate);
        inboxes.put(id, new CopyOnWriteArrayList<>());
        return teammate;
    }
    
    public List<Teammate> list() {
        return new ArrayList<>(teammates.values());
    }
    
    public void sendMessage(String from, String to, String content) {
        Message msg = new Message(UUID.randomUUID().toString().substring(0, 8), from, to, content, System.currentTimeMillis());
        List<Message> inbox = inboxes.get(to);
        if (inbox != null) {
            inbox.add(msg);
        }
    }
    
    public List<Message> readInbox(String teammateId) {
        return new ArrayList<>(inboxes.getOrDefault(teammateId, Collections.emptyList()));
    }
}
