package com.claudecode.agent.s02.tool;

import com.claudecode.agent.s02.model.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, ToolExecutor> tools = new HashMap<>();

    public ToolRegistry() {
        register(new BashTool());
        register(new ReadFileTool());
        register(new WriteFileTool());
        register(new EditFileTool());
    }

    public void register(ToolExecutor tool) {
        tools.put(tool.name(), tool);
    }

    public ToolExecutor get(String name) {
        return tools.get(name);
    }

    public List<Tool> getAllToolSpecs() {
        return tools.values().stream()
                .map(ToolExecutor::toolSpec)
                .toList();
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
}
