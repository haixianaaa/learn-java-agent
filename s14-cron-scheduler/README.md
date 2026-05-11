# S14 - Cron Scheduler

本章实现 Cron 调度器，支持定时执行任务。调度器在后台运行，到时间后注入提示。

## 运行方式

```bash
cd s14-cron-scheduler
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s14.Main"
```

## 本章新增能力

- 新增 `CronScheduler` 管理定时任务
- 支持简化的 Cron 表达式
- 后台线程定期检查
- 通知注入机制

## 代码结构

```text
s14-cron-scheduler/
├── src/main/java/com/claudecode/agent/s14/
│   ├── Main.java
│   └── cron/
│       ├── CronTask.java         # 定时任务模型
│       └── CronScheduler.java    # 调度器
└── pom.xml
```

## CronTask 模型

```java
public class CronTask {
    private String id;
    private String name;
    private String cronExpression;
    private String prompt;
    private boolean enabled;
    private Instant lastRun;
    private Instant nextRun;
}
```

## 简化的 Cron 表达式

目前只支持简化的间隔表达式：

```text
*/5 * * * * *  -> 每 5 秒
*/60 * * * * * -> 每 60 秒
```

## 通知注入

```java
List<String> notifications = scheduler.drainNotifications();
for (String n : notifications) {
    context.add(Message.text("user", n));
}
```

## 本章的局限

- Cron 表达式解析简化
- 没有任务持久化
- 没有任务历史

## 下一章

s15 会实现代理团队系统，支持多代理协作。
