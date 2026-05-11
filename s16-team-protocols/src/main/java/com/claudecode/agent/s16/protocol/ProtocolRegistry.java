package com.claudecode.agent.s16.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolRegistry {
    private final Map<String, ProtocolHandler> protocols = new ConcurrentHashMap<>();

    public void register(String name, ProtocolHandler handler) {
        protocols.put(name, handler);
    }

    public ProtocolResponse execute(String protocol, Map<String, Object> data, String from, String to) {
        ProtocolHandler handler = protocols.get(protocol);
        if (handler == null) {
            return ProtocolResponse.denied("Unknown protocol: " + protocol);
        }
        
        ProtocolRequest request = ProtocolRequest.create(protocol, data, from, to);
        return handler.handle(request);
    }

    public boolean hasProtocol(String name) {
        return protocols.containsKey(name);
    }

    public java.util.Set<String> listProtocols() {
        return protocols.keySet();
    }
}
