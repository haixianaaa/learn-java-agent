package com.claudecode.agent.s21.rag;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Chunker {
    private final AtomicInteger chunkIdCounter = new AtomicInteger(0);
    private final int chunkSize;
    private final int overlapSize;

    public Chunker(int chunkSize, int overlapSize) {
        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
    }

    public Chunker() {
        this(500, 50);
    }

    public List<DocumentChunk> chunk(Document document) {
        String content = document.getContent();
        List<DocumentChunk> chunks = new ArrayList<>();

        if (content.length() <= chunkSize) {
            chunks.add(createChunk(document, content, 0, content.length()));
            return chunks;
        }

        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());

            if (end < content.length()) {
                int lastPeriod = content.lastIndexOf('。', end);
                int lastNewline = content.lastIndexOf('\n', end);
                int lastSpace = content.lastIndexOf(' ', end);
                int breakPoint = Math.max(Math.max(lastPeriod, lastNewline), lastSpace);

                if (breakPoint > start) {
                    end = breakPoint + 1;
                }
            }

            String chunkContent = content.substring(start, end).trim();
            if (!chunkContent.isEmpty()) {
                chunks.add(createChunk(document, chunkContent, start, end));
            }

            start = end - overlapSize;
            if (start >= content.length()) break;
        }

        return chunks;
    }

    public List<DocumentChunk> chunkAll(List<Document> documents) {
        List<DocumentChunk> allChunks = new ArrayList<>();
        for (Document doc : documents) {
            allChunks.addAll(chunk(doc));
        }
        return allChunks;
    }

    private DocumentChunk createChunk(Document document, String content, int startIndex, int endIndex) {
        String chunkId = "chunk-" + chunkIdCounter.incrementAndGet();
        Map<String, String> metadata = new HashMap<>(document.getMetadata());
        metadata.put("chunk_index", String.valueOf(startIndex));
        return new DocumentChunk(chunkId, document.getId(), content, startIndex, endIndex, metadata);
    }
}
