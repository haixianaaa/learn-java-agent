package com.claudecode.agent.s12.task;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    PENDING("pending", "[ ]"),
    IN_PROGRESS("in_progress", "[>]"),
    COMPLETED("completed", "[x]"),
    DELETED("deleted", "[-]");

    @JsonValue
    private final String value;
    private final String marker;

    public static TaskStatus fromString(String value) {
        for (TaskStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
