package com.claudecode.agent.s08.hook;

import lombok.Getter;

@Getter
public enum HookControl {
    CONTINUE("continue"),
    BLOCK("block");

    private final String value;
    private String reason;

    HookControl(String value) {
        this.value = value;
    }

    public static HookControl block(String reason) {
        HookControl control = BLOCK;
        control.reason = reason;
        return control;
    }
}
