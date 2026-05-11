package com.claudecode.agent.s05;

import com.claudecode.agent.s05.skill.SkillRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path skillsDir = Paths.get(System.getProperty("user.dir"), "skills");
        SkillRegistry registry = new SkillRegistry(skillsDir);

        try {
            registry.loadSkills();
        } catch (Exception e) {
            System.err.println("Failed to load skills: " + e.getMessage());
        }

        System.out.println("Skill Registry started. Type 'exit()' to quit.");
        System.out.println("Commands: list, show <name>, reload");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("list")) {
                System.out.println(registry.describeAvailable());
            } else if (input.startsWith("show ")) {
                String name = input.substring(5);
                System.out.println(registry.loadFullText(name));
            } else if (input.equals("reload")) {
                try {
                    registry.loadSkills();
                    System.out.println("Skills reloaded.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Unknown command. Use: list, show <name>, reload");
            }
        }

        scanner.close();
    }
}
