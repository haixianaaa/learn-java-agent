package com.claudecode.agent.s05.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillManifest {
    private String name;
    private String description;
    private Path path;
}
