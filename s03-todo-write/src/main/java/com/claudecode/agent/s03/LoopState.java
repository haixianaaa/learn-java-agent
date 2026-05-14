package com.claudecode.agent.s03;

import com.claudecode.agent.s03.client.LLMClient;
import com.claudecode.agent.s03.model.ContentBlock;
import com.claudecode.agent.s03.model.CreateMessageRequest;
import com.claudecode.agent.s03.model.CreateMessageResponse;
import com.claudecode.agent.s03.model.Message;
import com.claudecode.agent.s03.tool.ToolExecutor;
import com.claudecode.agent.s03.tool.ToolRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoopState {
    private static final int PLAN_REMINDER_INTERVAL = 3;
    
    private final LLMClient client;
    private final List<Message> context = new ArrayList<>();
    private final ToolRegistry tools;
    private int todoRoundsSinceUpdate = 0;

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

    @SuppressWarnings("unchecked")
    public List<ContentBlock> executeToolCall(List<ContentBlock> content) {
        List<ContentBlock> result = new ArrayList<>();
        boolean usedTodo = false;

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

                if ("todo".equals(name)) {
                    usedTodo = true;
                }
            }
        }

        if (usedTodo) {
            todoRoundsSinceUpdate = 0;
        } else {
            noteRoundWithoutUpdate();
            String reminder = reminder();
            if (reminder != null) {
                result.add(0, ContentBlock.text(reminder));
            }
        }

        return result;
    }

    public String reminder() {
        if (todoRoundsSinceUpdate >= PLAN_REMINDER_INTERVAL) {
            return "<reminder>Refresh your current plan before continuing.</reminder>";
        }
        return null;
    }

    public void noteRoundWithoutUpdate() {
        todoRoundsSinceUpdate++;
    }

    public CreateMessageResponse createMessage(String model, String system) throws IOException {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .model(model)
                .messages(new ArrayList<>(context))
                .maxTokens(8000)
                .system(system)
                .tools(tools.getAllToolSpecs())
                .build();

        return client.createMessage(request);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength);
    }
}
