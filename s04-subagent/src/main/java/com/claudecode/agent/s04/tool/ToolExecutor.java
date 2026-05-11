package com.claudecode.agent.s04.tool;

import com.claudecode.agent.s04.model.Tool;

import java.util.Map;

public interface ToolExecutor {
    String invoke(Map<String, Object> input) throws Exception;
    String name();
    Tool toolSpec();
}
