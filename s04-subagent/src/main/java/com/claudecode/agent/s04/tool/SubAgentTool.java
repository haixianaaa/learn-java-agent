package com.claudecode.agent.s04.tool;

import com.claudecode.agent.s04.agent.SubAgent;
import com.claudecode.agent.s04.client.LLMClient;
import com.claudecode.agent.s04.model.Tool;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SubAgentTool implements ToolExecutor {
    private final LLMClient client;

    public SubAgentTool(LLMClient client) {
        this.client = client;
    }

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String prompt = (String) input.get("prompt");
        if (prompt == null) {
            throw new IllegalArgumentException("Invalid prompt");
        }

        String description = (String) input.get("description");
        System.out.println("> task - (" + (description != null ? description : "") + "): " + prompt);

        ToolRegistry subagentTools = new ToolRegistry();
        subagentTools.register(new BashTool());
        subagentTools.register(new ReadFileTool());

        String workDir = Paths.get(System.getProperty("user.dir")).toString();
        String systemPrompt = String.format(
                "You are a coding subagent at %s. Complete the given task, then summarize your findings.",
                workDir
        );

        SubAgent subAgent = new SubAgent(client, subagentTools, systemPrompt);
        return subAgent.run(prompt);
    }

    @Override
    public String name() {
        return "task";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("task")
                .description("Spawn a subagent with fresh context. It shares the filesystem but not conversation history.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "prompt", Map.of("type", "string"),
                                "description", Map.of("type", "string", "description", "Short description of the task")
                        ),
                        "required", List.of("prompt")
                ))
                .build();
    }
}
