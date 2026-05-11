# S09 - Memory System

本章实现记忆系统，持久化存储用户偏好、项目事实和外部资源位置。记忆在会话间保持。

## 运行方式

```bash
cd s09-memory-system
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s09.Main"
```

## 本章新增能力

- 新增 `MemoryManager` 管理记忆
- 支持四种记忆类型：`USER`、`FEEDBACK`、`PROJECT`、`REFERENCE`
- 记忆保存到 `.claude/memory/` 目录
- 使用 YAML frontmatter 格式

## 代码结构

```text
s09-memory-system/
├── src/main/java/com/claudecode/agent/s09/
│   ├── Main.java
│   └── memory/
│       ├── MemoryType.java       # 记忆类型
│       ├── MemoryEntry.java      # 记忆条目
│       └── MemoryManager.java    # 记忆管理器
└── pom.xml
```

## 记忆类型

| 类型 | 用途 |
|------|------|
| `USER` | 用户偏好 |
| `FEEDBACK` | 用户纠正 |
| `PROJECT` | 项目事实 |
| `REFERENCE` | 外部资源 |

## 记忆文件格式

保存在 `.claude/memory/<name>.md`：

```markdown
---
name: user-preferences
description: User coding preferences
type: user
---

# User Preferences

- Use tabs for indentation
- Prefer functional programming style
```

## 何时保存记忆

- 用户陈述偏好："我喜欢使用 tabs"
- 用户纠正："不要做 X，因为..."
- 学习项目事实：不易从代码推断的规则
- 发现外部资源：文档 URL、看板地址

## 何时不保存记忆

- 容易从代码推断的信息
- 临时任务状态
- 敏感信息（API 密钥、密码）

## 本章的局限

- 没有记忆搜索
- 没有记忆过期
- 没有记忆冲突检测

## 下一章

s10 会实现结构化的 system prompt，统一管理角色、约束、技能和记忆。
