package com.claudecode.agent.s19.mcp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MCPPluginManager {
    private final Map<String, MCPPlugin> plugins = new ConcurrentHashMap<>();

    public void register(MCPPlugin plugin) {
        plugins.put(plugin.name(), plugin);
    }

    public void unregister(String name) {
        plugins.remove(name);
    }

    public MCPPlugin getPlugin(String name) {
        return plugins.get(name);
    }

    public List<String> listPlugins() {
        return new ArrayList<>(plugins.keySet());
    }

    public List<Map<String, Object>> listAllTools() {
        List<Map<String, Object>> allTools = new ArrayList<>();
        for (MCPPlugin plugin : plugins.values()) {
            for (String tool : plugin.tools()) {
                allTools.add(plugin.toolSpec(tool));
            }
        }
        return allTools;
    }

    public Object callTool(String pluginName, String toolName, Map<String, Object> params) {
        MCPPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            return "Unknown plugin: " + pluginName;
        }
        
        if (!plugin.tools().contains(toolName)) {
            return "Unknown tool: " + toolName + " in plugin: " + pluginName;
        }
        
        return plugin.callTool(toolName, params);
    }

    public String renderPlugins() {
        if (plugins.isEmpty()) {
            return "No plugins registered.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Registered plugins:\n");
        for (MCPPlugin plugin : plugins.values()) {
            sb.append(String.format("  - %s: %s%n", plugin.name(), plugin.description()));
            sb.append(String.format("    Tools: %s%n", plugin.tools()));
        }
        return sb.toString();
    }
}
