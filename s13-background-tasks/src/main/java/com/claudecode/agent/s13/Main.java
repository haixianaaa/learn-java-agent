package com.claudecode.agent.s13;

import com.claudecode.agent.s13.background.BackgroundManager;
import com.claudecode.agent.s13.background.BackgroundTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path tasksDir = Paths.get(System.getProperty("user.dir"), ".runtime-tasks");
        BackgroundManager manager = new BackgroundManager(tasksDir);

        System.out.println("S13 - Background Tasks");
        System.out.println("Commands: start <command>, list, get <id>, cancel <id>, results, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                manager.shutdown();
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("start ")) {
                String command = input.substring(6);
                String id = manager.start(command);
                System.out.println("Started background task: " + id);
            } else if (input.equals("list")) {
                for (BackgroundTask task : manager.list()) {
                    System.out.println("  " + task.getId() + ": " + task.getStatus() + " - " + task.getCommand());
                }
            } else if (input.startsWith("get ")) {
                String id = input.substring(4);
                BackgroundTask task = manager.get(id);
                if (task != null) {
                    System.out.println(task);
                    if (task.getOutput() != null) {
                        System.out.println("Output:\n" + task.getOutput());
                    }
                } else {
                    System.out.println("Task not found: " + id);
                }
            } else if (input.startsWith("cancel ")) {
                String id = input.substring(7);
                manager.cancel(id);
                System.out.println("Cancelled: " + id);
            } else if (input.equals("results")) {
                String results = manager.drainResultsMessage();
                if (results != null) {
                    System.out.println(results);
                } else {
                    System.out.println("No pending results");
                }
            }
        }

        scanner.close();
    }
}
