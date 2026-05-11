package com.claudecode.agent.s19;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("S19 - MCP Plugin System");
        
        MCPPluginManager pluginManager = new MCPPluginManager();
        
        pluginManager.registerPlugin("filesystem", new FilesystemPlugin());
        pluginManager.registerPlugin("database", new DatabasePlugin());
        
        System.out.println("Registered plugins:");
        pluginManager.listPlugins().forEach(p -> System.out.println("  " + p));
        
        Object result = pluginManager.callTool("filesystem", "read_file", Map.of("path", "/tmp/test.txt"));
        System.out.println("\nTool result: " + result);
    }
}

interface MCPPlugin {
    String name();
    List<String> tools();
    Object callTool(String toolName, Map<String, Object> params);
}

class FilesystemPlugin implements MCPPlugin {
    @Override
    public String name() {
        return "filesystem";
    }
    
    @Override
    public List<String> tools() {
        return List.of("read_file", "write_file", "list_dir");
    }
    
    @Override
    public Object callTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "read_file" -> "Content of " + params.get("path");
            case "write_file" -> "Written to " + params.get("path");
            case "list_dir" -> List.of("file1.txt", "file2.txt");
            default -> "Unknown tool: " + toolName;
        };
    }
}

class DatabasePlugin implements MCPPlugin {
    @Override
    public String name() {
        return "database";
    }
    
    @Override
    public List<String> tools() {
        return List.of("query", "insert", "update");
    }
    
    @Override
    public Object callTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "query" -> List.of(Map.of("id", 1, "name", "test"));
            case "insert" -> "Inserted 1 row";
            case "update" -> "Updated 1 row";
            default -> "Unknown tool: " + toolName;
        };
    }
}

class MCPPluginManager {
    private final Map<String, MCPPlugin> plugins = new ConcurrentHashMap<>();
    
    public void registerPlugin(String name, MCPPlugin plugin) {
        plugins.put(name, plugin);
    }
    
    public List<String> listPlugins() {
        return new ArrayList<>(plugins.keySet());
    }
    
    public Object callTool(String pluginName, String toolName, Map<String, Object> params) {
        MCPPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            return "Unknown plugin: " + pluginName;
        }
        return plugin.callTool(toolName, params);
    }
}
