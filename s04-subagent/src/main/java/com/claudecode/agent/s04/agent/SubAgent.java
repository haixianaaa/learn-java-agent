package com.claudecode.agent.s04.agent;

import com.claudecode.agent.s04.client.LLMClient;
import com.claudecode.agent.s04.model.*;
import com.claudecode.agent.s04.tool.ToolRegistry;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.*;

@Data
@Builder
public class SubAgent {
    private static final String MODEL = "deepseek-chat";
    private static final int MAX_TURNS = 30;

    private final LLMClient client;
    private final ToolRegistry tools;
    private final String systemPrompt;
    private final List<Message> context;
    private int turnCount;

    public SubAgent(LLMClient client, ToolRegistry tools, String systemPrompt) {
        this.client = client;
        this.tools = tools;
        this.systemPrompt = systemPrompt;
        this.context = new ArrayList<>();
        this.turnCount = 0;
    }

    public String run(String prompt) throws IOException {
        context.add(Message.text("user", prompt));

        while (turnCount < MAX_TURNS) {
            if (!runOneTurn()) {
                break;
            }
            turnCount++;
        }

        for (int i = context.size() - 1; i >= 0; i--) {
            Message msg = context.get(i);
            if (msg.getRole().equals("assistant")) {
                return extractText(msg);
            }
        }

        return "(no summary)";
    }

    private boolean runOneTurn() throws IOException {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .model(MODEL)
                .messages(context)
                .maxTokens(8000)
                .system(systemPrompt)
                .tools(tools.getAllToolSpecs())
                .build();

        CreateMessageResponse response = client.createMessage(request);
        context.add(Message.blocks("assistant", response.getContent()));

        String stopReason = response.getStopReason();
        if (stopReason != null && !stopReason.equals("tool_use")) {
            return false;
        }

        List<ContentBlock> toolResults = executeToolCall(response.getContent());
        context.add(Message.blocks("user", toolResults));
        return true;
    }

    private List<ContentBlock> executeToolCall(List<ContentBlock> content) {
        List<ContentBlock> results = new ArrayList<>();
        for (ContentBlock block : content) {
            if (block instanceof ContentBlock.ToolUseBlock toolUse) {
                String output = execute(toolUse.getName(), toolUse.getInput());
                results.add(ContentBlock.toolResult(toolUse.getId(), output));
            }
        }
        return results;
    }

    private String execute(String name, Map<String, Object> input) {
        try {
            var tool = tools.get(name);
            if (tool == null) {
                return "Unknown tool: " + name;
            }
            return tool.invoke(input);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String extractText(Message message) {
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
