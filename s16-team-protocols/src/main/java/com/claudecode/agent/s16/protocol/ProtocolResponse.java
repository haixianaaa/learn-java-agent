package com.claudecode.agent.s16.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProtocolResponse {
    private boolean approved;
    private String message;
    private Object result;

    public static ProtocolResponse approved(String message) {
        return new ProtocolResponse(true, message, null);
    }

    public static ProtocolResponse approved(String message, Object result) {
        return new ProtocolResponse(true, message, result);
    }

    public static ProtocolResponse denied(String message) {
        return new ProtocolResponse(false, message, null);
    }
}
