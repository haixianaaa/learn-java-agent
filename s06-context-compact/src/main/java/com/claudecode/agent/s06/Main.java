package com.claudecode.agent.s06;

import com.claudecode.agent.s06.compact.CompactUtils;
import com.claudecode.agent.s06.model.ContentBlock;
import com.claudecode.agent.s06.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        List<Message> messages = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Context Compact Demo. Type 'exit()' to quit.");
        System.out.println("Commands: add <text>, compact, size, show");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("add ")) {
                String text = input.substring(4);
                messages.add(Message.text("user", text));
                System.out.println("Added message. Total: " + messages.size());
            } else if (input.equals("compact")) {
                CompactUtils.microCompact(messages);
                System.out.println("Micro compact applied.");
            } else if (input.equals("size")) {
                int size = CompactUtils.estimateContextSize(messages);
                System.out.println("Estimated context size: " + size + " characters");
            } else if (input.equals("show")) {
                for (int i = 0; i < messages.size(); i++) {
                    System.out.println(i + ": " + messages.get(i).getRole() + " - " + 
                            truncate(messages.get(i).getContent().toString(), 50));
                }
            } else {
                System.out.println("Unknown command. Use: add <text>, compact, size, show");
            }
        }

        scanner.close();
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}
