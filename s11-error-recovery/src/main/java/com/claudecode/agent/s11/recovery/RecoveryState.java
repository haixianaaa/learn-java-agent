package com.claudecode.agent.s11.recovery;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecoveryState {
    @Builder.Default
    private int continuationAttempts = 0;
    @Builder.Default
    private int compactAttempts = 0;
    @Builder.Default
    private int transportAttempts = 0;
}
