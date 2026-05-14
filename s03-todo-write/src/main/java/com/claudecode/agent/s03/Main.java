package com.claudecode.agent.s03;

import com.claudecode.agent.s03.client.LLMClient;
import com.claudecode.agent.s03.model.ContentBlock;
import com.claudecode.agent.s03.model.CreateMessageResponse;
import com.claudecode.agent.s03.model.Message;
import com.claudecode.agent.s03.tool.ToolRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String MODEL = "deepseek-chat";
    private static final String SYSTEM = """
            You are a coding agent.
            Use the todo tool for multi-step work.
            Keep exactly one step in_progress when a task has multiple steps.
            Refresh the plan as work advances. Prefer tools over prose.
            """;

    public static void main(String[] args) {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        String baseUrl = System.getenv("ANTHROPIC_BASE_URL");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: ANTHROPIC_API_KEY environment variable is not set");
            return;
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            System.err.println("Error: ANTHROPIC_BASE_URL environment variable is not set");
            return;
        }

        LLMClient client = new LLMClient(baseUrl, apiKey);
        ToolRegistry tools = new ToolRegistry();
        LoopState state = new LoopState(client, tools);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Agent started. Type 'exit()' to quit.");

        while (true) {
            System.out.print("\n--- How can I help you? ");
            String query = scanner.nextLine().trim();

            if (query.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            state.addToContext(Message.text("user", query));

            try {
                agentLoop(state);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

            Message finalMessage = state.getLastMessage();
            if (finalMessage != null) {
                String finalContent = extractText(finalMessage.getContent());
                System.out.println("\n--- Final response:\n" + finalContent);
            }
        }

        scanner.close();
    }

    private static void agentLoop(LoopState state) throws IOException {
        while (true) {
            CreateMessageResponse response = state.createMessage(MODEL, SYSTEM);

            state.addToContext(Message.blocks("assistant", response.getContent()));

            String stopReason = response.getStopReason();
            if (stopReason != null && !stopReason.equals("tool_use")) {
                return;
            }

            List<ContentBlock> toolResults = state.executeToolCall(response.getContent());
            state.addToContext(Message.blocks("user", toolResults));
        }
    }

    private static String extractText(List<ContentBlock> content) {
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : content) {
            if (block instanceof ContentBlock.TextBlock textBlock) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(textBlock.getText());
            }
        }
        return sb.toString();
    }
}
