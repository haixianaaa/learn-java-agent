package com.claudecode.agent.s11.tool;

import com.claudecode.agent.s11.model.Tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ReadFileTool implements ToolExecutor {
    private static final int MAX_OUTPUT_LENGTH = 50000;

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String pathStr = (String) input.get("path");
        if (pathStr == null) {
            throw new IllegalArgumentException("Invalid path");
        }

        Path path = safePath(pathStr);

        if (!Files.exists(path)) {
            throw new RuntimeException("Error: File not found: " + pathStr);
        }

        String content = Files.readString(path, StandardCharsets.UTF_8);

        Integer limit = null;
        if (input.containsKey("limit")) {
            Object limitObj = input.get("limit");
            if (limitObj instanceof Number) {
                limit = ((Number) limitObj).intValue();
            }
        }

        List<String> lines = content.lines().toList();

        if (limit != null && limit < lines.size()) {
            int remaining = lines.size() - limit;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                result.append(lines.get(i)).append("\n");
            }
            result.append("... (").append(remaining).append(" more lines)");
            String output = result.toString();
            if (output.length() > MAX_OUTPUT_LENGTH) {
                return output.substring(0, MAX_OUTPUT_LENGTH);
            }
            return output;
        }

        if (content.length() > MAX_OUTPUT_LENGTH) {
            return content.substring(0, MAX_OUTPUT_LENGTH);
        }
        return content;
    }

    private Path safePath(String pathStr) throws IOException {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path fullPath = cwd.resolve(pathStr).toAbsolutePath().normalize();
        
        if (!fullPath.startsWith(cwd.toAbsolutePath().normalize())) {
            throw new IOException("Path escapes workspace");
        }
        
        return fullPath;
    }

    @Override
    public String name() {
        return "read_file";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("read_file")
                .description("Read file contents.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "path", Map.of("type", "string"),
                                "limit", Map.of("type", "integer")
                        ),
                        "required", List.of("path")
                ))
                .build();
    }
}
