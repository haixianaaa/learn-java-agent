package com.claudecode.agent.s08;

import com.claudecode.agent.s08.hook.*;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        HookSystem hookSystem = new HookSystem();

        hookSystem.registerPreToolHook(toolUse -> {
            System.out.println("[PreHook] Tool called: " + toolUse.getName());
            if (toolUse.getName().equals("dangerous_tool")) {
                System.out.println("[PreHook] Blocked dangerous tool!");
                return HookControl.block("Dangerous tool not allowed");
            }
            return HookControl.CONTINUE;
        });

        hookSystem.registerPostToolUseHook(toolResult -> {
            System.out.println("[PostHook] Tool result length: " + toolResult.getContent().length());
            return HookControl.CONTINUE;
        });

        hookSystem.registerSessionStartHook(v -> {
            System.out.println("[SessionStart] Hook system initialized");
            return HookControl.CONTINUE;
        });

        hookSystem.runSessionStartHooks();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nHook System Demo. Type 'exit()' to quit.");
        System.out.println("Commands: run <tool_name> <input>");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("run ")) {
                String[] parts = input.substring(4).split(" ", 2);
                String toolName = parts[0];
                String toolInput = parts.length > 1 ? parts[1] : "";

                ToolUse toolUse = new ToolUse("id-1", toolName, Map.of("input", toolInput));

                HookControl preResult = hookSystem.runPreToolHooks(toolUse);
                if (preResult == HookControl.BLOCK) {
                    System.out.println("Tool blocked by pre-hook: " + preResult.getReason());
                    continue;
                }

                ToolResult result = new ToolResult("id-1", "Output from " + toolName + ": " + toolInput);
                hookSystem.runPostToolHooks(result);

                System.out.println("Result: " + result.getContent());
            } else {
                System.out.println("Unknown command. Use: run <tool_name> <input>");
            }
        }

        scanner.close();
    }
}
