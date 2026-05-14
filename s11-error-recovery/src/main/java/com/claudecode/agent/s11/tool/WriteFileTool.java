package com.claudecode.agent.s11.tool;

import com.claudecode.agent.s11.model.Tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        Files.writeString(path, content, StandardCharsets.UTF_8);

        return "Wrote " + content.length() + " bytes to " + pathStr;
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
}
