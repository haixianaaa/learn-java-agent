package com.claudecode.agent.s21.rag;

import java.util.List;

public class RAGRetriever {
    private final VectorStore vectorStore;
    private int topK = 3;
    private float minScore = 0.0f;

    public RAGRetriever(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public RAGRetriever withTopK(int topK) {
        this.topK = topK;
        return this;
    }

    public RAGRetriever withMinScore(float minScore) {
        this.minScore = minScore;
        return this;
    }

    public List<VectorStore.SearchResult> retrieve(String query) {
        if (minScore > 0) {
            return vectorStore.searchWithThreshold(query, topK, minScore);
        }
        return vectorStore.search(query, topK);
    }

    public String retrieveContext(String query) {
        List<VectorStore.SearchResult> results = retrieve(query);

        if (results.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            VectorStore.SearchResult result = results.get(i);
            DocumentChunk chunk = result.getChunk();

            sb.append(String.format("--- Context [%d] (score: %.4f, source: %s) ---%n",
                    i + 1, result.getScore(),
                    chunk.getMetadata().getOrDefault("source", "unknown")));
            sb.append(chunk.getContent());
            sb.append("\n\n");
        }

        return sb.toString();
    }

    public VectorStore getVectorStore() {
        return vectorStore;
    }
}
