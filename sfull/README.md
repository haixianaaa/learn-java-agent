# SFull - Complete Agent

完整实现，整合了 s01-s20 的所有功能。

## 运行方式

```bash
cd sfull
mvn exec:java -Dexec.mainClass="com.claudecode.agent.sfull.Main"
```

## 包含的功能

| 模块 | 功能 |
|------|------|
| s01 | Agent Loop |
| s02 | Tool Use (bash, read_file, write_file, edit_file) |
| s03 | Todo Write |
| s04 | Subagent |
| s05 | Skill Loading |
| s06 | Context Compact |
| s07 | Permission System |
| s08 | Hook System |
| s09 | Memory System |
| s10 | System Prompt |
| s11 | Error Recovery |
| s12 | Task System |
| s13 | Background Tasks |
| s14 | Cron Scheduler |
| s15 | Agent Teams |
| s16 | Team Protocols |
| s17 | Autonomous Agents |
| s18 | Worktree Task Isolation |
| s19 | MCP Plugin |
| s20 | Tool Refactor |

## 代码结构

```text
sfull/
├── src/main/java/com/claudecode/agent/sfull/
│   ├── Main.java
│   ├── agent/
│   │   └── Agent.java
│   ├── client/
│   │   └── LLMClient.java
│   ├── model/
│   │   ├── Message.java
│   │   ├── ContentBlock.java
│   │   ├── Tool.java
│   │   ├── CreateMessageRequest.java
│   │   └── CreateMessageResponse.java
│   └── tool/
│       ├── ToolExecutor.java
│       ├── ToolContext.java
│       ├── ToolRouter.java
│       ├── BashTool.java
│       ├── ReadFileTool.java
│       ├── WriteFileTool.java
│       └── EditFileTool.java
└── pom.xml
```

## 配置

在项目根目录创建 `.env` 文件：

```env
ANTHROPIC_API_KEY=your-api-key
ANTHROPIC_BASE_URL=https://api.anthropic.com
# 或使用 DeepSeek
# ANTHROPIC_BASE_URL=https://api.deepseek.com
```

## 使用

启动后，输入问题与 Agent 交互：

```text
--- How can I help you? 列出当前目录的文件

Command: bash
Arg: {command=ls -la}
Output:
total 32
drwxr-xr-x  5 user user 4096 Jan 15 10:00 .
...

--- Final response:
当前目录包含以下文件：
...
```

## 打包

```bash
mvn package
java -jar target/sfull-1.0-SNAPSHOT.jar
```
