package com.claudecode.agent.s19.mcp;

import java.util.List;
import java.util.Map;

public class DatabasePlugin implements MCPPlugin {
    @Override
    public String name() {
        return "database";
    }

    @Override
    public String description() {
        return "Database operations plugin";
    }

    @Override
    public List<String> tools() {
        return List.of("query", "insert", "update", "delete", "list_tables");
    }

    @Override
    public Object callTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "query" -> {
                String sql = (String) params.getOrDefault("sql", "");
                yield List.of(
                        Map.of("id", 1, "name", "Alice"),
                        Map.of("id", 2, "name", "Bob")
                );
            }
            case "insert" -> {
                String table = (String) params.getOrDefault("table", "");
                yield "Inserted into " + table;
            }
            case "update" -> {
                String table = (String) params.getOrDefault("table", "");
                yield "Updated " + table;
            }
            case "delete" -> {
                String table = (String) params.getOrDefault("table", "");
                yield "Deleted from " + table;
            }
            case "list_tables" -> List.of("users", "products", "orders");
            default -> "Unknown tool: " + toolName;
        };
    }

    @Override
    public Map<String, Object> toolSpec(String toolName) {
        return Map.of(
                "name", toolName,
                "description", "Database tool: " + toolName,
                "input_schema", Map.of("type", "object", "properties", Map.of())
        );
    }
}
