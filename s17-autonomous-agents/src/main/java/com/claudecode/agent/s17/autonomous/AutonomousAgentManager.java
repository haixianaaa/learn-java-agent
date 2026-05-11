package com.claudecode.agent.s17.autonomous;

import java.util.*;
import java.util.concurrent.*;

public class AutonomousAgentManager {
    private final Map<String, AutonomousAgent> agents = new ConcurrentHashMap<>();
    private final Map<String, AgentTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, List<AgentTask>> agentTasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AutonomousAgent createAgent(String name, String role, String systemPrompt) {
        AutonomousAgent agent = AutonomousAgent.create(name, role, systemPrompt);
        agents.put(agent.getId(), agent);
        agentTasks.put(agent.getId(), new CopyOnWriteArrayList<>());
        return agent;
    }

    public AgentTask createTask(String subject, String description) {
        AgentTask task = AgentTask.create(subject, description);
        tasks.put(task.getId(), task);
        return task;
    }

    public void assignTask(String taskId, String agentId) {
        AgentTask task = tasks.get(taskId);
        if (task != null && agents.containsKey(agentId)) {
            AgentTask assigned = task.withAssignee(agentId);
            tasks.put(taskId, assigned);
            agentTasks.get(agentId).add(assigned);
        }
    }

    public void startAgent(String agentId) {
        AutonomousAgent agent = agents.get(agentId);
        if (agent != null) {
            agents.put(agentId, agent.working());
            System.out.println("[Agent " + agentId + "] started working");
        }
    }

    public void stopAgent(String agentId) {
        AutonomousAgent agent = agents.get(agentId);
        if (agent != null) {
            agents.put(agentId, agent.idle());
            System.out.println("[Agent " + agentId + "] stopped");
        }
    }

    public AutonomousAgent getAgent(String id) {
        return agents.get(id);
    }

    public List<AutonomousAgent> listAgents() {
        return new ArrayList<>(agents.values());
    }

    public List<AgentTask> getAgentTasks(String agentId) {
        return new ArrayList<>(agentTasks.getOrDefault(agentId, Collections.emptyList()));
    }

    public List<AgentTask> listTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void shutdown() {
        executor.shutdown();
    }
}
