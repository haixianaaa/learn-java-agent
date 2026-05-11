package com.claudecode.agent.s20.registry;

import com.claudecode.agent.s20.annotation.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ToolRegistry {
    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    public void register(Object toolInstance) {
        Class<?> clazz = toolInstance.getClass();
        
        for (Method method : clazz.getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation == null) continue;

            String toolName = toolAnnotation.name();
            String description = toolAnnotation.description();

            List<String> paramNames = new ArrayList<>();
            Map<String, Object> properties = new LinkedHashMap<>();

            Parameter[] params = method.getParameters();
            for (Parameter param : params) {
                Param paramAnnotation = param.getAnnotation(Param.class);
                String paramName = paramAnnotation != null ? paramAnnotation.name() : param.getName();
                String paramDesc = paramAnnotation != null ? paramAnnotation.description() : "";

                paramNames.add(paramName);
                properties.put(paramName, Map.of(
                        "type", guessType(param.getType()),
                        "description", paramDesc
                ));
            }

            Map<String, Object> inputSchema = new LinkedHashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("properties", properties);
            if (!paramNames.isEmpty()) {
                inputSchema.put("required", paramNames);
            }

            ToolDefinition definition = new ToolDefinition(
                    toolName, description, method, toolInstance, paramNames, inputSchema
            );

            tools.put(toolName, definition);
        }
    }

    private String guessType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class) return "integer";
        if (type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class) return "number";
        if (type == float.class || type == Float.class) return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type.isArray() || List.class.isAssignableFrom(type)) return "array";
        return "object";
    }

    public ToolDefinition get(String name) {
        return tools.get(name);
    }

    public List<ToolDefinition> list() {
        return new ArrayList<>(tools.values());
    }

    public List<Map<String, Object>> listToolSpecs() {
        return tools.values().stream()
                .map(ToolDefinition::toToolSpec)
                .toList();
    }

    public Object execute(String name, Map<String, Object> params) throws Exception {
        ToolDefinition tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }

        Method method = tool.getMethod();
        Object[] args = new Object[tool.getParameterNames().size()];

        List<String> paramNames = tool.getParameterNames();
        Class<?>[] paramTypes = method.getParameterTypes();

        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            Object value = params.get(paramName);
            
            if (value != null) {
                args[i] = convertValue(value, paramTypes[i]);
            } else {
                args[i] = getDefaultValue(paramTypes[i]);
            }
        }

        return method.invoke(tool.getInstance(), args);
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        return value;
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        return null;
    }

    public String renderTools() {
        if (tools.isEmpty()) {
            return "No tools registered.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Registered tools:\n");
        for (ToolDefinition tool : tools.values()) {
            sb.append(String.format("  - %s: %s%n", tool.getName(), tool.getDescription()));
            sb.append(String.format("    Parameters: %s%n", tool.getParameterNames()));
        }
        return sb.toString();
    }
}
