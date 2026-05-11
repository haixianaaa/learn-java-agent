package com.claudecode.agent.sfull.tool;

import com.claudecode.agent.sfull.model.Tool;

import java.util.Map;

public interface ToolExecutor {
    String invoke(ToolContext context, Map<String, Object> input) throws Exception;
    String name();
    Tool toolSpec();
}
