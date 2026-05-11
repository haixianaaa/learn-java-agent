package com.claudecode.agent.s12;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("S12 - Task System");
        
        TaskManager taskManager = new TaskManager();
        
        Task task1 = taskManager.create("Implement feature A", "high");
        Task task2 = taskManager.create("Fix bug B", "medium");
        
        System.out.println("Tasks:");
        taskManager.list().forEach(t -> System.out.println("  " + t));
        
        taskManager.update(task1.getId(), "in_progress");
        System.out.println("\nAfter update:");
        taskManager.list().forEach(t -> System.out.println("  " + t));
    }
}

record Task(String id, String content, String priority, String status) {
    public Task {
        if (status == null) status = "pending";
    }
}

class TaskManager {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    
    public Task create(String content, String priority) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Task task = new Task(id, content, priority, "pending");
        tasks.put(id, task);
        return task;
    }
    
    public Task get(String id) {
        return tasks.get(id);
    }
    
    public List<Task> list() {
        return new ArrayList<>(tasks.values());
    }
    
    public void update(String id, String status) {
        Task existing = tasks.get(id);
        if (existing != null) {
            Task updated = new Task(existing.id(), existing.content(), existing.priority(), status);
            tasks.put(id, updated);
        }
    }
}
