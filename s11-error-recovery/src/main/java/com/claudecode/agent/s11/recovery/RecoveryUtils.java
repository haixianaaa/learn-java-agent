package com.claudecode.agent.s11.recovery;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class RecoveryUtils {
    private static final Random random = new Random();

    private static final List<String> TRANSIENT_ERROR_PATTERNS = List.of(
            "timeout", "timed out", "rate limit", "too many requests",
            "unavailable", "connection", "overloaded", "temporarily",
            "econnreset", "broken pipe"
    );

    private static final List<String> PROMPT_TOO_LONG_PATTERNS = List.of(
            "prompt", "long", "overlong_prompt", "too many tokens", "context length"
    );

    public static boolean isPromptTooLongError(String errorText) {
        String lower = errorText.toLowerCase();
        return (lower.contains("prompt") && lower.contains("long"))
                || PROMPT_TOO_LONG_PATTERNS.stream().anyMatch(lower::contains);
    }

    public static boolean isTransientTransportError(String errorText) {
        String lower = errorText.toLowerCase();
        return TRANSIENT_ERROR_PATTERNS.stream().anyMatch(lower::contains);
    }

    public static Duration backoffDelay(int attempt) {
        double base = Math.min(
                RecoveryConfig.BACKOFF_BASE_DELAY_SECS * Math.pow(2, attempt),
                RecoveryConfig.BACKOFF_MAX_DELAY_SECS
        );
        double jitter = random.nextDouble();
        return Duration.ofMillis((long) ((base + jitter) * 1000));
    }

    public static int estimateContextSize(List<?> messages) {
        return messages.stream()
                .mapToInt(m -> m.toString().length())
                .sum();
    }
}
