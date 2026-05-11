package com.claudecode.agent.s10;

import com.claudecode.agent.s10.prompt.PromptBuilder;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PromptBuilder builder = new PromptBuilder();
        String workDir = System.getProperty("user.dir");

        System.out.println("System Prompt Demo. Type 'exit()' to quit.");
        System.out.println("Commands: show, build");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("show") || input.equals("build")) {
                String prompt = builder.buildSystemPrompt(workDir);
                System.out.println("\n=== System Prompt ===\n");
                System.out.println(prompt);
                System.out.println("\n=== End ===");
            } else {
                System.out.println("Unknown command. Use: show, build");
            }
        }

        scanner.close();
    }
}
