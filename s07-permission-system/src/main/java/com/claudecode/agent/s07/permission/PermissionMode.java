package com.claudecode.agent.s07.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionMode {
    DEFAULT("default - ask by default"),
    PLAN("plan - read only"),
    AUTO("auto - allow reads, ask for writes");

    private final String description;

    @Override
    public String toString() {
        return description;
    }
}
