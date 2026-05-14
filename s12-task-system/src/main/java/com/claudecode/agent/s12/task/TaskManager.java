package com.claudecode.agent.s12.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TaskManager {
    private final Path tasksDir;
    private long nextId;
    private final ObjectMapper objectMapper;

    public TaskManager(Path tasksDir) {
        this.tasksDir = tasksDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        try {
            Files.createDirectories(tasksDir);
            this.nextId = maxTaskId() + 1;
        } catch (IOException e) {
            this.nextId = 1;
        }
    }

    public String create(String subject, String description) throws IOException {
        TaskRecord task = TaskRecord.create(nextId, subject, description);
        save(task);
        nextId++;
        return renderJson(task);
    }

    public String get(long taskId) throws IOException {
        TaskRecord task = load(taskId);
        return renderJson(task);
    }

    public String update(long taskId, TaskUpdate update) throws IOException {
        TaskRecord task = load(taskId);

        if (update.getOwner() != null) {
            task.setOwner(update.getOwner());
        }

        if (update.getStatus() != null) {
            task.setStatus(update.getStatus());
            if (update.getStatus() == TaskStatus.COMPLETED) {
                clearDependency(taskId);
            }
        }

        if (!update.getAddBlockedBy().isEmpty()) {
            mergeUnique(task.getBlockedBy(), update.getAddBlockedBy());
        }

        if (!update.getAddBlocks().isEmpty()) {
            mergeUnique(task.getBlocks(), update.getAddBlocks());
            for (Long blockedId : update.getAddBlocks()) {
                try {
                    TaskRecord blocked = load(blockedId);
                    if (!blocked.getBlockedBy().contains(taskId)) {
                        blocked.getBlockedBy().add(taskId);
                        blocked.getBlockedBy().sort(Long::compare);
                        save(blocked);
                    }
                } catch (IOException ignored) {
                }
            }
        }

        task.getBlockedBy().sort(Long::compare);
        task.getBlocks().sort(Long::compare);
        save(task);
        return renderJson(task);
    }

    public String listAll() throws IOException {
        List<TaskRecord> tasks = loadAll();
        if (tasks.isEmpty()) {
            return "No tasks.";
        }

        tasks.sort(Comparator.comparingLong(TaskRecord::getId));
        List<String> lines = new ArrayList<>();
        
        for (TaskRecord task : tasks) {
            String blocked = task.getBlockedBy().isEmpty() 
                    ? "" 
                    : String.format(" (blocked by: %s)", task.getBlockedBy());
            String owner = task.getOwner() == null || task.getOwner().isEmpty() 
                    ? "" 
                    : String.format(" owner=%s", task.getOwner());
            lines.add(String.format("%s #%d: %s%s%s",
                    task.getStatus().getMarker(),
                    task.getId(),
                    task.getSubject(),
                    owner,
                    blocked));
        }

        return String.join("\n", lines);
    }

    private long maxTaskId() throws IOException {
        long maxId = 0;
        try (Stream<Path> stream = Files.list(tasksDir)) {
            for (Path path : stream.toList()) {
                String name = path.getFileName().toString();
                if (name.startsWith("task_") && name.endsWith(".json")) {
                    String idText = name.substring(5, name.length() - 5);
                    try {
                        long id = Long.parseLong(idText);
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxId;
    }

    private TaskRecord load(long taskId) throws IOException {
        Path path = taskPath(taskId);
        if (!Files.exists(path)) {
            throw new IOException("Task " + taskId + " not found");
        }
        String content = Files.readString(path);
        return objectMapper.readValue(content, TaskRecord.class);
    }

    private List<TaskRecord> loadAll() throws IOException {
        List<TaskRecord> tasks = new ArrayList<>();
        try (Stream<Path> stream = Files.list(tasksDir)) {
            for (Path path : stream.toList()) {
                if (!Files.isRegularFile(path)) continue;
                String name = path.getFileName().toString();
                if (!name.startsWith("task_") || !name.endsWith(".json")) continue;
                
                try {
                    String content = Files.readString(path);
                    TaskRecord task = objectMapper.readValue(content, TaskRecord.class);
                    tasks.add(task);
                } catch (IOException ignored) {
                }
            }
        }
        return tasks;
    }

    private void save(TaskRecord task) throws IOException {
        Path path = taskPath(task.getId());
        String content = objectMapper.writeValueAsString(task);
        Files.writeString(path, content);
    }

    private void clearDependency(long completedId) throws IOException {
        for (TaskRecord task : loadAll()) {
            if (task.getBlockedBy().contains(completedId)) {
                task.getBlockedBy().removeIf(id -> id == completedId);
                save(task);
            }
        }
    }

    private String renderJson(TaskRecord task) throws IOException {
        return objectMapper.writeValueAsString(task);
    }

    private Path taskPath(long taskId) {
        return tasksDir.resolve(String.format("task_%d.json", taskId));
    }

    private void mergeUnique(List<Long> target, List<Long> additions) {
        target.addAll(additions);
        target.sort(Long::compare);
        List<Long> unique = new ArrayList<>();
        for (Long id : target) {
            if (!unique.contains(id)) {
                unique.add(id);
            }
        }
        target.clear();
        target.addAll(unique);
    }
}
