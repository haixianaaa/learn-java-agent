package com.claudecode.agent.s04.tool;

import com.claudecode.agent.s04.model.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class EditFileTool implements ToolExecutor {
    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String pathStr = (String) input.get("path");
        String oldText = (String) input.get("old_text");
        String newText = (String) input.get("new_text");

        if (pathStr == null) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (oldText == null) {
            throw new IllegalArgumentException("Invalid old_text");
        }
        if (newText == null) {
            throw new IllegalArgumentException("Invalid new_text");
        }

        Path path = safePath(pathStr);

        if (!Files.exists(path)) {
            throw new RuntimeException("Error: File not found: " + path);
        }

        String content = Files.readString(path);

        if (!content.contains(oldText)) {
            throw new RuntimeException("Error: Text not found in " + path);
        }

        String updated = content.replaceFirst(oldText.replace("$", "\\$").replace("\\", "\\\\"), newText);
        Files.writeString(path, updated);

        return "Edited " + path;
    }

    @Override
    public String name() {
        return "edit_file";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("edit_file")
                .description("Replace exact text in file.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "path", Map.of("type", "string"),
                                "old_text", Map.of("type", "string"),
                                "new_text", Map.of("type", "string")
                        ),
                        "required", List.of("path", "old_text", "new_text")
                ))
                .build();
    }

    private Path safePath(String path) throws IOException {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path full = cwd.resolve(path).toAbsolutePath().normalize();
        if (!full.startsWith(cwd)) {
            throw new IOException("Path escapes workspace");
        }
        return full;
    }
}
