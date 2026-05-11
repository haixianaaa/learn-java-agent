# S04 - Subagent

本章引入 subagent 机制：主 agent 可以启动一个拥有独立上下文的子代理来执行子任务，完成后返回总结。

## 运行方式

```bash
cd s04-subagent
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s04.Main"
```

## 本章新增能力

- 新增 `SubAgent` 类
- 新增 `task` 工具启动子代理
- 子代理拥有独立的上下文，不共享主 agent 的对话历史
- 子代理共享文件系统
- 子代理有最大轮次限制（30 轮）

## 代码结构

```text
s04-subagent/
├── src/main/java/com/claudecode/agent/s04/
│   ├── Main.java
│   ├── agent/
│   │   └── SubAgent.java        # 子代理实现
│   ├── client/
│   │   └── LLMClient.java
│   ├── model/
│   │   └── ...
│   └── tool/
│       ├── ToolExecutor.java
│       ├── ToolRegistry.java
│       ├── SubAgentTool.java    # task 工具
│       ├── BashTool.java
│       ├── ReadFileTool.java
│       ├── WriteFileTool.java
│       └── EditFileTool.java
└── pom.xml
```

## Subagent vs Teammate

| 特性 | Subagent | Teammate |
|------|----------|----------|
| 生命周期 | 一次性 | 长期存活 |
| 上下文 | 独立新建 | 可恢复 |
| 通信 | 返回总结 | inbox 消息 |
| 身份 | 匿名 | 有名字和角色 |

## task 工具

```json
{
  "prompt": "Search for all TODO comments in the codebase",
  "description": "Find all TODOs"
}
```

## 子代理工具集

子代理默认只拥有读取类工具：
- `bash`（只读命令）
- `read_file`

这是为了限制子代理的修改能力。

## 本章的局限

- 子代理没有持久化状态
- 没有子代理取消机制
- 没有子代理超时处理

## 下一章

s05 会引入 skill 加载系统，让 agent 可以从文件加载预定义的技能。
