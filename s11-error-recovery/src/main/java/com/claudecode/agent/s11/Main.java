package com.claudecode.agent.s11;

public class Main {
    public static void main(String[] args) {
        System.out.println("S11 - Error Recovery System");
        System.out.println("This module demonstrates error recovery with retry and backoff.");
        
        RecoveryConfig config = new RecoveryConfig(3, 1000, 2.0);
        RecoveryHandler handler = new RecoveryHandler(config);
        
        handler.executeWithRetry(() -> {
            System.out.println("Executing task...");
            return "Success";
        });
    }
}

record RecoveryConfig(int maxAttempts, long initialDelayMs, double backoffMultiplier) {}

class RecoveryHandler {
    private final RecoveryConfig config;
    
    public RecoveryHandler(RecoveryConfig config) {
        this.config = config;
    }
    
    public <T> T executeWithRetry(java.util.function.Supplier<T> task) {
        int attempts = 0;
        long delay = config.initialDelayMs();
        
        while (attempts < config.maxAttempts()) {
            try {
                attempts++;
                T result = task.get();
                System.out.println("Task succeeded on attempt " + attempts);
                return result;
            } catch (Exception e) {
                System.out.println("Attempt " + attempts + " failed: " + e.getMessage());
                if (attempts >= config.maxAttempts()) {
                    throw new RuntimeException("Max retries exceeded", e);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", ie);
                }
                delay = (long) (delay * config.backoffMultiplier());
            }
        }
        throw new RuntimeException("Should not reach here");
    }
}
