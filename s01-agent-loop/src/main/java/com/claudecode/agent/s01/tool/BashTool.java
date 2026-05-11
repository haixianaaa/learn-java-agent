package com.claudecode.agent.s01.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BashTool {
    private static final List<String> DANGEROUS_COMMANDS = Arrays.asList(
            "rm -rf /", "sudo", "shutdown", "reboot", "> /dev/"
    );
    private static final int TIMEOUT_SECONDS = 120;
    private static final int MAX_OUTPUT_LENGTH = 50000;

    public String execute(String command) {
        for (String dangerous : DANGEROUS_COMMANDS) {
            if (command.contains(dangerous)) {
                return "Error: Dangerous command blocked";
            }
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd", "/c", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: Timeout (" + TIMEOUT_SECONDS + "s)";
            }

            String result = output.toString().trim();
            if (result.isEmpty()) {
                return "(no output)";
            }

            if (result.length() > MAX_OUTPUT_LENGTH) {
                return result.substring(0, MAX_OUTPUT_LENGTH);
            }
            return result;

        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
