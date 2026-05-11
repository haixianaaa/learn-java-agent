package com.claudecode.agent.s15;

import com.claudecode.agent.s15.team.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MessageBus messageBus = new MessageBus();
        TeammateManager teamManager = new TeammateManager(messageBus);

        System.out.println("S15 - Agent Teams");
        System.out.println("Commands: spawn <role> <name>, list, send <from> <to> <msg>, inbox <id>, shutdown <from> <to>, exit()");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("spawn ")) {
                String[] parts = input.substring(6).split(" ", 2);
                if (parts.length >= 2) {
                    Teammate t = teamManager.spawn(parts[0], parts[1]);
                    System.out.println("Spawned: " + t);
                }
            } else if (input.equals("list")) {
                System.out.println(teamManager.renderTeam());
            } else if (input.startsWith("send ")) {
                String[] parts = input.substring(5).split(" ", 3);
                if (parts.length >= 3) {
                    teamManager.sendMessage(parts[0], parts[1], parts[2]);
                    System.out.println("Message sent from " + parts[0] + " to " + parts[1]);
                }
            } else if (input.startsWith("inbox ")) {
                String id = input.substring(6);
                List<InboxMessage> messages = teamManager.readInbox(id);
                if (messages.isEmpty()) {
                    System.out.println("Inbox empty");
                } else {
                    for (InboxMessage m : messages) {
                        System.out.println("  From " + m.getFrom() + ": " + m.getContent());
                    }
                }
            } else if (input.startsWith("shutdown ")) {
                String[] parts = input.substring(9).split(" ", 2);
                if (parts.length >= 2) {
                    teamManager.sendShutdownRequest(parts[0], parts[1]);
                    System.out.println("Shutdown request sent");
                }
            }
        }

        scanner.close();
    }
}
