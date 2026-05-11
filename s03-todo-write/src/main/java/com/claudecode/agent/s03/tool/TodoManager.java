package com.claudecode.agent.s03.tool;

import com.claudecode.agent.s03.model.PlanItem;
import com.claudecode.agent.s03.model.PlanItemStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TodoManager {
    private static final int MAX_ITEMS = 12;
    private final List<PlanItem> items = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public String invoke(Object input) {
        try {
            List<Map<String, Object>> itemsData;
            
            if (input instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) input;
                Object itemsObj = inputMap.get("items");
                if (itemsObj == null) {
                    return "Error: Invalid items";
                }
                itemsData = (List<Map<String, Object>>) itemsObj;
            } else if (input instanceof List) {
                itemsData = (List<Map<String, Object>>) input;
            } else {
                return "Error: Invalid items";
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
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<PlanItem> getItems() {
        return new ArrayList<>(items);
    }
}
