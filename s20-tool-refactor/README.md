# S20 - Tool Refactor

本章实现工具重构系统，使用注解简化工具定义。通过反射自动生成工具规格。

## 运行方式

```bash
cd s20-tool-refactor
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s20.Main"
```

## 本章新增能力

- 新增 `@Tool` 和 `@Param` 注解
- 新增 `ToolRegistry` 自动注册工具
- 通过反射生成工具规格
- 自动类型转换

## 代码结构

```text
s20-tool-refactor/
├── src/main/java/com/claudecode/agent/s20/
│   ├── Main.java
│   ├── annotation/
│   │   ├── Tool.java             # 工具注解
│   │   └── Param.java            # 参数注解
│   ├── registry/
│   │   ├── ToolDefinition.java   # 工具定义
│   │   └── ToolRegistry.java     # 工具注册表
│   └── tools/
│       ├── MathTools.java        # 数学工具
│       └── StringTools.java      # 字符串工具
└── pom.xml
```

## 注解定义

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tool {
    String name();
    String description();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String name();
    String description() default "";
}
```

## 工具示例

```java
public class MathTools {
    @Tool(name = "add", description = "Add two numbers")
    public int add(
        @Param(name = "a", description = "First number") int a,
        @Param(name = "b", description = "Second number") int b
    ) {
        return a + b;
    }
}
```

## 自动注册

```java
ToolRegistry registry = new ToolRegistry();
registry.register(new MathTools());
registry.register(new StringTools());
```

## 类型推断

自动推断参数类型：
- `String` -> `string`
- `int/long` -> `integer`
- `double/float` -> `number`
- `boolean` -> `boolean`
- `List` -> `array`

## 本章总结

s20 完成了工具系统的重构，使工具定义更加简洁和类型安全。

## 完整实现

`sfull` 模块整合了所有 s01-s20 的功能，提供了一个完整的 Agent 实现。
