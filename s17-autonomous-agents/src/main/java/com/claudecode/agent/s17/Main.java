package com.claudecode.agent.s17;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("S17 - Autonomous Agents");
        
        AutonomousAgentManager manager = new AutonomousAgentManager();
        
        AutonomousAgent agent = manager.createAgent("worker", "Task execution agent");
        System.out.println("Created agent: " + agent);
        
        manager.assignTask(agent.id(), "Process data files");
        manager.assignTask(agent.id(), "Generate report");
        
        System.out.println("\nAgent tasks:");
        manager.getTasks(agent.id()).forEach(t -> System.out.println("  " + t));
        
        manager.startAgent(agent.id());
        
        System.out.println("\nAgent status: " + manager.getStatus(agent.id()));
    }
}

record AutonomousAgent(String id, String name, String role, String status) {
    public AutonomousAgent(String id, String name, String role) {
        this(id, name, role, "idle");
    }
    
    public AutonomousAgent withStatus(String newStatus) {
        return new AutonomousAgent(id, name, role, newStatus);
    }
}

record Task(String id, String content, String status) {
    public Task(String id, String content) {
        this(id, content, "pending");
    }
    
    public Task withStatus(String newStatus) {
        return new Task(id, content, newStatus);
    }
}

class AutonomousAgentManager {
    private final Map<String, AutonomousAgent> agents = new ConcurrentHashMap<>();
    private final Map<String, List<Task>> agentTasks = new ConcurrentHashMap<>();
    
    public AutonomousAgent createAgent(String role, String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        AutonomousAgent agent = new AutonomousAgent(id, name, role);
        agents.put(id, agent);
        agentTasks.put(id, new CopyOnWriteArrayList<>());
        return agent;
    }
    
    public void assignTask(String agentId, String content) {
        List<Task> tasks = agentTasks.get(agentId);
        if (tasks != null) {
            tasks.add(new Task(UUID.randomUUID().toString().substring(0, 8), content));
        }
    }
    
    public List<Task> getTasks(String agentId) {
        return new ArrayList<>(agentTasks.getOrDefault(agentId, Collections.emptyList()));
    }
    
    public void startAgent(String agentId) {
        AutonomousAgent agent = agents.get(agentId);
        if (agent != null) {
            agents.put(agentId, agent.withStatus("running"));
        }
    }
    
    public String getStatus(String agentId) {
        return agents.getOrDefault(agentId, new AutonomousAgent("", "", "", "not_found")).status();
    }
}
