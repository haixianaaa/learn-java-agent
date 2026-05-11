package com.claudecode.agent.s13;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("S13 - Background Tasks");
        
        BackgroundTaskManager manager = new BackgroundTaskManager();
        
        String taskId = manager.start(() -> {
            for (int i = 1; i <= 5; i++) {
                Thread.sleep(1000);
                System.out.println("Background task progress: " + i + "/5");
            }
            return "Background task completed";
        });
        
        System.out.println("Task started with ID: " + taskId);
        
        Thread.sleep(3000);
        
        BackgroundTaskManager.TaskStatus status = manager.check(taskId);
        System.out.println("Task status: " + status);
        
        manager.shutdown();
    }
}

class BackgroundTaskManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Future<String>> tasks = new ConcurrentHashMap<>();
    private final Map<String, TaskStatus> statuses = new ConcurrentHashMap<>();
    
    public String start(Callable<String> task) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        statuses.put(id, TaskStatus.RUNNING);
        
        Future<String> future = executor.submit(() -> {
            try {
                String result = task.call();
                statuses.put(id, TaskStatus.COMPLETED);
                return result;
            } catch (Exception e) {
                statuses.put(id, TaskStatus.FAILED);
                throw e;
            }
        });
        
        tasks.put(id, future);
        return id;
    }
    
    public TaskStatus check(String id) {
        return statuses.getOrDefault(id, TaskStatus.NOT_FOUND);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    enum TaskStatus {
        RUNNING, COMPLETED, FAILED, NOT_FOUND
    }
}
