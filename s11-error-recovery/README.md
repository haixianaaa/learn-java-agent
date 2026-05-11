# S11 - Error Recovery

本章实现错误恢复系统，处理模型输出截断、上下文过长和临时传输错误。

## 运行方式

```bash
cd s11-error-recovery
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s11.Main"
```

## 本章新增能力

- 新增 `RecoveryHandler` 处理错误恢复
- 实现指数退避重试
- 检测临时错误和上下文过长错误
- 跟踪恢复状态

## 代码结构

```text
s11-error-recovery/
├── src/main/java/com/claudecode/agent/s11/
│   ├── Main.java
│   └── recovery/
│       ├── RecoveryState.java    # 恢复状态
│       ├── RecoveryConfig.java   # 配置常量
│       └── RecoveryUtils.java    # 工具方法
└── pom.xml
```

## 恢复策略

| 错误类型 | 恢复策略 |
|----------|----------|
| 临时传输错误 | 指数退避重试 |
| 上下文过长 | 压缩上下文 |
| 输出截断 | 发送 continuation 消息 |

## 指数退避

```java
public static Duration backoffDelay(int attempt) {
    double base = Math.min(
        BACKOFF_BASE_DELAY_SECS * Math.pow(2, attempt),
        BACKOFF_MAX_DELAY_SECS
    );
    double jitter = Math.random();
    return Duration.ofMillis((long) ((base + jitter) * 1000));
}
```

## 错误检测

```java
public static boolean isTransientTransportError(String errorText) {
    return TRANSIENT_ERROR_PATTERNS.stream()
        .anyMatch(errorText.toLowerCase()::contains);
}

public static boolean isPromptTooLongError(String errorText) {
    return errorText.contains("prompt") && errorText.contains("long");
}
```

## 本章的局限

- 没有区分不同类型的截断
- 没有恢复策略的 A/B 测试
- 没有恢复效果的度量

## 下一章

s12 会实现任务系统，持久化管理工作项。
