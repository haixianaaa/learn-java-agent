package com.claudecode.agent.s01.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tool {
    private String name;
    private String description;
    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;

    public static Tool bash() {
        return Tool.builder()
                .name("bash")
                .description("Run a shell command in the current workspace.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "command", Map.of("type", "string")
                        ),
                        "required", List.of("command")
                ))
                .build();
    }
}
