# S18 - Worktree Task Isolation

本章实现工作树隔离系统，为任务创建独立的工作目录。每个任务有自己的文件系统视图。

## 运行方式

```bash
cd s18-worktree-task-isolation
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s18.Main"
```

## 本章新增能力

- 新增 `WorktreeManager` 管理工作树
- 支持创建/删除工作树
- 支持在工作树中执行命令
- 集成 Git worktree

## 代码结构

```text
s18-worktree-task-isolation/
├── src/main/java/com/claudecode/agent/s18/
│   ├── Main.java
│   └── worktree/
│       ├── Worktree.java         # 工作树模型
│       └── WorktreeManager.java  # 工作树管理器
└── pom.xml
```

## Worktree 模型

```java
public class Worktree {
    private String id;
    private String name;
    private Path path;
    private String branch;
    private String status;
}
```

## 使用场景

- 并行开发多个特性
- 隔离实验性修改
- 代码审查工作区

## Git Worktree 集成

```bash
git worktree list
git worktree add .worktrees/task-123 feature-branch
git worktree remove .worktrees/task-123
```

## 本章的局限

- 没有自动分支管理
- 没有工作树同步
- 没有冲突检测

## 下一章

s19 会实现 MCP 插件系统，支持插件化扩展工具。
