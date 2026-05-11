# S19 - MCP Plugin

本章实现 MCP (Model Context Protocol) 插件系统，支持插件化扩展工具。

## 运行方式

```bash
cd s19-mcp-plugin
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s19.Main"
```

## 本章新增能力

- 新增 `MCPPlugin` 接口定义插件
- 新增 `MCPPluginManager` 管理插件
- 内置 Filesystem 和 Database 插件
- 支持动态注册/注销插件

## 代码结构

```text
s19-mcp-plugin/
├── src/main/java/com/claudecode/agent/s19/
│   ├── Main.java
│   └── mcp/
│       ├── MCPPlugin.java        # 插件接口
│       ├── MCPPluginManager.java # 插件管理器
│       ├── FilesystemPlugin.java # 文件系统插件
│       └── DatabasePlugin.java   # 数据库插件
└── pom.xml
```

## MCPPlugin 接口

```java
public interface MCPPlugin {
    String name();
    String description();
    List<String> tools();
    Object callTool(String toolName, Map<String, Object> params);
    Map<String, Object> toolSpec(String toolName);
}
```

## 内置插件

### FilesystemPlugin

- `read_file`: 读取文件
- `write_file`: 写入文件
- `list_directory`: 列出目录
- `create_directory`: 创建目录
- `delete_file`: 删除文件

### DatabasePlugin

- `query`: 执行查询
- `insert`: 插入数据
- `update`: 更新数据
- `delete`: 删除数据
- `list_tables`: 列出表

## 本章的局限

- 没有插件配置
- 没有插件依赖
- 没有插件沙箱

## 下一章

s20 会实现工具重构系统，使用注解简化工具定义。
