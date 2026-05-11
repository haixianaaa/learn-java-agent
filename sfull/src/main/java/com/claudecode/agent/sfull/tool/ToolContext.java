package com.claudecode.agent.sfull.tool;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
public class ToolContext {
    private Path workDir;
    private ConcurrentHashMap<String, Object> taskManager;
    private ConcurrentHashMap<String, Object> backgroundManager;
    private ConcurrentHashMap<String, Object> cronScheduler;
    private ConcurrentHashMap<String, Object> teammateManager;
    private ConcurrentHashMap<String, Object> worktreeManager;
    private ConcurrentHashMap<String, Object> memoryManager;
    private ConcurrentHashMap<String, Object> skillRegistry;
}
