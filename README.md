# Learn Java Agent

A Java implementation of Claude Code Agent learning project, converted from the original Rust implementation.

## Project Structure

This project is a multi-module Maven project that demonstrates the progressive development of an AI coding agent:

- **s01-agent-loop**: Basic agent loop implementation
- **s02-tool-use**: Tool use system with bash, read_file, write_file, edit_file tools
- **s03-todo-write**: Todo write tool implementation
- **s04-subagent**: Subagent implementation
- **s05-skill-loading**: Skill loading system
- **s06-context-compact**: Context compaction system
- **s07-permission-system**: Permission management system
- **s08-hook-system**: Hook system implementation
- **s09-memory-system**: Memory management system
- **s10-system-prompt**: System prompt management
- **s11-error-recovery**: Error recovery system
- **s12-task-system**: Task management system
- **s13-background-tasks**: Background task execution
- **s14-cron-scheduler**: Cron job scheduler
- **s15-agent-teams**: Agent team collaboration
- **s16-team-protocols**: Team communication protocols
- **s17-autonomous-agents**: Autonomous agent implementation
- **s18-worktree-task-isolation**: Worktree task isolation
- **s19-mcp-plugin**: MCP plugin system
- **s20-tool-refactor**: Tool refactoring with annotations
- **sfull**: Complete agent implementation with all features

## Requirements

- Java 17+
- Maven 3.8+

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and configure your API keys
3. Build the project: `mvn clean install`
4. Run a module: `java -jar sfull/target/sfull-1.0-SNAPSHOT.jar`

## Configuration

Create a `.env` file in the project root:

```env
ANTHROPIC_API_KEY=your-api-key-here
ANTHROPIC_BASE_URL=https://api.anthropic.com
```

## Usage

Run the complete agent:

```bash
cd sfull
mvn exec:java -Dexec.mainClass="com.claudecode.agent.sfull.Main"
```

Or after building:

```bash
java -jar sfull/target/sfull-1.0-SNAPSHOT.jar
```

## License

MIT License
