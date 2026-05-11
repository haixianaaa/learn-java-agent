# S12 - Task System

本章实现任务系统，持久化管理工作项。任务比 todo 更正式，有生命周期和持久化存储。

## 运行方式

```bash
cd s12-task-system
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s12.Main"
```

## 本章新增能力

- 新增 `TaskManager` 管理任务
- 任务持久化到 `.tasks/` 目录
- 支持 `pending`、`in_progress`、`completed` 状态
- 任务摘要功能

## 代码结构

```text
s12-task-system/
├── src/main/java/com/claudecode/agent/s12/
│   ├── Main.java
│   └── task/
│       ├── Task.java             # 任务模型
│       └── TaskManager.java      # 任务管理器
└── pom.xml
```

## Task 模型

```java
public class Task {
    private String id;
    private String content;
    private String priority;
    private String status;
    private String summary;
    private Instant createdAt;
    private Instant updatedAt;
}
```

## 任务 vs Todo

| 特性 | Task | Todo |
|------|------|------|
| 生命周期 | 持久化 | 会话内 |
| 存储 | 文件系统 | 内存 |
| 用途 | 长期工作项 | 短期计划 |
| 状态 | 多种 | 三种 |

## 任务文件格式

保存在 `.tasks/<id>.json`：

```json
{
  "id": "abc12345",
  "content": "Implement feature X",
  "priority": "high",
  "status": "in_progress",
  "summary": null,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

## 本章的局限

- 没有任务依赖关系
- 没有任务分配
- 没有任务截止日期

## 下一章

s13 会实现后台任务系统，支持异步执行长时间运行的任务。
