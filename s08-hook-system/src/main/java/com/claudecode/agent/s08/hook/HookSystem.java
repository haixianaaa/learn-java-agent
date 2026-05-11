package com.claudecode.agent.s08.hook;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HookSystem {
    private final List<PreToolUseHook> preToolHooks = new ArrayList<>();
    private final List<PostToolUseHook> postToolHooks = new ArrayList<>();
    private final List<SessionStartHook> sessionStartHooks = new ArrayList<>();

    public void registerPreToolHook(PreToolUseHook hook) {
        preToolHooks.add(hook);
    }

    public void registerPostToolUseHook(PostToolUseHook hook) {
        postToolHooks.add(hook);
    }

    public void registerSessionStartHook(SessionStartHook hook) {
        sessionStartHooks.add(hook);
    }

    public HookControl runPreToolHooks(ToolUse toolUse) {
        for (PreToolUseHook hook : preToolHooks) {
            HookControl result = hook.apply(toolUse);
            if (result == HookControl.BLOCK) {
                return result;
            }
        }
        return HookControl.CONTINUE;
    }

    public HookControl runPostToolHooks(ToolResult toolResult) {
        for (PostToolUseHook hook : postToolHooks) {
            HookControl result = hook.apply(toolResult);
            if (result == HookControl.BLOCK) {
                return result;
            }
        }
        return HookControl.CONTINUE;
    }

    public void runSessionStartHooks() {
        for (SessionStartHook hook : sessionStartHooks) {
            hook.apply(null);
        }
    }
}
