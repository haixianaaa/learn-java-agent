package com.claudecode.agent.s08.hook;

import java.util.function.Function;

@FunctionalInterface
public interface PreToolUseHook extends Function<ToolUse, HookControl> {
}
