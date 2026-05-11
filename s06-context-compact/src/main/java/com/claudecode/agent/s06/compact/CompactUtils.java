package com.claudecode.agent.s06.compact;

import com.claudecode.agent.s06.model.ContentBlock;
import com.claudecode.agent.s06.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CompactUtils {
    private static final int KEEP_RECENT_TOOL_RESULTS = 3;
    private static final int PERSIST_THRESHOLD = 30000;
    private static final int PREVIEW_CHARS = 2000;
    private static final String TRANSCRIPT_DIR = ".transcripts";
    private static final String OUTPUT_DIR = ".task_outputs/tool-results";
    private static final String COMPACTED_TOOL_RESULT = "[Earlier tool result compacted. Re-run the tool if you need full detail.]";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void microCompact(List<Message> messages) {
        List<int[]> toolResultPositions = collectToolResultPositions(messages);
        if (toolResultPositions.size() <= KEEP_RECENT_TOOL_RESULTS) {
            return;
        }

        int compactUntil = toolResultPositions.size() - KEEP_RECENT_TOOL_RESULTS;

        for (int i = 0; i < compactUntil; i++) {
            int[] pos = toolResultPositions.get(i);
            int messageIdx = pos[0];
            int blockIdx = pos[1];

            if (messageIdx >= messages.size()) continue;

            Message message = messages.get(messageIdx);
            Object content = message.getContent();

            if (content instanceof List<?> blocks) {
                if (blockIdx >= blocks.size()) continue;

                Object block = blocks.get(blockIdx);
                if (block instanceof ContentBlock.ToolResultBlock toolResult) {
                    if (toolResult.getContent().length() > 120) {
                        toolResult.setContent(COMPACTED_TOOL_RESULT);
                    }
                }
            }
        }
    }

    public static int estimateContextSize(List<Message> messages) {
        try {
            String serialized = objectMapper.writeValueAsString(messages);
            return serialized.length();
        } catch (Exception e) {
            return messages.stream()
                    .mapToInt(msg -> {
                        Object content = msg.getContent();
                        if (content instanceof String text) {
                            return text.length();
                        } else if (content instanceof List<?> blocks) {
                            return blocks.stream()
                                    .mapToInt(block -> {
                                        if (block instanceof ContentBlock.TextBlock textBlock) {
                                            return textBlock.getText().length();
                                        } else if (block instanceof ContentBlock.ToolUseBlock toolUse) {
                                            return toolUse.getName().length() + toolUse.getInput().toString().length();
                                        } else if (block instanceof ContentBlock.ToolResultBlock toolResult) {
                                            return toolResult.getContent().length();
                                        }
                                        return 0;
                                    })
                                    .sum();
                        }
                        return 0;
                    })
                    .sum();
        }
    }

    public static Path writeTranscript(List<Message> messages) throws IOException {
        Path transcriptDir = Paths.get(System.getProperty("user.dir"), TRANSCRIPT_DIR);
        Files.createDirectories(transcriptDir);

        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        Path transcriptPath = transcriptDir.resolve("transcript_" + timestamp + ".jsonl");

        try (BufferedWriter writer = Files.newBufferedWriter(transcriptPath)) {
            for (Message message : messages) {
                String json = objectMapper.writeValueAsString(message);
                writer.write(json);
                writer.newLine();
            }
        }

        return transcriptPath;
    }

    public static String persistLargeOutput(String toolUseId, String output) throws IOException {
        if (output.length() <= PERSIST_THRESHOLD) {
            return output;
        }

        Path outputDir = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve(toolUseId + ".txt");
        Files.writeString(outputPath, output);

        String preview = output.length() > PREVIEW_CHARS 
                ? output.substring(0, PREVIEW_CHARS) 
                : output;

        return String.format("<persisted-output>\nFull output saved to: %s\nPreview:\n%s\n</persisted-output>",
                outputPath, preview);
    }

    private static List<int[]> collectToolResultPositions(List<Message> messages) {
        List<int[]> positions = new ArrayList<>();

        for (int messageIdx = 0; messageIdx < messages.size(); messageIdx++) {
            Message message = messages.get(messageIdx);
            if (!message.getRole().equals("user")) continue;

            Object content = message.getContent();
            if (content instanceof List<?> blocks) {
                for (int blockIdx = 0; blockIdx < blocks.size(); blockIdx++) {
                    Object block = blocks.get(blockIdx);
                    if (block instanceof ContentBlock.ToolResultBlock) {
                        positions.add(new int[]{messageIdx, blockIdx});
                    }
                }
            }
        }

        return positions;
    }
}
