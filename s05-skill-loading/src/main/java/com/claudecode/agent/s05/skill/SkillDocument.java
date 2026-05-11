package com.claudecode.agent.s05.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDocument {
    private SkillManifest manifest;
    private String body;

    @Override
    public String toString() {
        return String.format("<skill name=\"%s\">\n%s\n</skill>", manifest.getName(), body);
    }
}
