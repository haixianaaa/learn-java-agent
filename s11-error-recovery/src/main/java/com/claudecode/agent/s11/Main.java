package com.claudecode.agent.s11;

import com.claudecode.agent.s11.recovery.*;

import java.time.Duration;
import java.util.Scanner;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        System.out.println("S11 - Error Recovery System Demo");
        System.out.println("This demonstrates retry with exponential backoff.\n");

        RecoveryHandler handler = new RecoveryHandler();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Commands: test, exit()");
        
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit()")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equals("test")) {
                RecoveryState state = RecoveryState.builder().build();
                
                Supplier<String> task = () -> {
                    if (Math.random() < 0.7) {
                        throw new RuntimeException("Simulated transient error");
                    }
                    return "Task completed successfully";
                };

                try {
                    String result = handler.executeWithRecovery(task, state);
                    System.out.println("Result: " + result);
                    System.out.println("Recovery state: " + state);
                } catch (Exception e) {
                    System.out.println("Failed after all retries: " + e.getMessage());
                }
            }
        }

        scanner.close();
    }
}

class RecoveryHandler {
    public <T> T executeWithRecovery(Supplier<T> task, RecoveryState state) {
        while (state.getTransportAttempts() < RecoveryConfig.MAX_RECOVERY_ATTEMPTS) {
            try {
                T result = task.get();
                state.setTransportAttempts(0);
                return result;
            } catch (Exception e) {
                String errorText = e.getMessage();
                
                if (RecoveryUtils.isTransientTransportError(errorText)) {
                    state.setTransportAttempts(state.getTransportAttempts() + 1);
                    System.out.printf("[Recovery] backoff (%d/%d): transient failure%n",
                            state.getTransportAttempts(), RecoveryConfig.MAX_RECOVERY_ATTEMPTS);
                    
                    Duration delay = RecoveryUtils.backoffDelay(state.getTransportAttempts());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted", ie);
                    }
                } else {
                    throw new RuntimeException("Non-recoverable error: " + errorText, e);
                }
            }
        }
        throw new RuntimeException("Max recovery attempts exceeded");
    }
}
