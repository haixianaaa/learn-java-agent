package com.claudecode.agent.s19;

import com.claudecode.agent.s19.mcp.*;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MCPPluginManager manager = new MCPPluginManager();
        
        manager.register(new FilesystemPlugin());
        manager.register(new DatabasePlugin());

        System.out.println("S19 - MCP Plugin System");
        System.out.println(manager.renderPlugins());
        System.out.println("Commands: plugins, tools, call <plugin> <tool> [params], exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("plugins")) {
                System.out.println(manager.renderPlugins());
            } else if (input.equals("tools")) {
                System.out.println("All available tools:");
                for (Map<String, Object> tool : manager.listAllTools()) {
                    System.out.println("  - " + tool.get("name"));
                }
            } else if (input.startsWith("call ")) {
                String[] parts = input.substring(5).split(" ", 3);
                if (parts.length >= 2) {
                    String plugin = parts[0];
                    String tool = parts[1];
                    Map<String, Object> params = parts.length > 2 
                            ? Map.of("input", parts[2]) 
                            : Map.of();
                    
                    Object result = manager.callTool(plugin, tool, params);
                    System.out.println("Result: " + result);
                }
            }
        }

        scanner.close();
    }
}
