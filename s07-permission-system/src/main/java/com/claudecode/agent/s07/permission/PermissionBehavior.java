package com.claudecode.agent.s07.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionBehavior {
    ALLOW("allow"),
    DENY("deny"),
    ASK("ask");

    private final String value;
}
