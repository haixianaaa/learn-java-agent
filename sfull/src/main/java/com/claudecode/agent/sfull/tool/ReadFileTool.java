package com.claudecode.agent.sfull.tool;

import com.claudecode.agent.sfull.model.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ReadFileTool implements ToolExecutor {
    private static final int MAX_OUTPUT_LENGTH = 50000;

    @Override
    public String invoke(ToolContext context, Map<String, Object> input) throws Exception {
        String pathStr = (String) input.get("path");
        if (pathStr == null) {
            throw new IllegalArgumentException("Invalid path");
        }

        Path path = safePath(context.getWorkDir(), pathStr);

        if (!Files.exists(path)) {
            throw new RuntimeException("Error: File not found: " + path);
        }

        String content = Files.readString(path);

        Integer limit = input.containsKey("limit") ? ((Number) input.get("limit")).intValue() : null;

        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();

        int lineCount = limit != null && limit < lines.length ? limit : lines.length;
        for (int i = 0; i < lineCount; i++) {
            result.append(lines[i]).append("\n");
        }

        if (limit != null && limit < lines.length) {
            int remaining = lines.length - limit;
            result.append("... (").append(remaining).append(" more lines)");
        }

        String output = result.toString();
        if (output.length() > MAX_OUTPUT_LENGTH) {
            return output.substring(0, MAX_OUTPUT_LENGTH);
        }
        return output;
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

    private Path safePath(Path workDir, String path) throws IOException {
        Path full = workDir.resolve(path).toAbsolutePath().normalize();
        if (!full.startsWith(workDir.toAbsolutePath().normalize())) {
            throw new IOException("Path escapes workspace");
        }
        return full;
    }
}
