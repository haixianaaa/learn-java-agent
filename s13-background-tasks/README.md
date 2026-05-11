# S13 - Background Tasks

本章实现后台任务系统，支持异步执行长时间运行的任务，并在主循环中注入完成通知。

## 运行方式

```bash
cd s13-background-tasks
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s13.Main"
```

## 本章新增能力

- 新增 `BackgroundManager` 管理后台任务
- 支持异步执行 shell 命令
- 任务状态跟踪
- 结果通知队列

## 代码结构

```text
s13-background-tasks/
├── src/main/java/com/claudecode/agent/s13/
│   ├── Main.java
│   └── background/
│       ├── BackgroundTask.java   # 后台任务模型
│       └── BackgroundManager.java # 后台任务管理器
└── pom.xml
```

## BackgroundTask 模型

```java
public class BackgroundTask {
    private String id;
    private String command;
    private String status;
    private String output;
    private Instant startTime;
    private Instant endTime;
}
```

## 使用流程

```text
start(command) -> 返回任务 ID
  -> 异步执行命令
  -> 完成后加入通知队列
drainResultsMessage() -> 获取完成通知
```

## 通知注入

在 agent 主循环中：

```java
String notifications = backgroundManager.drainResultsMessage();
if (notifications != null) {
    context.add(Message.text("user", notifications));
}
```

## 本章的局限

- 没有任务取消
- 没有任务优先级
- 没有资源限制

## 下一章

s14 会实现 Cron 调度器，支持定时执行任务。
