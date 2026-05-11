package com.claudecode.agent.s09;

import com.claudecode.agent.s09.memory.MemoryManager;
import com.claudecode.agent.s09.memory.MemoryType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path memoryDir = Paths.get(System.getProperty("user.dir"), ".claude", "memory");
        MemoryManager manager = new MemoryManager(memoryDir);

        try {
            manager.loadAll();
        } catch (Exception e) {
            System.err.println("Failed to load memories: " + e.getMessage());
        }

        System.out.println("Memory System Demo. Type 'exit()' to quit.");
        System.out.println("Commands: list, save <name> <type> <description>, show, prompt");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("list")) {
                System.out.println(manager.describeMemories());
            } else if (input.startsWith("save ")) {
                String[] parts = input.substring(5).split(" ", 3);
                if (parts.length >= 3) {
                    String name = parts[0];
                    MemoryType type = MemoryType.valueOf(parts[1].toUpperCase());
                    String description = parts[2];

                    try {
                        String result = manager.saveMemory(name, description, type, "");
                        System.out.println(result);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                } else {
                    System.out.println("Usage: save <name> <type> <description>");
                }
            } else if (input.equals("show")) {
                System.out.println(manager.loadMemoryPrompt());
            } else if (input.equals("prompt")) {
                System.out.println(MemoryManager.MEMORY_GUIDANCE);
            } else {
                System.out.println("Unknown command. Use: list, save, show, prompt");
            }
        }

        scanner.close();
    }
}
