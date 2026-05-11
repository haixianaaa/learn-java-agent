package com.claudecode.agent.s12.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    private final Path tasksDir;
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public TaskManager(Path tasksDir) {
        this.tasksDir = tasksDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void loadAll() throws IOException {
        tasks.clear();
        if (!Files.exists(tasksDir)) {
            return;
        }

        Files.walk(tasksDir)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(this::loadTask);
    }

    private void loadTask(Path file) {
        try {
            String content = Files.readString(file);
            Task task = objectMapper.readValue(content, Task.class);
            tasks.put(task.getId(), task);
        } catch (Exception e) {
            System.err.println("Failed to load task: " + file);
        }
    }

    public Task create(String content, String priority) {
        Task task = Task.create(content, priority);
        tasks.put(task.getId(), task);
        saveTask(task);
        return task;
    }

    public Task get(String id) {
        return tasks.get(id);
    }

    public List<Task> list() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> listByStatus(String status) {
        return tasks.values().stream()
                .filter(t -> t.getStatus().equals(status))
                .toList();
    }

    public void update(String id, String status) {
        Task existing = tasks.get(id);
        if (existing != null) {
            Task updated = existing.withStatus(status);
            tasks.put(id, updated);
            saveTask(updated);
        }
    }

    public void summarize(String id, String summary) {
        Task existing = tasks.get(id);
        if (existing != null) {
            Task updated = existing.withSummary(summary);
            tasks.put(id, updated);
            saveTask(updated);
        }
    }

    public void delete(String id) {
        tasks.remove(id);
        try {
            Files.deleteIfExists(tasksDir.resolve(id + ".json"));
        } catch (IOException ignored) {
        }
    }

    private void saveTask(Task task) {
        try {
            Files.createDirectories(tasksDir);
            Path file = tasksDir.resolve(task.getId() + ".json");
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task);
            Files.writeString(file, content);
        } catch (Exception e) {
            System.err.println("Failed to save task: " + task.getId());
        }
    }

    public String renderList() {
        if (tasks.isEmpty()) {
            return "No tasks.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tasks:\n");

        List<Task> sorted = tasks.values().stream()
                .sorted(Comparator.comparing(Task::getCreatedAt))
                .toList();

        for (Task task : sorted) {
            sb.append("  ").append(task).append("\n");
        }

        long completed = tasks.values().stream()
                .filter(t -> t.getStatus().equals("completed"))
                .count();
        sb.append(String.format("(%d/%d completed)", completed, tasks.size()));

        return sb.toString();
    }
}
