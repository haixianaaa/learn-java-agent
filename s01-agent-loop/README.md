# S01 - Agent Loop

本章实现一个最基础的 agent loop：接收用户输入，调用 LLM API，如果模型返回 tool_use 则执行工具并把结果回填，然后继续循环直到模型停止调用工具。

## 运行方式

```bash
cd s01-agent-loop
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s01.Main"
```

## 本章核心能力

- 实现 agent 主循环：用户输入 -> 模型响应 -> 工具调用 -> 结果回填 -> 继续循环
- 使用 OkHttp 调用 Anthropic/DeepSeek API
- 实现 `bash` 工具执行 shell 命令
- 支持基本的上下文管理

## 代码结构

```text
s01-agent-loop/
├── src/main/java/com/claudecode/agent/s01/
│   ├── Main.java              # 主入口和 agent loop
│   ├── client/
│   │   └── LLMClient.java     # LLM API 客户端
│   ├── model/
│   │   ├── Message.java       # 消息结构
│   │   ├── ContentBlock.java  # 内容块（文本/工具调用/工具结果）
│   │   ├── Tool.java          # 工具定义
│   │   ├── CreateMessageRequest.java
│   │   └── CreateMessageResponse.java
│   └── tool/
│       └── BashTool.java      # Shell 命令执行工具
└── pom.xml
```

## Agent Loop 核心流程

```text
用户输入
  -> 构造请求（messages + tools）
  -> 调用 LLM API
  -> 检查 stop_reason
  -> 如果是 tool_use：执行工具 -> 回填 tool_result -> 继续循环
  -> 如果是 end_turn：返回最终响应
```

## Java 实现要点

- 使用 OkHttp 作为 HTTP 客户端
- 使用 Jackson 进行 JSON 序列化/反序列化
- 使用 Lombok 简化 POJO 定义
- 使用 dotenv-java 加载环境变量

## 配置

在项目根目录创建 `.env` 文件：

```env
ANTHROPIC_API_KEY=your-api-key
ANTHROPIC_BASE_URL=https://api.anthropic.com
# 或使用 DeepSeek
# ANTHROPIC_BASE_URL=https://api.deepseek.com
```

## 本章的局限

- 只有一个 `bash` 工具
- 没有权限系统
- 没有上下文压缩
- 没有错误恢复机制
- 没有持久化

## 下一章

s02 会扩展工具系统，增加文件读写和编辑工具，并引入工具注册机制。
