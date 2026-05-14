package com.claudecode.agent.s12.tool;

import com.claudecode.agent.s12.model.Tool;
import com.claudecode.agent.s12.task.TaskManager;

import java.util.List;
import java.util.Map;

public class TaskListTool implements ToolExecutor {
    private final TaskManager taskManager;

    public TaskListTool(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        return taskManager.listAll();
    }

    @Override
    public String name() {
        return "task_list";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("task_list")
                .description("List all tasks.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of()
                ))
                .build();
    }
}
