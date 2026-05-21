package com.claudecode.agent.s21.rag;

import java.util.*;

public class Embedding {
    private final int dimension;
    private final Map<String, float[]> cache = new HashMap<>();

    public Embedding(int dimension) {
        this.dimension = dimension;
    }

    public Embedding() {
        this(64);
    }

    public float[] embed(String text) {
        if (cache.containsKey(text)) {
            return cache.get(text);
        }

        float[] vector = new float[dimension];
        Random random = new Random(text.hashCode());

        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (random.nextGaussian() * 0.1);
        }

        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            int seed = word.hashCode();
            Random wordRandom = new Random(seed);
            for (int i = 0; i < dimension; i++) {
                vector[i] += (float) (wordRandom.nextGaussian() * 0.05);
            }
        }

        normalize(vector);
        cache.put(text, vector.clone());
        return vector;
    }

    public float[][] embedBatch(List<String> texts) {
        float[][] results = new float[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            results[i] = embed(texts.get(i));
        }
        return results;
    }

    public static float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }

        float dotProduct = 0;
        float normA = 0;
        float normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return 0;

        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void normalize(float[] vector) {
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
    }

    public int getDimension() {
        return dimension;
    }
}
