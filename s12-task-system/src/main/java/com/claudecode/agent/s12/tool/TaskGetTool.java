package com.claudecode.agent.s12.tool;

import com.claudecode.agent.s12.model.Tool;
import com.claudecode.agent.s12.task.TaskManager;

import java.util.List;
import java.util.Map;

public class TaskGetTool implements ToolExecutor {
    private final TaskManager taskManager;

    public TaskGetTool(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        Object idObj = input.get("task_id");
        if (idObj == null) {
            throw new IllegalArgumentException("task_id is required");
        }

        long taskId;
        if (idObj instanceof Number) {
            taskId = ((Number) idObj).longValue();
        } else {
            taskId = Long.parseLong(idObj.toString());
        }

        return taskManager.get(taskId);
    }

    @Override
    public String name() {
        return "task_get";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("task_get")
                .description("Get a task by ID.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "task_id", Map.of("type", "integer")
                        ),
                        "required", List.of("task_id")
                ))
                .build();
    }
}
