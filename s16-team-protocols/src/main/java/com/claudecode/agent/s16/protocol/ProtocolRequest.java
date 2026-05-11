package com.claudecode.agent.s16.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ProtocolRequest {
    private String protocol;
    private Map<String, Object> data;
    private String from;
    private String to;

    public static ProtocolRequest create(String protocol, Map<String, Object> data, String from, String to) {
        return new ProtocolRequest(protocol, data, from, to);
    }
}
