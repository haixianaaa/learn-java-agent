package com.claudecode.agent.s08.hook;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ToolUse {
    private String id;
    private String name;
    private Map<String, Object> input;
}
