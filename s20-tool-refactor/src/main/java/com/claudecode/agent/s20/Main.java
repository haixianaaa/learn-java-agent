package com.claudecode.agent.s20;

import java.lang.annotation.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("S20 - Tool Refactor with Annotations");
        
        ToolRegistry registry = new ToolRegistry();
        registry.register(new MathTools());
        
        System.out.println("Registered tools:");
        registry.listTools().forEach(t -> System.out.println("  " + t.name() + ": " + t.description()));
        
        Object result = registry.execute("add", Map.of("a", 5, "b", 3));
        System.out.println("\nResult of add(5, 3): " + result);
        
        result = registry.execute("multiply", Map.of("a", 4, "b", 7));
        System.out.println("Result of multiply(4, 7): " + result);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Tool {
    String name();
    String description();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Param {
    String name();
    String description() default "";
}

record ToolInfo(String name, String description, java.lang.reflect.Method method, Object instance) {}

class ToolRegistry {
    private final Map<String, ToolInfo> tools = new HashMap<>();
    
    public void register(Object toolInstance) {
        for (java.lang.reflect.Method method : toolInstance.getClass().getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation != null) {
                ToolInfo info = new ToolInfo(toolAnnotation.name(), toolAnnotation.description(), method, toolInstance);
                tools.put(toolAnnotation.name(), info);
            }
        }
    }
    
    public List<ToolInfo> listTools() {
        return new ArrayList<>(tools.values());
    }
    
    public Object execute(String name, Map<String, Object> params) throws Exception {
        ToolInfo tool = tools.get(name);
        if (tool == null) {
            return "Unknown tool: " + name;
        }
        
        java.lang.reflect.Method method = tool.method();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        String[] paramNames = params.keySet().toArray(new String[0]);
        for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
            args[i] = params.get(paramNames[i]);
        }
        
        return method.invoke(tool.instance(), args);
    }
}

class MathTools {
    @Tool(name = "add", description = "Add two numbers")
    public int add(int a, int b) {
        return a + b;
    }
    
    @Tool(name = "subtract", description = "Subtract two numbers")
    public int subtract(int a, int b) {
        return a - b;
    }
    
    @Tool(name = "multiply", description = "Multiply two numbers")
    public int multiply(int a, int b) {
        return a * b;
    }
    
    @Tool(name = "divide", description = "Divide two numbers")
    public double divide(double a, double b) {
        return a / b;
    }
}
