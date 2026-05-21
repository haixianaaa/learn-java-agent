package com.claudecode.agent.s21.rag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VectorStore {
    private final Embedding embedding;
    private final Map<String, float[]> vectors = new ConcurrentHashMap<>();
    private final Map<String, DocumentChunk> chunks = new ConcurrentHashMap<>();

    public VectorStore(Embedding embedding) {
        this.embedding = embedding;
    }

    public void addChunk(DocumentChunk chunk) {
        float[] vector = embedding.embed(chunk.getContent());
        vectors.put(chunk.getId(), vector);
        chunks.put(chunk.getId(), chunk);
    }

    public void addChunks(List<DocumentChunk> chunkList) {
        for (DocumentChunk chunk : chunkList) {
            addChunk(chunk);
        }
    }

    public List<SearchResult> search(String query, int topK) {
        float[] queryVector = embedding.embed(query);

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, float[]> entry : vectors.entrySet()) {
            String chunkId = entry.getKey();
            float[] chunkVector = entry.getValue();
            float score = Embedding.cosineSimilarity(queryVector, chunkVector);
            results.add(new SearchResult(chunkId, score, chunks.get(chunkId)));
        }

        results.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
        return results.subList(0, Math.min(topK, results.size()));
    }

    public List<SearchResult> searchWithThreshold(String query, int topK, float minScore) {
        List<SearchResult> allResults = search(query, topK);
        List<SearchResult> filtered = new ArrayList<>();
        for (SearchResult result : allResults) {
            if (result.getScore() >= minScore) {
                filtered.add(result);
            }
        }
        return filtered;
    }

    public int size() {
        return chunks.size();
    }

    public boolean removeChunk(String chunkId) {
        vectors.remove(chunkId);
        return chunks.remove(chunkId) != null;
    }

    public void clear() {
        vectors.clear();
        chunks.clear();
    }

    public static class SearchResult {
        private final String chunkId;
        private final float score;
        private final DocumentChunk chunk;

        public SearchResult(String chunkId, float score, DocumentChunk chunk) {
            this.chunkId = chunkId;
            this.score = score;
            this.chunk = chunk;
        }

        public String getChunkId() { return chunkId; }
        public float getScore() { return score; }
        public DocumentChunk getChunk() { return chunk; }

        @Override
        public String toString() {
            return String.format("SearchResult{chunkId='%s', score=%.4f, content='%s'}",
                    chunkId, score,
                    chunk.getContent().length() > 80 ? chunk.getContent().substring(0, 80) + "..." : chunk.getContent());
        }
    }
}
