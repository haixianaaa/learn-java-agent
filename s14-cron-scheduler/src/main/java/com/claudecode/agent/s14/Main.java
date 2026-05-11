package com.claudecode.agent.s14;

import com.claudecode.agent.s14.cron.CronScheduler;
import com.claudecode.agent.s14.cron.CronTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path dataDir = Paths.get(System.getProperty("user.dir"));
        CronScheduler scheduler = new CronScheduler(dataDir);

        int loaded = scheduler.start();
        System.out.println("S14 - Cron Scheduler");
        System.out.println("[Cron scheduler running. Background checks every second.]");
        if (loaded > 0) {
            System.out.println("[Cron] Loaded " + loaded + " scheduled tasks");
        }

        System.out.println("Commands: create <name> <cron> <prompt>, list, delete <id>, test, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                scheduler.stop();
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("/cron") || input.equals("list")) {
                System.out.println(scheduler.listTasks());
            } else if (input.equals("/test") || input.equals("test")) {
                scheduler.enqueueTestNotification();
                System.out.println("[Test notification enqueued. It will be injected on your next message.]");
            } else if (input.startsWith("create ")) {
                String[] parts = input.substring(7).split(" ", 3);
                if (parts.length >= 3) {
                    CronTask task = scheduler.create(parts[0], parts[1], parts[2]);
                    System.out.println("Created scheduled task: " + task.getId());
                } else {
                    System.out.println("Usage: create <name> <cron> <prompt>");
                }
            } else if (input.startsWith("delete ")) {
                String id = input.substring(7);
                scheduler.delete(id);
                System.out.println("Deleted: " + id);
            } else if (input.equals("notifications")) {
                List<String> notifications = scheduler.drainNotifications();
                if (notifications.isEmpty()) {
                    System.out.println("No pending notifications");
                } else {
                    for (String n : notifications) {
                        System.out.println(n);
                    }
                }
            }
        }

        scanner.close();
    }
}
