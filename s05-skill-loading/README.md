# S05 - Skill Loading

本章实现技能加载系统，让 agent 可以从文件加载预定义的技能。技能是可发现但不会默认塞满上下文的知识模块。

## 运行方式

```bash
cd s05-skill-loading
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s05.Main"
```

## 本章新增能力

- 新增 `SkillRegistry` 管理技能
- 支持 `SKILL.md` 文件格式（YAML frontmatter + Markdown body）
- 技能摘要注入 system prompt
- 完整技能正文按需加载

## 代码结构

```text
s05-skill-loading/
├── src/main/java/com/claudecode/agent/s05/
│   ├── Main.java
│   └── skill/
│       ├── SkillManifest.java   # 技能元数据
│       ├── SkillDocument.java   # 技能文档
│       └── SkillRegistry.java   # 技能注册表
└── pom.xml
```

## SKILL.md 格式

```markdown
---
name: code-review
description: Code review guidelines
type: project
---

# Code Review Guidelines

1. Check for security issues
2. Verify test coverage
3. Review error handling
```

## 技能加载流程

```text
扫描 skills/ 目录
  -> 解析 SKILL.md 文件
  -> 提取 YAML frontmatter
  -> 缓存技能摘要
  -> 按需加载完整正文
```

## 技能类型

- `user`: 用户偏好
- `feedback`: 用户纠正
- `project`: 项目事实
- `reference`: 外部资源

## 设计原则

技能系统遵循一个原则：**让知识可发现，但不要默认塞满上下文。**

System prompt 中只放技能摘要，完整技能由 `load_skill` 工具按需加载。

## 本章的局限

- 没有技能版本管理
- 没有技能依赖关系
- 没有技能搜索功能

## 下一章

s06 会实现上下文压缩系统，当对话历史过长时自动压缩。
