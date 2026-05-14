package com.claudecode.agent.s12.tool;

import com.claudecode.agent.s12.model.Tool;
import com.claudecode.agent.s12.task.TaskManager;

import java.util.List;
import java.util.Map;

public class TaskCreateTool implements ToolExecutor {
    private final TaskManager taskManager;

    public TaskCreateTool(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String subject = (String) input.get("subject");
        String description = (String) input.get("description");
        
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }

        return taskManager.create(subject, description);
    }

    @Override
    public String name() {
        return "task_create";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("task_create")
                .description("Create a new task.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "subject", Map.of("type", "string"),
                                "description", Map.of("type", "string")
                        ),
                        "required", List.of("subject")
                ))
                .build();
    }
}
