package com.claudecode.agent.s16.protocol;

import java.util.Map;

@FunctionalInterface
public interface ProtocolHandler {
    ProtocolResponse handle(ProtocolRequest request);
}
