package com.claudecode.agent.s12.tool;

import com.claudecode.agent.s12.model.Tool;
import com.claudecode.agent.s12.task.TaskManager;
import com.claudecode.agent.s12.task.TaskStatus;
import com.claudecode.agent.s12.task.TaskUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskUpdateTool implements ToolExecutor {
    private final TaskManager taskManager;

    public TaskUpdateTool(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    @SuppressWarnings("unchecked")
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

        TaskUpdate update = new TaskUpdate();

        String statusStr = (String) input.get("status");
        if (statusStr != null) {
            update.setStatus(TaskStatus.fromString(statusStr));
        }

        String owner = (String) input.get("owner");
        if (owner != null) {
            update.setOwner(owner);
        }

        Object blockedByObj = input.get("add_blocked_by");
        if (blockedByObj instanceof List) {
            List<Long> blockedBy = new ArrayList<>();
            for (Object obj : (List<?>) blockedByObj) {
                if (obj instanceof Number) {
                    blockedBy.add(((Number) obj).longValue());
                } else {
                    blockedBy.add(Long.parseLong(obj.toString()));
                }
            }
            update.setAddBlockedBy(blockedBy);
        }

        Object blocksObj = input.get("add_blocks");
        if (blocksObj instanceof List) {
            List<Long> blocks = new ArrayList<>();
            for (Object obj : (List<?>) blocksObj) {
                if (obj instanceof Number) {
                    blocks.add(((Number) obj).longValue());
                } else {
                    blocks.add(Long.parseLong(obj.toString()));
                }
            }
            update.setAddBlocks(blocks);
        }

        return taskManager.update(taskId, update);
    }

    @Override
    public String name() {
        return "task_update";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("task_update")
                .description("Update a task.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "task_id", Map.of("type", "integer"),
                                "status", Map.of(
                                        "type", "string",
                                        "enum", List.of("pending", "in_progress", "completed", "deleted")
                                ),
                                "owner", Map.of("type", "string"),
                                "add_blocked_by", Map.of(
                                        "type", "array",
                                        "items", Map.of("type", "integer")
                                ),
                                "add_blocks", Map.of(
                                        "type", "array",
                                        "items", Map.of("type", "integer")
                                )
                        ),
                        "required", List.of("task_id")
                ))
                .build();
    }
}
