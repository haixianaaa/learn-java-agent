package com.claudecode.agent.s16;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("S16 - Team Protocols");
        
        ProtocolManager protocolManager = new ProtocolManager();
        
        protocolManager.registerProtocol("plan_approval", (request) -> {
            System.out.println("Plan approval request: " + request);
            return new ProtocolResponse(true, "Plan approved");
        });
        
        protocolManager.registerProtocol("shutdown_request", (request) -> {
            System.out.println("Shutdown request: " + request);
            return new ProtocolResponse(true, "Shutdown acknowledged");
        });
        
        ProtocolResponse response = protocolManager.sendRequest("plan_approval", 
                Map.of("plan", "Implement feature X"));
        System.out.println("Response: " + response);
    }
}

record ProtocolRequest(String protocol, Map<String, Object> data) {}
record ProtocolResponse(boolean approved, String message) {}

@FunctionalInterface
interface ProtocolHandler {
    ProtocolResponse handle(ProtocolRequest request);
}

class ProtocolManager {
    private final Map<String, ProtocolHandler> protocols = new ConcurrentHashMap<>();
    
    public void registerProtocol(String name, ProtocolHandler handler) {
        protocols.put(name, handler);
    }
    
    public ProtocolResponse sendRequest(String protocol, Map<String, Object> data) {
        ProtocolHandler handler = protocols.get(protocol);
        if (handler == null) {
            return new ProtocolResponse(false, "Unknown protocol: " + protocol);
        }
        return handler.handle(new ProtocolRequest(protocol, data));
    }
}
