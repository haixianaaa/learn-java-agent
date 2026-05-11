package com.claudecode.agent.s14.cron;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class CronScheduler {
    private final ScheduledExecutorService executor;
    private final Map<String, CronTask> tasks;
    private final Map<String, ScheduledFuture<?>> futures;
    private final Queue<String> pendingNotifications;
    private final Path dataDir;

    public CronScheduler(Path dataDir) {
        this.dataDir = dataDir;
        this.executor = Executors.newScheduledThreadPool(1);
        this.tasks = new ConcurrentHashMap<>();
        this.futures = new ConcurrentHashMap<>();
        this.pendingNotifications = new ConcurrentLinkedQueue<>();
    }

    public int start() {
        int loaded = 0;
        for (CronTask task : tasks.values()) {
            if (task.isEnabled()) {
                scheduleTask(task);
                loaded++;
            }
        }
        return loaded;
    }

    public CronTask create(String name, String cronExpression, String prompt) {
        CronTask task = CronTask.create(name, cronExpression, prompt);
        tasks.put(task.getId(), task);
        scheduleTask(task);
        return task;
    }

    private void scheduleTask(CronTask task) {
        long intervalSeconds = parseCronInterval(task.getCronExpression());
        
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            String notification = String.format(
                    "[Scheduled task '%s'] %s",
                    task.getName(), task.getPrompt()
            );
            pendingNotifications.add(notification);
            tasks.put(task.getId(), task.withLastRun(Instant.now()));
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        futures.put(task.getId(), future);
    }

    private long parseCronInterval(String cronExpr) {
        String[] parts = cronExpr.split(" ");
        if (parts.length >= 1 && parts[0].startsWith("*/")) {
            try {
                return Long.parseLong(parts[0].substring(2));
            } catch (NumberFormatException e) {
                return 60;
            }
        }
        return 60;
    }

    public CronTask get(String id) {
        return tasks.get(id);
    }

    public List<CronTask> list() {
        return new ArrayList<>(tasks.values());
    }

    public void delete(String id) {
        ScheduledFuture<?> future = futures.remove(id);
        if (future != null) {
            future.cancel(false);
        }
        tasks.remove(id);
    }

    public void disable(String id) {
        ScheduledFuture<?> future = futures.remove(id);
        if (future != null) {
            future.cancel(false);
        }
        CronTask task = tasks.get(id);
        if (task != null) {
            tasks.put(id, task.disable());
        }
    }

    public List<String> drainNotifications() {
        List<String> notifications = new ArrayList<>();
        String notification;
        while ((notification = pendingNotifications.poll()) != null) {
            notifications.add(notification);
        }
        return notifications;
    }

    public void enqueueTestNotification() {
        pendingNotifications.add("[Test notification] This is a test scheduled message.");
    }

    public String listTasks() {
        if (tasks.isEmpty()) {
            return "No scheduled tasks.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Scheduled tasks:\n");
        for (CronTask task : tasks.values()) {
            sb.append(String.format("  %s: %s (%s) - %s%n",
                    task.getId(), task.getName(), task.getCronExpression(),
                    task.isEnabled() ? "enabled" : "disabled"));
        }
        return sb.toString();
    }

    public void stop() {
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
