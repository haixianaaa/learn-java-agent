package com.claudecode.agent.s03.tool;

import com.claudecode.agent.s03.model.PlanItem;
import com.claudecode.agent.s03.model.PlanItemStatus;
import com.claudecode.agent.s03.model.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TodoManager implements ToolExecutor {
    private static final int MAX_ITEMS = 12;
    private final List<PlanItem> items = new ArrayList<>();

    public String update(List<PlanItem> newItems) {
        if (newItems.size() > MAX_ITEMS) {
            return "Error: Keep the session plan short (max " + MAX_ITEMS + " items)";
        }

        long inProgressCount = newItems.stream()
                .filter(item -> item.getStatus() == PlanItemStatus.IN_PROGRESS)
                .count();

        if (inProgressCount > 1) {
            return "Error: Only one plan item can be in_progress";
        }

        items.clear();
        items.addAll(newItems);
        return render();
    }

    public String render() {
        if (items.isEmpty()) {
            return "No session plan yet.";
        }

        StringBuilder sb = new StringBuilder();
        for (PlanItem item : items) {
            sb.append(item.toString()).append("\n");
        }

        long completed = items.stream()
                .filter(item -> item.getStatus() == PlanItemStatus.COMPLETED)
                .count();

        sb.append("(").append(completed).append("/").append(items.size()).append(" completed)");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        List<Map<String, Object>> itemsData;
        
        Object itemsObj = input.get("items");
        if (itemsObj == null) {
            throw new IllegalArgumentException("Invalid items");
        }
        
        if (itemsObj instanceof List) {
            itemsData = (List<Map<String, Object>>) itemsObj;
        } else {
            throw new IllegalArgumentException("Invalid items");
        }

        List<PlanItem> newItems = new ArrayList<>();
        for (Map<String, Object> itemData : itemsData) {
            String content = (String) itemData.get("content");
            String statusStr = (String) itemData.get("status");
            String activeForm = (String) itemData.get("activeForm");

            PlanItem item = PlanItem.builder()
                    .content(content)
                    .status(PlanItemStatus.fromString(statusStr))
                    .activeForm(activeForm)
                    .build();
            newItems.add(item);
        }

        return update(newItems);
    }

    @Override
    public String name() {
        return "todo";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("todo")
                .description("Rewrite the current session plan for multi-step work.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "items", Map.of(
                                        "type", "array",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "content", Map.of("type", "string"),
                                                        "status", Map.of(
                                                                "type", "string",
                                                                "enum", List.of("pending", "in_progress", "completed")
                                                        ),
                                                        "activeForm", Map.of(
                                                                "type", "string",
                                                                "description", "Optional present-continuous label."
                                                        )
                                                ),
                                                "required", List.of("content", "status")
                                        )
                                )
                        ),
                        "required", List.of("items")
                ))
                .build();
    }

    public List<PlanItem> getItems() {
        return new ArrayList<>(items);
    }
}
