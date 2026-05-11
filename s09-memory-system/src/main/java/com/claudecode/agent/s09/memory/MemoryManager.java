package com.claudecode.agent.s09.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MemoryManager {
    public static final String MEMORY_INDEX_FILE = "MEMORY.md";
    public static final int MAX_INDEX_LINES = 200;
    public static final String MEMORY_GUIDANCE = """
            
            When to save memories:
            - User states a preference ("I like tabs", "always use pytest") -> type: user
            - User corrects you ("don't do X", "that was wrong because...") -> type: feedback
            - You learn a project fact that is not easy to infer from current code alone
              (for example: a rule exists because of compliance, or a legacy module must
              stay untouched for business reasons) -> type: project
            - You learn where an external resource lives (ticket board, dashboard, docs URL)
              -> type: reference

            When NOT to save:
            - Anything easily derivable from code (function signatures, file structure, directory layout)
            - Temporary task state (current branch, open PR numbers, current TODOs)
            - Secrets or credentials (API keys, passwords)
            """;

    private final Path memoryDir;
    private final Map<String, MemoryEntry> memories = new HashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public MemoryManager(Path memoryDir) {
        this.memoryDir = memoryDir;
    }

    public void loadAll() throws IOException {
        memories.clear();

        if (!Files.exists(memoryDir)) {
            return;
        }

        Files.walkFileTree(memoryDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".md") && !fileName.equals(MEMORY_INDEX_FILE)) {
                    loadMemory(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadMemory(Path file) throws IOException {
        String content = Files.readString(file);
        ParsedMemory parsed = parseFrontmatter(content);

        String fallbackName = file.getFileName().toString().replace(".md", "");
        String name = parsed.name != null ? parsed.name : fallbackName;
        String description = parsed.description != null ? parsed.description : "";
        MemoryType type = parsed.type != null ? parseMemoryType(parsed.type) : MemoryType.PROJECT;

        MemoryEntry entry = new MemoryEntry(name, description, type, parsed.body);
        memories.put(name, entry);
    }

    public String loadMemoryPrompt() {
        if (memories.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Memories (persistent across sessions)\n\n");

        for (MemoryType type : MemoryType.values()) {
            List<MemoryEntry> typed = memories.values().stream()
                    .filter(e -> e.getMemoryType() == type)
                    .sorted(Comparator.comparing(MemoryEntry::getName))
                    .toList();

            if (!typed.isEmpty()) {
                sb.append("## [").append(type.getValue()).append("]\n");
                for (MemoryEntry entry : typed) {
                    sb.append(entry.toString().trim()).append("\n\n");
                }
            }
        }

        return sb.toString().trim();
    }

    public String saveMemory(String name, String description, MemoryType type, String content) throws IOException {
        String safeName = sanitizeName(name);
        if (safeName.isEmpty()) {
            return "Error: invalid memory name";
        }

        Files.createDirectories(memoryDir);

        Path filePath = memoryDir.resolve(safeName + ".md");
        String frontmatter = String.format("---\nname: %s\ndescription: %s\ntype: %s\n---\n%s\n",
                name, description, type.getValue(), content);
        Files.writeString(filePath, frontmatter);

        memories.put(name, new MemoryEntry(name, description, type, content));
        rebuildIndex();

        Path relativePath = memoryDir.relativize(filePath);
        return String.format("Saved memory '%s' [%s] to %s", name, type.getValue(), relativePath);
    }

    private void rebuildIndex() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Memory Index\n\n");

        List<MemoryEntry> sorted = memories.values().stream()
                .sorted(Comparator.comparing(MemoryEntry::getName))
                .toList();

        int lineCount = 2;
        for (MemoryEntry entry : sorted) {
            sb.append("- ").append(entry.getName()).append(": ")
                    .append(entry.getDescription()).append(" [")
                    .append(entry.getMemoryType().getValue()).append("]\n");
            lineCount++;

            if (lineCount >= MAX_INDEX_LINES) {
                sb.append("... (truncated at ").append(MAX_INDEX_LINES).append(" lines)\n");
                break;
            }
        }

        Files.writeString(memoryDir.resolve(MEMORY_INDEX_FILE), sb.toString());
    }

    public String describeMemories() {
        if (memories.isEmpty()) {
            return "  (no memories)";
        }

        StringBuilder sb = new StringBuilder();
        memories.values().stream()
                .sorted(Comparator.comparing(MemoryEntry::getName))
                .forEach(e -> sb.append("  [").append(e.getMemoryType().getValue())
                        .append("] ").append(e.getName()).append(": ")
                        .append(e.getDescription()).append("\n"));
        return sb.toString().trim();
    }

    public Map<String, MemoryEntry> getMemories() {
        return Collections.unmodifiableMap(memories);
    }

    private ParsedMemory parseFrontmatter(String text) {
        String normalized = text.replace("\r\n", "\n");

        if (!normalized.startsWith("---\n")) {
            return new ParsedMemory(null, null, null, normalized.trim());
        }

        String rest = normalized.substring(4);
        int endIndex = rest.indexOf("\n---\n");
        if (endIndex == -1) {
            return new ParsedMemory(null, null, null, normalized.trim());
        }

        String frontmatter = rest.substring(0, endIndex);
        String body = rest.substring(endIndex + 5).trim();

        try {
            Map<String, Object> meta = yamlMapper.readValue(frontmatter, Map.class);
            String name = (String) meta.get("name");
            String description = (String) meta.get("description");
            String type = (String) meta.get("type");
            return new ParsedMemory(name, description, type, body);
        } catch (Exception e) {
            return new ParsedMemory(null, null, null, body);
        }
    }

    private String sanitizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9_-]", "_")
                .replaceAll("^_+|_+$", "");
    }

    private MemoryType parseMemoryType(String value) {
        for (MemoryType type : MemoryType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MemoryType.PROJECT;
    }

    private record ParsedMemory(String name, String description, String type, String body) {}
}
