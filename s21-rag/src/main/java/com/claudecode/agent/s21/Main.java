package com.claudecode.agent.s21;

import com.claudecode.agent.s21.rag.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RAGPipeline pipeline = new RAGPipeline();

        pipeline.indexDocument(
                "Java Agent 是一种在 JVM 层面对字节码进行操作的技术。Java Agent 通过 Instrumentation API 在类加载时或运行时修改字节码，实现无侵入式的功能增强。常见的应用场景包括：APM（应用性能监控）、链路追踪、热部署、Mock 测试等。",
                Map.of("source", "java-agent-intro.txt", "topic", "java-agent")
        );

        pipeline.indexDocument(
                "RAG（Retrieval-Augmented Generation）是一种将检索与生成结合的技术。它先从知识库中检索与用户查询相关的文档片段，再将这些片段作为上下文注入到 LLM 的提示词中，从而让模型的回答基于事实而非幻觉。RAG 的核心流程包括：文档加载、文本分块、向量嵌入、相似度检索、上下文注入。",
                Map.of("source", "rag-intro.txt", "topic", "rag")
        );

        pipeline.indexDocument(
                "向量嵌入是将文本转换为高维数值向量的过程。嵌入模型将语义相近的文本映射到向量空间中距离较近的位置，使得我们可以通过计算向量间的余弦相似度来衡量文本的语义相关性。常用的嵌入模型有 OpenAI text-embedding-ada-002、BGE、E5 等。",
                Map.of("source", "embedding-intro.txt", "topic", "embedding")
        );

        pipeline.indexDocument(
                "文本分块（Chunking）是 RAG 流程中的关键步骤。它将长文档切分为较小的片段，以便嵌入模型处理和向量存储检索。常见的分块策略包括：固定长度分块、按句子分块、按段落分块、递归分块。分块时通常设置重叠区域（overlap），以避免语义在切分边界处断裂。",
                Map.of("source", "chunking-intro.txt", "topic", "chunking")
        );

        pipeline.indexDocument(
                "在 Agent 架构中集成 RAG，可以让 Agent 在执行任务时动态获取外部知识。Agent 可以通过 RAG Tool 在 agent loop 中按需检索文档，将检索结果作为 tool_result 返回给模型，模型据此做出更准确的决策。这种方式比一次性把所有文档塞入上下文更高效，也避免了超出上下文窗口的问题。",
                Map.of("source", "agent-rag.txt", "topic", "agent-rag")
        );

        System.out.println("S21 - RAG (Retrieval-Augmented Generation)");
        System.out.println("Indexed chunks: " + pipeline.getIndexedChunkCount());
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  index <text>             - Index a document");
        System.out.println("  query <question>         - Query and retrieve context");
        System.out.println("  search <question>        - Search with scores");
        System.out.println("  prompt <question>        - Build full RAG prompt");
        System.out.println("  stats                    - Show pipeline stats");
        System.out.println("  exit()                   - Exit");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("index ")) {
                String text = input.substring(6);
                pipeline.indexDocument(text, Map.of("source", "user-input", "topic", "user"));
                System.out.println("Indexed. Total chunks: " + pipeline.getIndexedChunkCount());
            } else if (input.startsWith("query ")) {
                String question = input.substring(6);
                String context = pipeline.query(question);
                if (context.isEmpty()) {
                    System.out.println("No relevant context found.");
                } else {
                    System.out.println("Retrieved context:\n" + context);
                }
            } else if (input.startsWith("search ")) {
                String question = input.substring(7);
                List<VectorStore.SearchResult> results = pipeline.queryWithScores(question);
                if (results.isEmpty()) {
                    System.out.println("No results found.");
                } else {
                    for (VectorStore.SearchResult result : results) {
                        System.out.printf("  [%.4f] %s%n", result.getScore(), result.getChunk().getContent().substring(0, Math.min(100, result.getChunk().getContent().length())) + "...");
                    }
                }
            } else if (input.startsWith("prompt ")) {
                String question = input.substring(7);
                String fullPrompt = pipeline.buildPrompt("You are a helpful assistant.", question);
                System.out.println("=== Full RAG Prompt ===");
                System.out.println(fullPrompt);
            } else if (input.equals("stats")) {
                System.out.println("Pipeline stats:");
                System.out.println("  Indexed chunks: " + pipeline.getIndexedChunkCount());
                System.out.println("  Embedding dimension: " + pipeline.getEmbedding().getDimension());
                System.out.println("  Vector store size: " + pipeline.getVectorStore().size());
            } else {
                System.out.println("Unknown command. Type 'exit()' to quit.");
            }
        }

        scanner.close();
    }
}
