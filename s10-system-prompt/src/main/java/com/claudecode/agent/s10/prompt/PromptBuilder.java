package com.claudecode.agent.s10.prompt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class PromptBuilder {
    private static final String MODEL = "deepseek-chat";

    public String buildSystemPrompt(String workDir) {
        return SystemPrompt.builder()
                .role(String.format("You are a coding agent operating in %s.", workDir))
                .guideline("Try to understand how to complete the task well before completing it.")
                .constraint("Think step by step")
                .constraint("Think before you act; respond with your thoughts before calling tools")
                .constraint("Do not make up any assumptions, use tools to get the information you need")
                .constraint("Use the provided tools to interact with the system and accomplish the task")
                .constraint("If you are stuck, or otherwise cannot complete the task, respond with your thoughts and stop")
                .constraint("If the task is completed, or otherwise cannot continue, like requiring user feedback, stop.")
                .claudeMd(loadClaudeMdPrompt(Paths.get(workDir)))
                .dynamicContext(loadDynamicContext(Paths.get(workDir)))
                .build()
                .render();
    }

    private String loadDynamicContext(Path workdir) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Dynamic context\n");
        sb.append("Current date: ").append(LocalDate.now()).append("\n");
        sb.append("Working directory: ").append(workdir).append("\n");
        sb.append("Model: ").append(MODEL).append("\n");
        sb.append("Platform: ").append(System.getProperty("os.name")).append("\n");
        return sb.toString();
    }

    private String loadClaudeMdPrompt(Path workdir) {
        StringBuilder sb = new StringBuilder();

        String homeDir = System.getProperty("user.home");
        Path userClaude = Paths.get(homeDir, ".claude", "CLAUDE.md");
        if (Files.exists(userClaude)) {
            try {
                String content = Files.readString(userClaude).trim();
                sb.append("## From user global (~").append("/.claude/CLAUDE.md)\n\n");
                sb.append(content).append("\n\n");
            } catch (IOException ignored) {
            }
        }

        Path projectClaude = workdir.resolve("CLAUDE.md");
        if (Files.exists(projectClaude)) {
            try {
                String content = Files.readString(projectClaude).trim();
                sb.append("## From project root (CLAUDE.md)\n\n");
                sb.append(content).append("\n\n");
            } catch (IOException ignored) {
            }
        }

        if (sb.length() == 0) {
            return "";
        }

        return "# CLAUDE.md instructions\n\n" + sb.toString().trim();
    }
}
