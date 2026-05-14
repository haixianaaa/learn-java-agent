package com.claudecode.agent.s11.recovery;

import lombok.Data;

@Data
public class RecoveryState {
    private int continuationAttempts = 0;
    private int compactAttempts = 0;
    private int transportAttempts = 0;
}
