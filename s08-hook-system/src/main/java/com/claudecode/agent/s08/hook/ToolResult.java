package com.claudecode.agent.s08.hook;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ToolResult {
    private String toolUseId;
    private String content;
}
