package com.claudecode.agent.s03.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    private String role;
    private List<ContentBlock> content;

    public static Message text(String role, String text) {
        return Message.builder()
                .role(role)
                .content(List.of(ContentBlock.text(text)))
                .build();
    }

    public static Message blocks(String role, List<ContentBlock> blocks) {
        return Message.builder()
                .role(role)
                .content(blocks)
                .build();
    }
}
