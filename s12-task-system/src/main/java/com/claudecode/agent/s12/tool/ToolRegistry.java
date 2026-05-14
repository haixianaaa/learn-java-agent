package com.claudecode.agent.s12.tool;

import com.claudecode.agent.s12.model.Tool;
import com.claudecode.agent.s12.task.TaskManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, ToolExecutor> tools = new HashMap<>();

    public ToolRegistry(TaskManager taskManager) {
        register(new BashTool());
        register(new ReadFileTool());
        register(new WriteFileTool());
        register(new EditFileTool());
        register(new TaskCreateTool(taskManager));
        register(new TaskGetTool(taskManager));
        register(new TaskListTool(taskManager));
        register(new TaskUpdateTool(taskManager));
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

    public boolean contains(String name) {
        return tools.containsKey(name);
    }
}
