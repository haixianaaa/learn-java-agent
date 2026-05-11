package com.claudecode.agent.s06.compact;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CompactState {
    @Builder.Default
    private boolean hasCompacted = false;
    private String lastSummary;
    @Builder.Default
    private List<String> recentFiles = new ArrayList<>();
}
