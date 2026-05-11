package com.claudecode.agent.sfull.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = ContentBlock.TextBlock.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ContentBlock.TextBlock.class, name = "text"),
    @JsonSubTypes.Type(value = ContentBlock.ThinkingBlock.class, name = "thinking"),
    @JsonSubTypes.Type(value = ContentBlock.ToolUseBlock.class, name = "tool_use"),
    @JsonSubTypes.Type(value = ContentBlock.ToolResultBlock.class, name = "tool_result")
})
public abstract class ContentBlock {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextBlock extends ContentBlock {
        private String type = "text";
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ThinkingBlock extends ContentBlock {
        private String type = "thinking";
        private String thinking;
        private String signature;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolUseBlock extends ContentBlock {
        private String type = "tool_use";
        private String id;
        private String name;
        private Map<String, Object> input;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolResultBlock extends ContentBlock {
        private String type = "tool_result";
        @JsonProperty("tool_use_id")
        private String toolUseId;
        private String content;
    }

    public static TextBlock text(String text) {
        return TextBlock.builder().text(text).build();
    }

    public static ToolUseBlock toolUse(String id, String name, Map<String, Object> input) {
        return ToolUseBlock.builder().id(id).name(name).input(input).build();
    }

    public static ToolResultBlock toolResult(String toolUseId, String content) {
        return ToolResultBlock.builder().toolUseId(toolUseId).content(content).build();
    }
}
