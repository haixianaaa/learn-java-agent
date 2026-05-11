package com.claudecode.agent.sfull.agent;

import com.claudecode.agent.sfull.client.LLMClient;
import com.claudecode.agent.sfull.model.*;
import com.claudecode.agent.sfull.tool.ToolContext;
import com.claudecode.agent.sfull.tool.ToolRouter;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Data
@Builder
public class Agent {
    private static final String MODEL = "deepseek-chat";
    private static final String SYSTEM = """
            You are a coding agent.
            Use bash to inspect and change the workspace. Act first, then report clearly.
            Think step by step before acting.
            """;

    private LLMClient client;
    private ToolContext toolContext;
    private ToolRouter tools;
    private List<Message> context;

    public void agentLoop() throws IOException {
        while (runOneTurn()) {
            // Continue until done
        }
    }

    private boolean runOneTurn() throws IOException {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .model(MODEL)
                .messages(normalizeMessages(context))
                .maxTokens(8000)
                .system(buildSystemPrompt())
                .tools(tools.toolSpecs())
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
            String output = tools.call(toolContext, name, input);
            System.out.println("Command: " + name);
            System.out.println("Arg: " + input);
            System.out.println("Output:\n" + output.substring(0, Math.min(200, output.length())));
            return output;
        } catch (Exception e) {
            String errorMsg = "Error invoking tool " + name + ": " + e.getMessage();
            System.out.println(errorMsg);
            return errorMsg;
        }
    }

    private String buildSystemPrompt() {
        Path workdir = toolContext.getWorkDir();
        return SYSTEM + "\n\nWorking directory: " + workdir + "\nPlatform: " + System.getProperty("os.name");
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

    public static String extractText(Message message) {
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
