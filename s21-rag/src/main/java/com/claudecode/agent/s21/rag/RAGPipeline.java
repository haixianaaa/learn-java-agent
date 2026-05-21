package com.claudecode.agent.s21.rag;

import java.util.List;

public class RAGPipeline {
    private final DocumentLoader documentLoader;
    private final Chunker chunker;
    private final Embedding embedding;
    private final VectorStore vectorStore;
    private final RAGRetriever retriever;

    public RAGPipeline() {
        this.embedding = new Embedding(64);
        this.documentLoader = new DocumentLoader();
        this.chunker = new Chunker(500, 50);
        this.vectorStore = new VectorStore(embedding);
        this.retriever = new RAGRetriever(vectorStore);
    }

    public RAGPipeline(int chunkSize, int overlapSize, int embeddingDimension, int topK) {
        this.embedding = new Embedding(embeddingDimension);
        this.documentLoader = new DocumentLoader();
        this.chunker = new Chunker(chunkSize, overlapSize);
        this.vectorStore = new VectorStore(embedding);
        this.retriever = new RAGRetriever(vectorStore).withTopK(topK);
    }

    public void indexDocument(String content) {
        indexDocument(content, null);
    }

    public void indexDocument(String content, java.util.Map<String, String> metadata) {
        Document doc = documentLoader.loadFromString(content, metadata);
        List<DocumentChunk> chunks = chunker.chunk(doc);
        vectorStore.addChunks(chunks);
    }

    public String query(String userQuery) {
        return retriever.retrieveContext(userQuery);
    }

    public List<VectorStore.SearchResult> queryWithScores(String userQuery) {
        return retriever.retrieve(userQuery);
    }

    public String buildPrompt(String systemPrompt, String userQuery) {
        String context = query(userQuery);

        if (context.isEmpty()) {
            return systemPrompt + "\n\nUser: " + userQuery;
        }

        return systemPrompt
                + "\n\nThe following reference material was retrieved for this query:\n\n"
                + context
                + "User: " + userQuery;
    }

    public int getIndexedChunkCount() {
        return vectorStore.size();
    }

    public DocumentLoader getDocumentLoader() { return documentLoader; }
    public Chunker getChunker() { return chunker; }
    public Embedding getEmbedding() { return embedding; }
    public VectorStore getVectorStore() { return vectorStore; }
    public RAGRetriever getRetriever() { return retriever; }
}
