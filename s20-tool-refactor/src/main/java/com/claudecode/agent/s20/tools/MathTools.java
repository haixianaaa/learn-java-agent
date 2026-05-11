package com.claudecode.agent.s20.tools;

import com.claudecode.agent.s20.annotation.*;

public class MathTools {
    @Tool(name = "add", description = "Add two numbers")
    public int add(
            @Param(name = "a", description = "First number") int a,
            @Param(name = "b", description = "Second number") int b
    ) {
        return a + b;
    }

    @Tool(name = "subtract", description = "Subtract two numbers")
    public int subtract(
            @Param(name = "a", description = "First number") int a,
            @Param(name = "b", description = "Second number") int b
    ) {
        return a - b;
    }

    @Tool(name = "multiply", description = "Multiply two numbers")
    public int multiply(
            @Param(name = "a", description = "First number") int a,
            @Param(name = "b", description = "Second number") int b
    ) {
        return a * b;
    }

    @Tool(name = "divide", description = "Divide two numbers")
    public double divide(
            @Param(name = "a", description = "Numerator") double a,
            @Param(name = "b", description = "Denominator") double b
    ) {
        if (b == 0) throw new IllegalArgumentException("Cannot divide by zero");
        return a / b;
    }

    @Tool(name = "power", description = "Raise a number to a power")
    public double power(
            @Param(name = "base", description = "Base number") double base,
            @Param(name = "exponent", description = "Exponent") double exponent
    ) {
        return Math.pow(base, exponent);
    }
}
