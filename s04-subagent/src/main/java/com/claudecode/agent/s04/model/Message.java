package com.claudecode.agent.s04.model;

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
public class Message {
    private String role;
    private Object content;

    public static Message text(String role, String text) {
        return Message.builder().role(role).content(text).build();
    }

    public static Message blocks(String role, List<ContentBlock> blocks) {
        return Message.builder().role(role).content(blocks).build();
    }
}
