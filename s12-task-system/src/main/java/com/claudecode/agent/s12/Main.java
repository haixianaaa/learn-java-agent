package com.claudecode.agent.s12;

import com.claudecode.agent.s12.task.Task;
import com.claudecode.agent.s12.task.TaskManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path tasksDir = Paths.get(System.getProperty("user.dir"), ".tasks");
        TaskManager taskManager = new TaskManager(tasksDir);

        try {
            taskManager.loadAll();
        } catch (Exception e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
        }

        System.out.println("S12 - Task System");
        System.out.println("Commands: list, create <content> <priority>, update <id> <status>, delete <id>, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("list")) {
                System.out.println(taskManager.renderList());
            } else if (input.startsWith("create ")) {
                String[] parts = input.substring(7).split(" ", 2);
                if (parts.length >= 1) {
                    String content = parts[0];
                    String priority = parts.length > 1 ? parts[1] : "medium";
                    Task task = taskManager.create(content, priority);
                    System.out.println("Created: " + task);
                }
            } else if (input.startsWith("update ")) {
                String[] parts = input.substring(7).split(" ", 2);
                if (parts.length == 2) {
                    taskManager.update(parts[0], parts[1]);
                    System.out.println("Updated task " + parts[0]);
                }
            } else if (input.startsWith("delete ")) {
                String id = input.substring(7);
                taskManager.delete(id);
                System.out.println("Deleted task " + id);
            }
        }

        scanner.close();
    }
}
