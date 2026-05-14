package com.claudecode.agent.s11;

import com.claudecode.agent.s11.client.LLMClient;
import com.claudecode.agent.s11.model.ContentBlock;
import com.claudecode.agent.s11.model.Message;
import com.claudecode.agent.s11.tool.ToolRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
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

        System.out.println("Agent started with error recovery. Type 'exit()' to quit.");

        while (true) {
            System.out.print("\n--- How can I help you? ");
            String query = scanner.nextLine().trim();

            if (query.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            state.addToContext(Message.text("user", query));

            try {
                String system = String.format(
                        "You are a coding agent at %s. Use tools to solve tasks.",
                        System.getProperty("user.dir")
                );
                state.agentLoop(system);
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
