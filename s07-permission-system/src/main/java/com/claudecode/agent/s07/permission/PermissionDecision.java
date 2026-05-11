package com.claudecode.agent.s07.permission;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionDecision {
    private PermissionBehavior behavior;
    private String reason;
}
