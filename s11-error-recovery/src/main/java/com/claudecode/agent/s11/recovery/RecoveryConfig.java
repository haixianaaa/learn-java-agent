package com.claudecode.agent.s11.recovery;

public class RecoveryConfig {
    public static final int MAX_RECOVERY_ATTEMPTS = 3;
    public static final double BACKOFF_BASE_DELAY_SECS = 1.0;
    public static final double BACKOFF_MAX_DELAY_SECS = 30.0;
    public static final int CONTEXT_THRESHOLD_CHARS = 50000;
    public static final String CONTINUATION_MESSAGE = 
            "Output limit hit. Continue directly from where you stopped. " +
            "No recap, no repetition. Pick up mid-sentence if needed.";

    private RecoveryConfig() {}
}
