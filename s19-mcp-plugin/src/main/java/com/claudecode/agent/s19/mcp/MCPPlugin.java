package com.claudecode.agent.s19.mcp;

import java.util.List;
import java.util.Map;

public interface MCPPlugin {
    String name();
    String description();
    List<String> tools();
    Object callTool(String toolName, Map<String, Object> params);
    Map<String, Object> toolSpec(String toolName);
}
