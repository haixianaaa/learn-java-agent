package com.claudecode.agent.s02;

import com.claudecode.agent.s02.client.LLMClient;
import com.claudecode.agent.s02.model.*;
import com.claudecode.agent.s02.tool.ToolExecutor;
import com.claudecode.agent.s02.tool.ToolRegistry;

import java.io.IOException;
import java.util.*;

public class Main {
    private static final String MODEL = "mimo-v2.5-pro";
    private static final String SYSTEM = """
            You are a coding agent.
            Use bash to inspect and change the workspace. Act first, then report clearly.
            """;

    private final LLMClient client;
    private final ToolRegistry toolRegistry;
    private final List<Message> context;

    public Main() {
        this.client = new LLMClient();
        this.toolRegistry = new ToolRegistry();
        this.context = new ArrayList<>();
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
        while (true) {
            CreateMessageRequest request = CreateMessageRequest.builder()
                    .model(MODEL)
                    .messages(normalizeMessages(context))
                    .maxTokens(8000)
                    .system(SYSTEM)
                    .tools(toolRegistry.getAllToolSpecs())
                    .build();

            CreateMessageResponse response = client.createMessage(request);

            context.add(Message.blocks("assistant", response.getContent()));

            String stopReason = response.getStopReason();
            if (stopReason != null && !stopReason.equals("tool_use")) {
                return;
            }

            List<ContentBlock> toolResults = executeToolCall(response.getContent());
            context.add(Message.blocks("user", toolResults));
        }
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
        ToolExecutor tool = toolRegistry.get(name);
        if (tool == null) {
            return "Unknown tool: " + name;
        }

        try {
            String output = tool.invoke(input);
            System.out.println("Command: " + name);
            System.out.println("Arg: " + input);
            System.out.println("Output:\n" + output);
            return output;
        } catch (Exception e) {
            String errorMsg = "Error invoking tool " + name + ": " + e.getMessage();
            System.out.println(errorMsg);
            return errorMsg;
        }
    }

    private List<Message> normalizeMessages(List<Message> messages) {
        List<Message> result = new ArrayList<>(messages);

        Set<String> existingResults = new HashSet<>();
        for (Message msg : result) {
            if (msg.getContent() instanceof List<?> blocks) {
                for (Object block : blocks) {
                    if (block instanceof ContentBlock.ToolResultBlock toolResult) {
                        existingResults.add(toolResult.getToolUseId());
                    }
                }
            }
        }

        List<Message> extraMessages = new ArrayList<>();
        for (Message msg : result) {
            if (msg.getRole().equals("user")) continue;

            if (msg.getContent() instanceof List<?> blocks) {
                for (Object block : blocks) {
                    if (block instanceof ContentBlock.ToolUseBlock toolUse) {
                        if (!existingResults.contains(toolUse.getId())) {
                            extraMessages.add(Message.blocks("user",
                                    List.of(ContentBlock.toolResult(toolUse.getId(), "(cancelled)"))));
                        }
                    }
                }
            }
        }
        result.addAll(extraMessages);

        List<Message> merged = new ArrayList<>();
        for (Message msg : result) {
            if (!merged.isEmpty()) {
                Message last = merged.get(merged.size() - 1);
                if (last.getRole().equals(msg.getRole())) {
                    mergeContent(last, msg);
                    continue;
                }
            }
            merged.add(msg);
        }

        return merged;
    }

    @SuppressWarnings("unchecked")
    private void mergeContent(Message target, Message source) {
        Object targetContent = target.getContent();
        Object sourceContent = source.getContent();

        if (targetContent instanceof String t && sourceContent instanceof String s) {
            target.setContent(t + "\n" + s);
        } else if (targetContent instanceof List<?> t && sourceContent instanceof List<?> s) {
            List<ContentBlock> merged = new ArrayList<>((List<ContentBlock>) t);
            merged.addAll((List<ContentBlock>) s);
            target.setContent(merged);
        } else if (targetContent instanceof String t && sourceContent instanceof List<?> s) {
            List<ContentBlock> merged = new ArrayList<>();
            merged.add(ContentBlock.text(t));
            merged.addAll((List<ContentBlock>) s);
            target.setContent(merged);
        } else if (targetContent instanceof List<?> t && sourceContent instanceof String s) {
            List<ContentBlock> merged = new ArrayList<>((List<ContentBlock>) t);
            merged.add(ContentBlock.text(s));
            target.setContent(merged);
        }
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
