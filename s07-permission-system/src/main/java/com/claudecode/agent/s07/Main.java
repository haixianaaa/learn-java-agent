package com.claudecode.agent.s07;

import com.claudecode.agent.s07.permission.*;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Permission System Demo. Select mode:");
        System.out.println("1. DEFAULT - ask by default");
        System.out.println("2. PLAN - read only");
        System.out.println("3. AUTO - allow reads, ask for writes");

        System.out.print("\nSelect mode (1-3): ");
        int modeChoice = Integer.parseInt(scanner.nextLine().trim());

        PermissionMode mode = switch (modeChoice) {
            case 2 -> PermissionMode.PLAN;
            case 3 -> PermissionMode.AUTO;
            default -> PermissionMode.DEFAULT;
        };

        PermissionManager manager = new PermissionManager(mode);
        System.out.println("\nPermission mode: " + mode);
        System.out.println("Commands: check <tool> <input>, exit()");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("check ")) {
                String[] parts = input.substring(6).split(" ", 2);
                String tool = parts[0];
                Map<String, Object> toolInput = parts.length > 1 
                        ? Map.of("command", parts[1], "path", parts[1])
                        : Map.of();

                PermissionDecision decision = manager.check(tool, toolInput);
                System.out.println("Decision: " + decision.getBehavior());
                System.out.println("Reason: " + decision.getReason());
            } else {
                System.out.println("Unknown command. Use: check <tool> <input>");
            }
        }

        scanner.close();
    }
}
