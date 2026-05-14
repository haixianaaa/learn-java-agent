package com.claudecode.agent.s10.prompt;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SystemPrompt {
    private String role;
    private List<String> guidelines;
    private List<String> constraints;
    private String skillsAvailable;
    private String memory;
    private String claudeMd;
    private String dynamicContext;
    private String memoryGuidance;

    public SystemPrompt() {
        this.guidelines = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }

    public SystemPrompt(String role, List<String> guidelines, List<String> constraints, 
                        String skillsAvailable, String memory, String claudeMd, 
                        String dynamicContext, String memoryGuidance) {
        this.role = role;
        this.guidelines = guidelines != null ? guidelines : new ArrayList<>();
        this.constraints = constraints != null ? constraints : new ArrayList<>();
        this.skillsAvailable = skillsAvailable;
        this.memory = memory;
        this.claudeMd = claudeMd;
        this.dynamicContext = dynamicContext;
        this.memoryGuidance = memoryGuidance;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();

        if (role != null && !role.isEmpty()) {
            sb.append("# Role\n").append(role).append("\n\n");
        }

        if (guidelines != null && !guidelines.isEmpty()) {
            sb.append("# Guidelines\n");
            for (String guideline : guidelines) {
                sb.append("- ").append(guideline).append("\n");
            }
            sb.append("\n");
        }

        if (constraints != null && !constraints.isEmpty()) {
            sb.append("# Constraints\n");
            for (String constraint : constraints) {
                sb.append("- ").append(constraint).append("\n");
            }
            sb.append("\n");
        }

        if (skillsAvailable != null && !skillsAvailable.isEmpty()) {
            sb.append("# Available Skills\n").append(skillsAvailable).append("\n\n");
        }

        if (memory != null && !memory.isEmpty()) {
            sb.append(memory).append("\n\n");
        }

        if (claudeMd != null && !claudeMd.isEmpty()) {
            sb.append(claudeMd).append("\n\n");
        }

        if (dynamicContext != null && !dynamicContext.isEmpty()) {
            sb.append(dynamicContext).append("\n\n");
        }

        if (memoryGuidance != null && !memoryGuidance.isEmpty()) {
            sb.append("# Memory Guidance\n").append(memoryGuidance).append("\n");
        }

        return sb.toString().trim();
    }

    public static SystemPromptBuilder builder() {
        return new SystemPromptBuilder();
    }

    public static class SystemPromptBuilder {
        private String role;
        private List<String> guidelines = new ArrayList<>();
        private List<String> constraints = new ArrayList<>();
        private String skillsAvailable;
        private String memory;
        private String claudeMd;
        private String dynamicContext;
        private String memoryGuidance;

        public SystemPromptBuilder role(String role) {
            this.role = role;
            return this;
        }

        public SystemPromptBuilder guidelines(List<String> guidelines) {
            this.guidelines = guidelines;
            return this;
        }

        public SystemPromptBuilder guideline(String guideline) {
            this.guidelines.add(guideline);
            return this;
        }

        public SystemPromptBuilder constraints(List<String> constraints) {
            this.constraints = constraints;
            return this;
        }

        public SystemPromptBuilder constraint(String constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public SystemPromptBuilder skillsAvailable(String skillsAvailable) {
            this.skillsAvailable = skillsAvailable;
            return this;
        }

        public SystemPromptBuilder memory(String memory) {
            this.memory = memory;
            return this;
        }

        public SystemPromptBuilder claudeMd(String claudeMd) {
            this.claudeMd = claudeMd;
            return this;
        }

        public SystemPromptBuilder dynamicContext(String dynamicContext) {
            this.dynamicContext = dynamicContext;
            return this;
        }

        public SystemPromptBuilder memoryGuidance(String memoryGuidance) {
            this.memoryGuidance = memoryGuidance;
            return this;
        }

        public SystemPrompt build() {
            return new SystemPrompt(role, guidelines, constraints, skillsAvailable, 
                    memory, claudeMd, dynamicContext, memoryGuidance);
        }
    }
}
