package com.claudecode.agent.s07.permission;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PermissionManager {
    private static final List<String> READ_ONLY_TOOLS = List.of("read_file", "bash_readonly");
    private static final List<String> WRITE_TOOLS = List.of("write_file", "edit_file", "bash");

    @Getter
    private PermissionMode mode;
    private final List<PermissionRule> rules;
    private final BashSecurityValidator bashValidator;
    private int consecutiveDenials;
    private static final int MAX_CONSECUTIVE_DENIALS = 3;

    public PermissionManager(PermissionMode mode) {
        this.mode = mode;
        this.rules = defaultRules();
        this.bashValidator = new BashSecurityValidator();
        this.consecutiveDenials = 0;
    }

    public static List<PermissionRule> defaultRules() {
        return List.of(
                PermissionRule.denyToolContent("bash", "rm -rf /"),
                PermissionRule.denyToolContent("bash", "sudo *"),
                PermissionRule.allowTool("read_file").withPath("*")
        );
    }

    public void setMode(PermissionMode mode) {
        this.mode = mode;
    }

    public PermissionDecision check(String toolName, Map<String, Object> toolInput) {
        PermissionDecision bashSecurity = checkBashSecurity(toolName, toolInput);
        if (bashSecurity != null) {
            return bashSecurity;
        }

        PermissionDecision denyRule = matchRule(toolName, toolInput, PermissionBehavior.DENY);
        if (denyRule != null) {
            return denyRule;
        }

        PermissionDecision modeDecision = checkMode(toolName);
        if (modeDecision != null) {
            return modeDecision;
        }

        PermissionDecision allowRule = matchRule(toolName, toolInput, PermissionBehavior.ALLOW);
        if (allowRule != null) {
            consecutiveDenials = 0;
            return allowRule;
        }

        return new PermissionDecision(PermissionBehavior.ASK, 
                "No rule matched for " + toolName + ", asking user");
    }

    private PermissionDecision checkBashSecurity(String toolName, Map<String, Object> toolInput) {
        if (!toolName.equals("bash")) {
            return null;
        }

        String command = toolInput.containsKey("command") 
                ? (String) toolInput.get("command") 
                : "";

        List<ValidationFailure> failures = bashValidator.validate(command);
        if (failures.isEmpty()) {
            return null;
        }

        boolean hasSevere = failures.stream()
                .anyMatch(f -> f.getName().equals("sudo") || f.getName().equals("rm_rf"));

        String summary = bashValidator.describeFailures(command);

        if (hasSevere) {
            return new PermissionDecision(PermissionBehavior.DENY, "Bash validator: " + summary);
        } else {
            return new PermissionDecision(PermissionBehavior.ASK, "Bash validator flagged: " + summary);
        }
    }

    private PermissionDecision matchRule(String toolName, Map<String, Object> toolInput, 
                                          PermissionBehavior behavior) {
        for (PermissionRule rule : rules) {
            if (rule.getBehavior() == behavior && rule.matches(toolName, toolInput)) {
                String reason = behavior == PermissionBehavior.ALLOW
                        ? "Matched allow rule: " + rule
                        : "Blocked by deny rule: " + rule;
                return new PermissionDecision(behavior, reason);
            }
        }
        return null;
    }

    private PermissionDecision checkMode(String toolName) {
        return switch (mode) {
            case PLAN -> {
                if (isWriteTool(toolName)) {
                    yield new PermissionDecision(PermissionBehavior.DENY, 
                            "Plan mode: write operations are blocked");
                } else {
                    yield new PermissionDecision(PermissionBehavior.ALLOW, 
                            "Plan mode: read-only allowed");
                }
            }
            case AUTO -> {
                if (isReadOnlyTool(toolName) || toolName.equals("read_file")) {
                    yield new PermissionDecision(PermissionBehavior.ALLOW, 
                            "Auto mode: read-only tool auto-approved");
                }
                yield null;
            }
            default -> null;
        };
    }

    private boolean isReadOnlyTool(String toolName) {
        return READ_ONLY_TOOLS.contains(toolName);
    }

    private boolean isWriteTool(String toolName) {
        return WRITE_TOOLS.contains(toolName);
    }

    @Getter
    public static class ValidationFailure {
        private final String name;
        private final String pattern;

        public ValidationFailure(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
        }
    }

    public static class BashSecurityValidator {
        private final List<ValidationRule> validators;

        public BashSecurityValidator() {
            this.validators = List.of(
                    new ValidationRule("shell_metachar", "[;&|`$]"),
                    new ValidationRule("sudo", "\\bsudo\\b"),
                    new ValidationRule("rm_rf", "\\brm\\s+(-[a-zA-Z]*)?r"),
                    new ValidationRule("cmd_substitution", "\\$\\("),
                    new ValidationRule("ifs_injection", "\\bIFS\\s*=")
            );
        }

        public List<ValidationFailure> validate(String command) {
            List<ValidationFailure> failures = new ArrayList<>();
            for (ValidationRule rule : validators) {
                if (rule.pattern.matcher(command).find()) {
                    failures.add(new ValidationFailure(rule.name, rule.patternString));
                }
            }
            return failures;
        }

        public String describeFailures(String command) {
            List<ValidationFailure> failures = validate(command);
            if (failures.isEmpty()) {
                return "No issues detected";
            }

            StringBuilder sb = new StringBuilder("Security flags: ");
            for (int i = 0; i < failures.size(); i++) {
                if (i > 0) sb.append(", ");
                ValidationFailure f = failures.get(i);
                sb.append(f.getName()).append(" (pattern: ").append(f.getPattern()).append(")");
            }
            return sb.toString();
        }
    }

    @Getter
    public static class ValidationRule {
        private final String name;
        private final String patternString;
        private final Pattern pattern;

        public ValidationRule(String name, String patternString) {
            this.name = name;
            this.patternString = patternString;
            this.pattern = Pattern.compile(patternString);
        }
    }
}
