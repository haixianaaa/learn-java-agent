package com.claudecode.agent.s12;

import com.claudecode.agent.s12.client.LLMClient;
import com.claudecode.agent.s12.model.*;
import com.claudecode.agent.s12.tool.ToolExecutor;
import com.claudecode.agent.s12.tool.ToolRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoopState {
    private static final String MODEL = "deepseek-chat";

    private final LLMClient client;
    private final List<Message> context = new ArrayList<>();
    private final ToolRegistry tools;

    public LoopState(LLMClient client, ToolRegistry tools) {
        this.client = client;
        this.tools = tools;
    }

    public void addToContext(Message message) {
        context.add(message);
    }

    public List<Message> getContext() {
        return new ArrayList<>(context);
    }

    public Message getLastMessage() {
        return context.isEmpty() ? null : context.get(context.size() - 1);
    }

    public void agentLoop(String system) throws IOException {
        while (true) {
            CreateMessageRequest request = CreateMessageRequest.builder()
                    .model(MODEL)
                    .messages(new ArrayList<>(context))
                    .maxTokens(8000)
                    .system(system)
                    .tools(tools.getAllToolSpecs())
                    .build();

            CreateMessageResponse response = client.createMessage(request);

            context.add(Message.blocks("assistant", response.getContent()));

            String stopReason = response.getStopReason();
            if (stopReason != null && !stopReason.equals("tool_use")) {
                return;
            }

            executeToolCall(response.getContent());
        }
    }

    @SuppressWarnings("unchecked")
    public void executeToolCall(List<ContentBlock> content) {
        List<ContentBlock> result = new ArrayList<>();

        for (ContentBlock block : content) {
            if (block instanceof ContentBlock.ToolUseBlock toolUse) {
                String id = toolUse.getId();
                String name = toolUse.getName();
                Map<String, Object> input = toolUse.getInput();

                String output;
                try {
                    ToolExecutor tool = tools.get(name);
                    if (tool == null) {
                        output = "Unknown tool: " + name;
                    } else {
                        output = tool.invoke(input);
                        System.out.println("Command: " + name);
                        System.out.println("Arg: " + input);
                        System.out.println("Output:\n" + truncate(output, 200));
                    }
                } catch (Exception e) {
                    output = "Error invoking tool " + name + ": " + e.getMessage();
                    System.out.println("Error invoking tool " + name + ": " + e.getMessage());
                }

                result.add(ContentBlock.toolResult(id, output));
            }
        }

        context.add(Message.blocks("user", result));
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength);
    }
}
