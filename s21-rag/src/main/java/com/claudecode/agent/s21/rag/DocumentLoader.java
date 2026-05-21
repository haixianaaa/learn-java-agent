package com.claudecode.agent.s21.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentLoader {
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public Document loadFromString(String content, Map<String, String> metadata) {
        String id = "doc-" + idCounter.incrementAndGet();
        return new Document(id, content, metadata != null ? metadata : Map.of());
    }

    public Document loadFromFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        String fileName = filePath.getFileName().toString();
        Map<String, String> metadata = Map.of(
                "source", filePath.toString(),
                "filename", fileName,
                "type", getFileExtension(fileName)
        );
        return loadFromString(content, metadata);
    }

    public List<Document> loadFromDirectory(Path dirPath) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (!Files.isDirectory(dirPath)) {
            return documents;
        }

        try (var stream = Files.walk(dirPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> isTextFile(p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            documents.add(loadFromFile(p));
                        } catch (IOException e) {
                            System.err.println("Failed to load: " + p + " - " + e.getMessage());
                        }
                    });
        }

        return documents;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "unknown";
    }

    private boolean isTextFile(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return Set.of("txt", "md", "java", "py", "js", "ts", "json", "xml", "yaml", "yml", "csv", "html", "css")
                .contains(ext);
    }
}
