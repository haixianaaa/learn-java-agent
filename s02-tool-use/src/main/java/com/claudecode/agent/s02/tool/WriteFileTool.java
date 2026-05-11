package com.claudecode.agent.s02.tool;

import com.claudecode.agent.s02.model.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WriteFileTool implements ToolExecutor {
    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String pathStr = (String) input.get("path");
        String content = (String) input.get("content");

        if (pathStr == null) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (content == null) {
            throw new IllegalArgumentException("Invalid content");
        }

        Path path = safePath(pathStr);

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Files.writeString(path, content);

        return "Wrote " + content.length() + " bytes to " + path;
    }

    @Override
    public String name() {
        return "write_file";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("write_file")
                .description("Write content to file.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "path", Map.of("type", "string"),
                                "content", Map.of("type", "string")
                        ),
                        "required", List.of("path", "content")
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
