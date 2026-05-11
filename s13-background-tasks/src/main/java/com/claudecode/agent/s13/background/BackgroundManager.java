package com.claudecode.agent.s13.background;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class BackgroundManager {
    private final ExecutorService executor;
    private final Map<String, BackgroundTask> tasks;
    private final Map<String, Future<?>> futures;
    private final Queue<String> pendingResults;
    private final Path tasksDir;

    public BackgroundManager(Path tasksDir) {
        this.tasksDir = tasksDir;
        this.executor = Executors.newCachedThreadPool();
        this.tasks = new ConcurrentHashMap<>();
        this.futures = new ConcurrentHashMap<>();
        this.pendingResults = new ConcurrentLinkedQueue<>();
    }

    public String start(String command) {
        BackgroundTask task = BackgroundTask.create(command);
        tasks.put(task.getId(), task);

        Future<?> future = executor.submit(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder();
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    pb.command("cmd", "/c", command);
                } else {
                    pb.command("sh", "-c", command);
                }
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

                int exitCode = process.waitFor();
                BackgroundTask completed = exitCode == 0 
                        ? task.complete(output.toString())
                        : task.fail("Exit code: " + exitCode + "\n" + output);

                tasks.put(task.getId(), completed);
                pendingResults.add(task.getId());

            } catch (Exception e) {
                BackgroundTask failed = task.fail(e.getMessage());
                tasks.put(task.getId(), failed);
                pendingResults.add(task.getId());
            }
        });

        futures.put(task.getId(), future);
        return task.getId();
    }

    public BackgroundTask get(String id) {
        return tasks.get(id);
    }

    public List<BackgroundTask> list() {
        return new ArrayList<>(tasks.values());
    }

    public void cancel(String id) {
        Future<?> future = futures.get(id);
        if (future != null) {
            future.cancel(true);
        }
        tasks.remove(id);
    }

    public String drainResultsMessage() {
        if (pendingResults.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Background tasks completed:\n\n");

        String taskId;
        while ((taskId = pendingResults.poll()) != null) {
            BackgroundTask task = tasks.get(taskId);
            if (task != null) {
                sb.append(String.format("Task %s (%s): %s%n",
                        task.getId(), task.getStatus(), task.getCommand()));
                if (task.getOutput() != null) {
                    String output = task.getOutput();
                    if (output.length() > 500) {
                        output = output.substring(0, 500) + "... (truncated)";
                    }
                    sb.append("```\n").append(output).append("\n```\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
