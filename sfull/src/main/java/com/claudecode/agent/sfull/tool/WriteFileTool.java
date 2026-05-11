package com.claudecode.agent.sfull.tool;

import com.claudecode.agent.sfull.model.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WriteFileTool implements ToolExecutor {
    @Override
    public String invoke(ToolContext context, Map<String, Object> input) throws Exception {
        String pathStr = (String) input.get("path");
        String content = (String) input.get("content");

        if (pathStr == null) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (content == null) {
            throw new IllegalArgumentException("Invalid content");
        }

        Path path = safePath(context.getWorkDir(), pathStr);

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

    private Path safePath(Path workDir, String path) throws IOException {
        Path full = workDir.resolve(path).toAbsolutePath().normalize();
        if (!full.startsWith(workDir.toAbsolutePath().normalize())) {
            throw new IOException("Path escapes workspace");
        }
        return full;
    }
}
