package com.claudecode.agent.s18;

import com.claudecode.agent.s18.worktree.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path workDir = Paths.get(System.getProperty("user.dir"));
        WorktreeManager manager = new WorktreeManager(workDir);

        System.out.println("S18 - Worktree Task Isolation");
        System.out.println("Commands: create <name>, list, exec <id> <cmd>, remove <id>, gitlist, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("create ")) {
                String name = input.substring(7);
                try {
                    Worktree w = manager.create(name);
                    System.out.println("Created worktree: " + w.getId() + " at " + w.getPath());
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (input.equals("list")) {
                System.out.println(manager.renderList());
            } else if (input.startsWith("exec ")) {
                String[] parts = input.substring(5).split(" ", 2);
                if (parts.length == 2) {
                    try {
                        String output = manager.executeInWorktree(parts[0], parts[1]);
                        System.out.println(output);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else if (input.startsWith("remove ")) {
                String id = input.substring(7);
                try {
                    manager.remove(id);
                    System.out.println("Removed worktree: " + id);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (input.equals("gitlist")) {
                try {
                    System.out.println(manager.listGitWorktrees());
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        scanner.close();
    }
}
