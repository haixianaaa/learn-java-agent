package com.claudecode.agent.s20.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ToolDefinition {
    private String name;
    private String description;
    private Method method;
    private Object instance;
    private List<String> parameterNames;
    private Map<String, Object> inputSchema;

    public Map<String, Object> toToolSpec() {
        return Map.of(
                "name", name,
                "description", description,
                "input_schema", inputSchema
        );
    }
}
