package com.claudecode.agent.s14;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("S14 - Cron Scheduler");
        
        CronScheduler scheduler = new CronScheduler();
        
        scheduler.schedule("job1", "*/5 * * * * *", () -> {
            System.out.println("Cron job executed at: " + new Date());
        });
        
        System.out.println("Scheduled jobs:");
        scheduler.list().forEach(j -> System.out.println("  " + j));
        
        Thread.sleep(15000);
        
        scheduler.stop("job1");
        scheduler.shutdown();
    }
}

class CronScheduler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();
    
    public void schedule(String id, String cronExpr, Runnable task) {
        long interval = parseCronInterval(cronExpr);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(task, 0, interval, TimeUnit.SECONDS);
        jobs.put(id, future);
    }
    
    public void stop(String id) {
        ScheduledFuture<?> future = jobs.remove(id);
        if (future != null) {
            future.cancel(false);
        }
    }
    
    public List<String> list() {
        return new ArrayList<>(jobs.keySet());
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    private long parseCronInterval(String cronExpr) {
        String[] parts = cronExpr.split(" ");
        if (parts.length >= 1 && parts[0].startsWith("*/")) {
            return Long.parseLong(parts[0].substring(2));
        }
        return 60;
    }
}
