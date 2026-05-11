package com.claudecode.agent.s18;

import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("S18 - Worktree Task Isolation");
        
        WorktreeManager worktreeManager = new WorktreeManager();
        
        Path mainWorktree = Paths.get(System.getProperty("user.dir"));
        Path taskWorktree = worktreeManager.createWorktree("task-123");
        
        System.out.println("Main worktree: " + mainWorktree);
        System.out.println("Task worktree: " + taskWorktree);
        
        System.out.println("\nActive worktrees:");
        worktreeManager.listWorktrees().forEach(w -> System.out.println("  " + w));
        
        worktreeManager.removeWorktree("task-123");
        System.out.println("\nAfter removal:");
        worktreeManager.listWorktrees().forEach(w -> System.out.println("  " + w));
    }
}

class WorktreeManager {
    private final Map<String, Path> worktrees = new ConcurrentHashMap<>();
    private final Path baseDir;
    
    public WorktreeManager() {
        this.baseDir = Paths.get(System.getProperty("user.dir"), ".worktrees");
    }
    
    public Path createWorktree(String name) {
        Path worktreePath = baseDir.resolve(name);
        try {
            Files.createDirectories(worktreePath);
            worktrees.put(name, worktreePath);
            return worktreePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create worktree: " + name, e);
        }
    }
    
    public void removeWorktree(String name) {
        Path path = worktrees.remove(name);
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {
            }
        }
    }
    
    public List<String> listWorktrees() {
        return new ArrayList<>(worktrees.keySet());
    }
}
