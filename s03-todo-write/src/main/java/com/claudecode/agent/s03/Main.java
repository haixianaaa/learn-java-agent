package com.claudecode.agent.s03;

import com.claudecode.agent.s03.tool.TodoManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TodoManager todoManager = new TodoManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Todo Manager started. Type 'exit()' to quit.");
        System.out.println("Commands: add <content>, complete <index>, show, clear");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.startsWith("add ")) {
                String content = input.substring(4);
                System.out.println(todoManager.invoke(java.util.Map.of(
                        "items", java.util.List.of(java.util.Map.of(
                                "content", content,
                                "status", "pending"
                        ))
                )));
            } else if (input.startsWith("complete ")) {
                try {
                    int index = Integer.parseInt(input.substring(9)) - 1;
                    var items = todoManager.getItems();
                    if (index >= 0 && index < items.size()) {
                        var item = items.get(index);
                        System.out.println(todoManager.invoke(java.util.Map.of(
                                "items", java.util.List.of(java.util.Map.of(
                                        "content", item.getContent(),
                                        "status", "completed"
                                ))
                        )));
                    } else {
                        System.out.println("Invalid index");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid index");
                }
            } else if (input.equals("show")) {
                System.out.println(todoManager.render());
            } else if (input.equals("clear")) {
                todoManager.update(java.util.List.of());
                System.out.println("Cleared all items");
            } else {
                System.out.println("Unknown command. Use: add <content>, complete <index>, show, clear");
            }
        }

        scanner.close();
    }
}
