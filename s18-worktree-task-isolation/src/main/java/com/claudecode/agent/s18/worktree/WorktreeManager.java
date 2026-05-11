package com.claudecode.agent.s18.worktree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorktreeManager {
    private final Map<String, Worktree> worktrees = new ConcurrentHashMap<>();
    private final Path baseDir;
    private final Path repoRoot;

    public WorktreeManager(Path repoRoot) {
        this.repoRoot = repoRoot;
        this.baseDir = repoRoot.resolve(".worktrees");
    }

    public Worktree create(String name) throws IOException {
        Path worktreePath = baseDir.resolve(name);
        
        Files.createDirectories(worktreePath);
        
        Worktree worktree = Worktree.create(name, baseDir);
        worktrees.put(worktree.getId(), worktree);
        
        return worktree;
    }

    public Worktree get(String id) {
        return worktrees.get(id);
    }

    public List<Worktree> list() {
        return new ArrayList<>(worktrees.values());
    }

    public void remove(String id) throws IOException {
        Worktree worktree = worktrees.remove(id);
        if (worktree != null) {
            deleteRecursively(worktree.getPath());
        }
    }

    public String executeInWorktree(String id, String command) throws IOException, InterruptedException {
        Worktree worktree = worktrees.get(id);
        if (worktree == null) {
            return "Worktree not found: " + id;
        }

        ProcessBuilder pb = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            pb.command("cmd", "/c", command);
        } else {
            pb.command("sh", "-c", command);
        }
        pb.directory(worktree.getPath().toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }

    public String listGitWorktrees() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "worktree", "list");
        pb.directory(repoRoot.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    public String renderList() {
        if (worktrees.isEmpty()) {
            return "No worktrees.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Worktrees:\n");
        for (Worktree w : worktrees.values()) {
            sb.append(String.format("  [%s] %s: %s%n", w.getStatus(), w.getName(), w.getPath()));
        }
        return sb.toString();
    }
}
