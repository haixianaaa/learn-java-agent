package com.claudecode.agent.s03.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanItemStatus {
    PENDING("pending", "[ ]"),
    IN_PROGRESS("in_progress", "[>]"),
    COMPLETED("completed", "[x]");

    @JsonValue
    private final String value;
    private final String marker;

    public static PlanItemStatus fromString(String value) {
        for (PlanItemStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
