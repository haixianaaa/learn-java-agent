package com.claudecode.agent.s16;

import com.claudecode.agent.s16.protocol.*;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ProtocolRegistry registry = new ProtocolRegistry();

        registry.register("plan_approval", request -> {
            System.out.println("[Protocol] Plan approval request from " + request.getFrom());
            return ProtocolResponse.approved("Plan approved", Map.of("approvedAt", System.currentTimeMillis()));
        });

        registry.register("task_delegation", request -> {
            System.out.println("[Protocol] Task delegation: " + request.getData());
            return ProtocolResponse.approved("Task accepted");
        });

        registry.register("shutdown_request", request -> {
            System.out.println("[Protocol] Shutdown request from " + request.getFrom());
            return ProtocolResponse.approved("Shutdown acknowledged");
        });

        registry.register("status_query", request -> {
            return ProtocolResponse.approved("Status: running", Map.of("status", "running", "tasks", 3));
        });

        System.out.println("S16 - Team Protocols");
        System.out.println("Available protocols: " + registry.listProtocols());
        System.out.println("Commands: exec <protocol> <from> <to> <json>, list, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("list")) {
                System.out.println("Registered protocols: " + registry.listProtocols());
            } else if (input.startsWith("exec ")) {
                String[] parts = input.substring(5).split(" ", 4);
                if (parts.length >= 3) {
                    String protocol = parts[0];
                    String from = parts[1];
                    String to = parts[2];
                    Map<String, Object> data = parts.length > 3 ? Map.of("payload", parts[3]) : Map.of();

                    ProtocolResponse response = registry.execute(protocol, data, from, to);
                    System.out.println("Response: " + (response.isApproved() ? "APPROVED" : "DENIED"));
                    System.out.println("Message: " + response.getMessage());
                    if (response.getResult() != null) {
                        System.out.println("Result: " + response.getResult());
                    }
                }
            }
        }

        scanner.close();
    }
}
