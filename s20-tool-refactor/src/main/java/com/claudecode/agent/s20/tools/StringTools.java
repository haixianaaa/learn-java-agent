package com.claudecode.agent.s20.tools;

import com.claudecode.agent.s20.annotation.*;

public class StringTools {
    @Tool(name = "concat", description = "Concatenate two strings")
    public String concat(
            @Param(name = "a", description = "First string") String a,
            @Param(name = "b", description = "Second string") String b
    ) {
        return a + b;
    }

    @Tool(name = "length", description = "Get string length")
    public int length(
            @Param(name = "s", description = "Input string") String s
    ) {
        return s != null ? s.length() : 0;
    }

    @Tool(name = "uppercase", description = "Convert string to uppercase")
    public String uppercase(
            @Param(name = "s", description = "Input string") String s
    ) {
        return s != null ? s.toUpperCase() : null;
    }

    @Tool(name = "lowercase", description = "Convert string to lowercase")
    public String lowercase(
            @Param(name = "s", description = "Input string") String s
    ) {
        return s != null ? s.toLowerCase() : null;
    }

    @Tool(name = "reverse", description = "Reverse a string")
    public String reverse(
            @Param(name = "s", description = "Input string") String s
    ) {
        if (s == null) return null;
        return new StringBuilder(s).reverse().toString();
    }
}
