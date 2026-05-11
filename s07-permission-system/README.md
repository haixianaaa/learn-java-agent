# S07 - Permission System

本章实现权限系统，控制工具调用的权限。支持多种权限模式和自定义规则。

## 运行方式

```bash
cd s07-permission-system
mvn exec:java -Dexec.mainClass="com.claudecode.agent.s07.Main"
```

## 本章新增能力

- 新增 `PermissionManager` 管理权限
- 支持三种权限模式：`DEFAULT`、`PLAN`、`AUTO`
- 支持自定义权限规则
- 实现 Bash 安全验证器

## 代码结构

```text
s07-permission-system/
├── src/main/java/com/claudecode/agent/s07/
│   ├── Main.java
│   └── permission/
│       ├── PermissionMode.java        # 权限模式
│       ├── PermissionBehavior.java    # 权限行为
│       ├── PermissionDecision.java    # 权限决策
│       ├── PermissionRule.java        # 权限规则
│       ├── PermissionManager.java     # 权限管理器
│       └── BashSecurityValidator.java # Bash 安全验证
└── pom.xml
```

## 权限模式

| 模式 | 描述 |
|------|------|
| `DEFAULT` | 默认询问写入操作 |
| `PLAN` | 只读模式，拒绝所有写入 |
| `AUTO` | 自动允许读取，询问高风险操作 |

## 权限行为

- `ALLOW`: 允许执行
- `DENY`: 拒绝执行
- `ASK`: 询问用户

## 权限规则

```java
PermissionRule.denyToolContent("bash", "rm -rf /");
PermissionRule.allowTool("read_file").withPath("*");
```

## Bash 安全验证

检测危险命令模式：
- `shell_metachar`: Shell 元字符
- `sudo`: sudo 命令
- `rm_rf`: 删除命令
- `cmd_substitution`: 命令替换
- `ifs_injection`: IFS 注入

## 本章的局限

- 规则引擎简单
- 没有审计日志
- 没有权限缓存

## 下一章

s08 会引入钩子系统，在工具调用前后执行自定义逻辑。
