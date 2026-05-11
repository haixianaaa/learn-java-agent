package com.claudecode.agent.s07.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRule {
    private String tool;
    private String path;
    private String content;
    private PermissionBehavior behavior;

    public static PermissionRule allowTool(String tool) {
        return new PermissionRule(tool, null, null, PermissionBehavior.ALLOW);
    }

    public static PermissionRule denyToolContent(String tool, String content) {
        return new PermissionRule(tool, null, content, PermissionBehavior.DENY);
    }

    public PermissionRule withPath(String path) {
        this.path = path;
        return this;
    }

    public boolean matches(String toolName, Map<String, Object> toolInput) {
        if (!tool.equals("*") && !tool.equals(toolName)) {
            return false;
        }

        if (path != null) {
            String inputPath = toolInput.containsKey("path") 
                    ? (String) toolInput.get("path") 
                    : "";
            if (!wildcardMatch(path, inputPath)) {
                return false;
            }
        }

        if (content != null) {
            String command = toolInput.containsKey("command") 
                    ? (String) toolInput.get("command") 
                    : "";
            if (!wildcardMatch(content, command)) {
                return false;
            }
        }

        return true;
    }

    private boolean wildcardMatch(String pattern, String text) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        return Pattern.matches(regex, text);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("tool=").append(tool);
        if (path != null) sb.append(", path=").append(path);
        if (content != null) sb.append(", content=").append(content);
        sb.append(", behavior=").append(behavior.getValue());
        return sb.toString();
    }
}
