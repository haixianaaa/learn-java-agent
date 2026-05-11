package com.claudecode.agent.s19.mcp;

import java.util.List;
import java.util.Map;

public class FilesystemPlugin implements MCPPlugin {
    @Override
    public String name() {
        return "filesystem";
    }

    @Override
    public String description() {
        return "Filesystem operations plugin";
    }

    @Override
    public List<String> tools() {
        return List.of("read_file", "write_file", "list_directory", "create_directory", "delete_file");
    }

    @Override
    public Object callTool(String toolName, Map<String, Object> params) {
        return switch (toolName) {
            case "read_file" -> {
                String path = (String) params.getOrDefault("path", "");
                yield "Content of file: " + path;
            }
            case "write_file" -> {
                String path = (String) params.getOrDefault("path", "");
                String content = (String) params.getOrDefault("content", "");
                yield "Written " + content.length() + " bytes to " + path;
            }
            case "list_directory" -> {
                String path = (String) params.getOrDefault("path", ".");
                yield List.of("file1.txt", "file2.txt", "subdir/");
            }
            case "create_directory" -> {
                String path = (String) params.getOrDefault("path", "");
                yield "Created directory: " + path;
            }
            case "delete_file" -> {
                String path = (String) params.getOrDefault("path", "");
                yield "Deleted: " + path;
            }
            default -> "Unknown tool: " + toolName;
        };
    }

    @Override
    public Map<String, Object> toolSpec(String toolName) {
        return Map.of(
                "name", toolName,
                "description", "Filesystem tool: " + toolName,
                "input_schema", Map.of("type", "object", "properties", Map.of())
        );
    }
}
