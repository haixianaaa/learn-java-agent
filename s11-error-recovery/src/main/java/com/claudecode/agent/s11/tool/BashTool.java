package com.claudecode.agent.s11.tool;

import com.claudecode.agent.s11.model.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BashTool implements ToolExecutor {
    private static final List<String> DANGEROUS_COMMANDS = Arrays.asList(
            "rm -rf /", "sudo", "shutdown", "reboot", "> /dev/"
    );
    private static final int TIMEOUT_SECONDS = 120;
    private static final int MAX_OUTPUT_LENGTH = 50000;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    @Override
    public String invoke(Map<String, Object> input) throws Exception {
        String command = (String) input.get("command");
        if (command == null) {
            throw new IllegalArgumentException("Invalid command");
        }

        for (String dangerous : DANGEROUS_COMMANDS) {
            if (command.contains(dangerous)) {
                throw new RuntimeException("Error: Dangerous command blocked");
            }
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            Charset charset;
            
            if (IS_WINDOWS) {
                processBuilder.command("cmd", "/c", command);
                charset = Charset.forName("GBK");
            } else {
                processBuilder.command("sh", "-c", command);
                charset = StandardCharsets.UTF_8;
            }
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Error: Timeout (" + TIMEOUT_SECONDS + "s)");
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
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public String name() {
        return "bash";
    }

    @Override
    public Tool toolSpec() {
        return Tool.builder()
                .name("bash")
                .description("Run a shell command in the current workspace.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "command", Map.of("type", "string")
                        ),
                        "required", List.of("command")
                ))
                .build();
    }
}
