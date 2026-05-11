package com.claudecode.agent.s05.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SkillRegistry {
    private final Path skillsDir;
    private final Map<String, SkillDocument> skills = new HashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public SkillRegistry(Path skillsDir) {
        this.skillsDir = skillsDir;
    }

    public void loadSkills() throws IOException {
        skills.clear();

        if (!Files.exists(skillsDir)) {
            return;
        }

        Files.walkFileTree(skillsDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("SKILL.md")) {
                    loadSkill(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void loadSkill(Path path) throws IOException {
        String content = Files.readString(path);

        ParsedSkill parsed = parseFrontmatter(content);

        String fallbackName = Optional.ofNullable(path.getParent())
                .map(Path::getFileName)
                .map(Path::toString)
                .orElse("unknown");

        String name = parsed.name != null ? parsed.name : fallbackName;
        String description = parsed.description != null ? parsed.description : "No description";

        SkillManifest manifest = new SkillManifest(name, description, path);
        SkillDocument document = new SkillDocument(manifest, parsed.body);

        skills.put(name, document);
    }

    public String describeAvailable() {
        if (skills.isEmpty()) {
            return "(no skills available)";
        }

        List<String> names = new ArrayList<>(skills.keySet());
        Collections.sort(names);

        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            SkillDocument skill = skills.get(name);
            sb.append(" - ").append(skill.getManifest().getName())
                    .append(": ").append(skill.getManifest().getDescription())
                    .append("\n");
        }

        return sb.toString().trim();
    }

    public String loadFullText(String name) {
        SkillDocument skill = skills.get(name);
        if (skill != null) {
            return skill.toString();
        }

        List<String> names = new ArrayList<>(skills.keySet());
        Collections.sort(names);
        return String.format("Error: Unknown skill '%s'. Available: %s", name, String.join(", ", names));
    }

    public Map<String, SkillDocument> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    private ParsedSkill parseFrontmatter(String text) {
        String normalized = text.replace("\r\n", "\n");

        if (!normalized.startsWith("---\n")) {
            return new ParsedSkill(null, null, normalized.trim());
        }

        String rest = normalized.substring(4);
        int endIndex = rest.indexOf("\n---\n");
        if (endIndex == -1) {
            return new ParsedSkill(null, null, normalized.trim());
        }

        String frontmatter = rest.substring(0, endIndex);
        String body = rest.substring(endIndex + 5).trim();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> meta = yamlMapper.readValue(frontmatter, Map.class);
            String name = (String) meta.get("name");
            String description = (String) meta.get("description");
            return new ParsedSkill(name, description, body);
        } catch (Exception e) {
            return new ParsedSkill(null, null, body);
        }
    }

    private record ParsedSkill(String name, String description, String body) {}
}
