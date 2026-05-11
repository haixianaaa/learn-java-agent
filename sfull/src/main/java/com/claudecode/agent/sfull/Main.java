package com.claudecode.agent.sfull;

import com.claudecode.agent.sfull.agent.Agent;
import com.claudecode.agent.sfull.client.LLMClient;
import com.claudecode.agent.sfull.model.ContentBlock;
import com.claudecode.agent.sfull.model.Message;
import com.claudecode.agent.sfull.tool.ToolContext;
import com.claudecode.agent.sfull.tool.ToolRouter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
        LLMClient client = new LLMClient();
        ToolRouter tools = new ToolRouter();
        Path workDir = Paths.get(System.getProperty("user.dir"));

        ToolContext toolContext = ToolContext.builder()
                .workDir(workDir)
                .taskManager(new ConcurrentHashMap<>())
                .backgroundManager(new ConcurrentHashMap<>())
                .cronScheduler(new ConcurrentHashMap<>())
                .teammateManager(new ConcurrentHashMap<>())
                .worktreeManager(new ConcurrentHashMap<>())
                .memoryManager(new ConcurrentHashMap<>())
                .skillRegistry(new ConcurrentHashMap<>())
                .build();

        Agent agent = Agent.builder()
                .client(client)
                .toolContext(toolContext)
                .tools(tools)
                .context(new ArrayList<>())
                .build();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Java Agent started. Type 'exit()' to quit.");

        while (true) {
            System.out.print("\n--- How can I help you? ");
            String query = scanner.nextLine().trim();

            if (query.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            agent.getContext().add(Message.text("user", query));

            try {
                agent.agentLoop();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

            if (!agent.getContext().isEmpty()) {
                Message lastMessage = agent.getContext().get(agent.getContext().size() - 1);
                System.out.println("--- Final response:\n" + Agent.extractText(lastMessage));
            }
        }

        scanner.close();
    }
}
