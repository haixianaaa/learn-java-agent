package com.claudecode.agent.s17;

import com.claudecode.agent.s17.autonomous.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AutonomousAgentManager manager = new AutonomousAgentManager();

        System.out.println("S17 - Autonomous Agents");
        System.out.println("Commands: agent <name> <role>, task <subject> <desc>, assign <taskId> <agentId>, start <id>, stop <id>, list, tasks, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                manager.shutdown();
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("agent ")) {
                String[] parts = input.substring(6).split(" ", 2);
                if (parts.length >= 2) {
                    AutonomousAgent agent = manager.createAgent(parts[0], parts[1], "You are an autonomous agent.");
                    System.out.println("Created agent: " + agent.getId() + " - " + agent.getName());
                }
            } else if (input.startsWith("task ")) {
                String[] parts = input.substring(5).split(" ", 2);
                if (parts.length >= 2) {
                    AgentTask task = manager.createTask(parts[0], parts[1]);
                    System.out.println("Created task: " + task.getId() + " - " + task.getSubject());
                }
            } else if (input.startsWith("assign ")) {
                String[] parts = input.substring(7).split(" ", 2);
                if (parts.length == 2) {
                    manager.assignTask(parts[0], parts[1]);
                    System.out.println("Assigned task " + parts[0] + " to agent " + parts[1]);
                }
            } else if (input.startsWith("start ")) {
                String id = input.substring(6);
                manager.startAgent(id);
            } else if (input.startsWith("stop ")) {
                String id = input.substring(5);
                manager.stopAgent(id);
            } else if (input.equals("list")) {
                for (AutonomousAgent agent : manager.listAgents()) {
                    System.out.println("  [" + agent.getStatus() + "] " + agent.getId() + ": " + agent.getName() + " (" + agent.getRole() + ")");
                }
            } else if (input.equals("tasks")) {
                for (AgentTask task : manager.listTasks()) {
                    System.out.println("  [" + task.getStatus() + "] " + task.getId() + ": " + task.getSubject() + 
                            (task.getAssignee() != null ? " -> " + task.getAssignee() : ""));
                }
            }
        }

        scanner.close();
    }
}
