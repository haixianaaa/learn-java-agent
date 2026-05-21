# Learn Java Agent - 项目规则

## 项目概述

这是一个多模块 Maven 项目，逐步构建一个 AI Coding Agent。每个模块（s01 ~ s21 + sfull）是一个独立的章节，演示 Agent 的一个核心能力。

## 技术栈

- Java 17+
- Maven 3.8+（多模块，父 POM 统一管理依赖版本）
- Lombok（@Data, @AllArgsConstructor 等）
- Jackson 2.16.1（JSON 序列化）
- OkHttp 4.12.0（HTTP 请求）
- dotenv-java 3.0.0（环境变量加载）

## 模块命名规范

- 目录格式：`s{两位数字}-{kebab-case描述}`，如 `s01-agent-loop`、`s21-rag`
- 包名格式：`com.claudecode.agent.s{两位数字}`，如 `com.claudecode.agent.s21`
- 主类：每个模块的 Main 类位于 `com.claudecode.agent.s{两位数字}.Main`
- 子包：按功能领域划分，如 `rag/`、`mcp/`、`annotation/`、`registry/`

## 代码风格

- 不添加注释，除非用户明确要求
- 使用 Lombok 注解减少模板代码（@Data, @AllArgsConstructor, @Getter 等）
- switch 表达式使用箭头语法：`case "foo" -> { yield result; }`
- 集合初始化优先使用 `List.of()`、`Map.of()` 等不可变工厂方法
- 并发场景使用 ConcurrentHashMap
- 每个模块的 Main.java 提供交互式命令行演示（Scanner + while loop）

## 模块结构模板

每个章节模块包含：

```text
s{NN}-{name}/
├── pom.xml          # 继承父 POM，声明 mainClass
├── s{NN}.md         # 中文文档，说明目标、新增能力、代码结构、局限
└── src/main/java/com/claudecode/agent/s{NN}/
    ├── Main.java    # 交互式入口
    └── {domain}/    # 功能子包
```

## POM 规范

- 所有子模块 pom.xml 继承父 POM（groupId: com.claudecode, artifactId: learn-java-agent）
- 使用 maven-shade-plugin 打包可执行 JAR
- 依赖版本由父 POM dependencyManagement 统一管理，子模块不指定 version
- 新增模块后必须在父 POM 的 <modules> 中添加声明

## 文档规范（s{NN}.md）

- 使用中文撰写
- 包含以下章节：目标、运行方式、本章新增能力、代码结构、核心组件说明、本章的局限、下一步
- 代码结构使用 tree 格式展示
- 接口和关键代码片段用 ```java 代码块展示

## 构建与运行

- 编译：`mvn compile -q`（在模块目录下）
- 运行：`mvn exec:java -Dexec.mainClass=com.claudecode.agent.s{NN}.Main`（PowerShell 环境下 -D 参数需要用引号包裹）
- 打包：`mvn package`（生成 shade JAR）

## 环境变量

- API Key 等敏感信息存放在项目根目录 `.env` 文件中
- `.env` 已加入 `.gitignore`，不得提交
- 参考 `.env.example` 进行配置

## 注意事项

- 新增模块时，同步更新父 POM 的 modules 列表和 README.md
- 每个模块保持独立可运行，不依赖其他模块的代码
- 模拟实现（如 s21 的 Embedding）需在文档和代码中明确标注
- 遵循渐进式教学原则：每个章节只聚焦一个核心概念
