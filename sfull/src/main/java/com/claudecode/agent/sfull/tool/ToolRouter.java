package com.claudecode.agent.sfull.tool;

import com.claudecode.agent.sfull.model.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolRouter {
    private final Map<String, ToolExecutor> tools = new HashMap<>();

    public ToolRouter() {
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

    public List<Tool> toolSpecs() {
        return tools.values().stream()
                .map(ToolExecutor::toolSpec)
                .toList();
    }

    public String call(ToolContext context, String name, Map<String, Object> input) throws Exception {
        ToolExecutor tool = tools.get(name);
        if (tool == null) {
            return "Unknown tool: " + name;
        }
        return tool.invoke(context, input);
    }
}
