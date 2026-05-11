package com.claudecode.agent.s08.hook;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HookControl {
    CONTINUE("continue"),
    BLOCK("block");

    private final String value;
    private String reason;

    public static HookControl block(String reason) {
        HookControl control = BLOCK;
        control.reason = reason;
        return control;
    }
}
