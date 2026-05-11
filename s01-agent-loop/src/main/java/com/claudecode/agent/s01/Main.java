package com.claudecode.agent.s01;

import com.claudecode.agent.s01.client.LLMClient;
import com.claudecode.agent.s01.model.*;
import com.claudecode.agent.s01.tool.BashTool;

import java.io.IOException;
import java.util.*;

public class Main {
    private static final String MODEL = "mimo-v2.5-pro";
    private static final String SYSTEM = """
            You are a coding agent.
            Use bash to inspect and change the workspace. Act first, then report clearly.
            """;

    private final LLMClient client;
    private final BashTool bashTool;
    private final List<Message> context;
    private int turnCount;
    private String transitionReason;

    public Main() {
        this.client = new LLMClient();
        this.bashTool = new BashTool();
        this.context = new ArrayList<>();
        this.turnCount = 1;
        this.transitionReason = null;
    }

    public static void main(String[] args) {
        Main agent = new Main();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Agent started. Type 'exit()' to quit.");

        while (true) {
            System.out.print("\n--- How can I help you? ");
            String query = scanner.nextLine().trim();

            if (query.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            agent.context.add(Message.text("user", query));

            try {
                agent.agentLoop();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

            if (!agent.context.isEmpty()) {
                Message lastMessage = agent.context.get(agent.context.size() - 1);
                System.out.println("--- Final response:\n" + extractText(lastMessage));
            }
        }

        scanner.close();
    }

    private void agentLoop() throws IOException {
        while (runOneTurn()) {
            // Continue until done
        }
    }

    private boolean runOneTurn() throws IOException {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .model(MODEL)
                .messages(new ArrayList<>(context))
                .maxTokens(8000)
                .system(SYSTEM)
                .tools(Collections.singletonList(Tool.bash()))
                .build();

        CreateMessageResponse response = client.createMessage(request);

        context.add(Message.blocks("assistant", response.getContent()));

        String stopReason = response.getStopReason();
        if (stopReason != null && !stopReason.equals("tool_use")) {
            transitionReason = null;
            return false;
        }

        List<ContentBlock> toolResults = executeToolCall(response.getContent());
        if (toolResults.isEmpty()) {
            transitionReason = null;
            return false;
        }

        context.add(Message.blocks("user", toolResults));
        turnCount++;
        transitionReason = "tool_result";
        return true;
    }

    private List<ContentBlock> executeToolCall(List<ContentBlock> content) {
        List<ContentBlock> results = new ArrayList<>();
        boolean hasToolUse = false;

        for (ContentBlock block : content) {
            if (block instanceof ContentBlock.ToolUseBlock toolUse) {
                if (toolUse.getName().equals("bash")) {
                    hasToolUse = true;
                    String command = (String) toolUse.getInput().get("command");
                    String output = bashTool.execute(command);

                    System.out.println("Command: " + command);
                    System.out.println("Output: " + output);

                    results.add(ContentBlock.toolResult(toolUse.getId(), output));
                }
            }
        }

        return results;
    }

    private static String extractText(Message message) {
        Object content = message.getContent();
        if (content instanceof String text) {
            return text;
        } else if (content instanceof List<?> blocks) {
            StringBuilder sb = new StringBuilder();
            for (Object block : blocks) {
                if (block instanceof ContentBlock.TextBlock textBlock) {
                    sb.append(textBlock.getText()).append("\n");
                }
            }
            return sb.toString().trim();
        }
        return "";
    }
}
