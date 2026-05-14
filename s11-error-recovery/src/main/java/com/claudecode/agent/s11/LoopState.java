package com.claudecode.agent.s11;

import com.claudecode.agent.s11.client.LLMClient;
import com.claudecode.agent.s11.model.*;
import com.claudecode.agent.s11.recovery.RecoveryConfig;
import com.claudecode.agent.s11.recovery.RecoveryState;
import com.claudecode.agent.s11.recovery.RecoveryUtils;
import com.claudecode.agent.s11.tool.ToolExecutor;
import com.claudecode.agent.s11.tool.ToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoopState {
    private static final String MODEL = "deepseek-chat";
    private static final String CONTINUATION_MESSAGE = 
            "Output limit hit. Continue directly from where you stopped. " +
            "No recap, no repetition. Pick up mid-sentence if needed.";

    private final LLMClient client;
    private final List<Message> context = new ArrayList<>();
    private final ToolRegistry tools;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public void agentLoop(String system) throws IOException, InterruptedException {
        RecoveryState recovery = new RecoveryState();

        while (true) {
            CreateMessageRequest request = CreateMessageRequest.builder()
                    .model(MODEL)
                    .messages(new ArrayList<>(context))
                    .maxTokens(8000)
                    .system(system)
                    .tools(tools.getAllToolSpecs())
                    .build();

            CreateMessageResponse response;
            try {
                response = client.createMessage(request);
                recovery.setTransportAttempts(0);
            } catch (IOException e) {
                LoopControl control = handleRequestErrorRecovery(e, recovery);
                if (control == LoopControl.STOP) {
                    return;
                }
                continue;
            }

            context.add(Message.blocks("assistant", response.getContent()));

            if ("max_tokens".equals(response.getStopReason()) && handleMaxTokensRecovery(recovery)) {
                continue;
            }

            recovery.setContinuationAttempts(0);

            String stopReason = response.getStopReason();
            if (stopReason != null && !stopReason.equals("tool_use")) {
                return;
            }

            executeToolCall(response.getContent());
            maybeAutoCompact();
        }
    }

    private enum LoopControl {
        CONTINUE, STOP
    }

    private LoopControl handleRequestErrorRecovery(IOException error, RecoveryState recovery) throws InterruptedException {
        String errorText = error.getMessage().toLowerCase();
        
        if (isPromptTooLongError(errorText)) {
            if (recovery.getCompactAttempts() >= RecoveryConfig.MAX_RECOVERY_ATTEMPTS) {
                System.out.printf("[Error] compact recovery exhausted after %d attempts: %s%n",
                        RecoveryConfig.MAX_RECOVERY_ATTEMPTS, error.getMessage());
                return LoopControl.STOP;
            }

            recovery.setCompactAttempts(recovery.getCompactAttempts() + 1);
            System.out.printf("[Recovery] compact (%d/%d): context too large%n",
                    recovery.getCompactAttempts(), RecoveryConfig.MAX_RECOVERY_ATTEMPTS);
            
            try {
                compactHistory();
            } catch (Exception e) {
                System.out.println("[Error] compact recovery failed: " + e.getMessage());
                return LoopControl.STOP;
            }
            return LoopControl.CONTINUE;
        }

        if (RecoveryUtils.isTransientTransportError(errorText)) {
            if (recovery.getTransportAttempts() >= RecoveryConfig.MAX_RECOVERY_ATTEMPTS) {
                System.out.printf("[Error] transport recovery exhausted after %d attempts: %s%n",
                        RecoveryConfig.MAX_RECOVERY_ATTEMPTS, error.getMessage());
                return LoopControl.STOP;
            }

            Duration delay = RecoveryUtils.backoffDelay(recovery.getTransportAttempts());
            recovery.setTransportAttempts(recovery.getTransportAttempts() + 1);
            System.out.printf("[Recovery] backoff (%d/%d): transient transport failure. Retrying in %.1fs%n",
                    recovery.getTransportAttempts(), RecoveryConfig.MAX_RECOVERY_ATTEMPTS, delay.toMillis() / 1000.0);
            Thread.sleep(delay);
            return LoopControl.CONTINUE;
        }

        System.out.println("[Error] API call failed: " + error.getMessage());
        return LoopControl.STOP;
    }

    private boolean handleMaxTokensRecovery(RecoveryState recovery) {
        if (recovery.getContinuationAttempts() >= RecoveryConfig.MAX_RECOVERY_ATTEMPTS) {
            System.out.printf("[Error] continuation recovery exhausted after %d attempts%n",
                    RecoveryConfig.MAX_RECOVERY_ATTEMPTS);
            return false;
        }

        recovery.setContinuationAttempts(recovery.getContinuationAttempts() + 1);
        System.out.printf("[Recovery] continue (%d/%d): output truncated%n",
                recovery.getContinuationAttempts(), RecoveryConfig.MAX_RECOVERY_ATTEMPTS);
        context.add(Message.text("user", CONTINUATION_MESSAGE));
        return true;
    }

    private void maybeAutoCompact() throws IOException {
        if (estimateContextSize() <= RecoveryConfig.CONTEXT_THRESHOLD_CHARS) {
            return;
        }

        System.out.println("[Recovery] compact: context estimate exceeded threshold");
        try {
            compactHistory();
        } catch (Exception e) {
            System.out.println("[Error] proactive compact failed: " + e.getMessage());
        }
    }

    public void compactHistory() throws IOException {
        String conversationText;
        try {
            conversationText = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to serialize conversation for summarization", e);
        }

        String truncated = conversationText.length() > 80000 
                ? conversationText.substring(0, 80000) 
                : conversationText;

        String prompt = String.format("""
                Summarize this conversation for continuity. Include:
                1) Task overview and success criteria
                2) Current state: completed work, files touched
                3) Key decisions and failed approaches
                4) Remaining next steps
                Be concise but preserve critical details.

                %s
                """, truncated);

        CreateMessageRequest request = CreateMessageRequest.builder()
                .model(MODEL)
                .messages(List.of(Message.text("user", prompt)))
                .maxTokens(4000)
                .build();

        CreateMessageResponse response = client.createMessage(request);
        String summary = extractText(response.getContent());

        context.clear();
        context.add(Message.text("user", String.format("""
                This session continues from a previous conversation that was compacted.
                Summary of prior context:

                %s

                Continue from where we left off without re-asking the user.
                """, summary)));
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

    private int estimateContextSize() {
        try {
            String serialized = objectMapper.writeValueAsString(context);
            return serialized.length();
        } catch (JsonProcessingException e) {
            return 0;
        }
    }

    private String extractText(List<ContentBlock> content) {
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : content) {
            if (block instanceof ContentBlock.TextBlock textBlock) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(textBlock.getText());
            }
        }
        return sb.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength);
    }

    private boolean isPromptTooLongError(String errorText) {
        return (errorText.contains("prompt") && errorText.contains("long"))
                || errorText.contains("overlong_prompt")
                || errorText.contains("too many tokens")
                || errorText.contains("context length");
    }
}
