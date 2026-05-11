package com.claudecode.agent.s20;

import com.claudecode.agent.s20.registry.*;
import com.claudecode.agent.s20.tools.*;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ToolRegistry registry = new ToolRegistry();
        
        registry.register(new MathTools());
        registry.register(new StringTools());

        System.out.println("S20 - Tool Refactor with Annotations");
        System.out.println(registry.renderTools());
        System.out.println("Commands: list, exec <tool> <json>, specs, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("list")) {
                System.out.println(registry.renderTools());
            } else if (input.equals("specs")) {
                for (Map<String, Object> spec : registry.listToolSpecs()) {
                    System.out.println("  " + spec.get("name") + ": " + spec.get("input_schema"));
                }
            } else if (input.startsWith("exec ")) {
                String[] parts = input.substring(5).split(" ", 2);
                String toolName = parts[0];
                
                Map<String, Object> params = Map.of();
                if (parts.length > 1) {
                    String[] keyValuePairs = parts[1].split(",");
                    Map<String, Object> parsed = new java.util.HashMap<>();
                    for (String pair : keyValuePairs) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            String key = kv[0].trim();
                            String value = kv[1].trim();
                            try {
                                parsed.put(key, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                try {
                                    parsed.put(key, Double.parseDouble(value));
                                } catch (NumberFormatException e2) {
                                    parsed.put(key, value);
                                }
                            }
                        }
                    }
                    params = parsed;
                }

                try {
                    Object result = registry.execute(toolName, params);
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        scanner.close();
    }
}
