package com.claudecode.agent.s04.tool;

import com.claudecode.agent.s04.model.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, ToolExecutor> tools = new HashMap<>();

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
}
