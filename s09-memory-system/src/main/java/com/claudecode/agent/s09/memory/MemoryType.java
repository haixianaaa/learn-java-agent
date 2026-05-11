package com.claudecode.agent.s09.memory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryType {
    USER("user", "User preferences"),
    FEEDBACK("feedback", "User corrections"),
    PROJECT("project", "Project facts"),
    REFERENCE("reference", "External resources");

    private final String value;
    private final String description;
}
