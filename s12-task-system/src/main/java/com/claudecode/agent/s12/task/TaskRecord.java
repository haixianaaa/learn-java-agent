package com.claudecode.agent.s12.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskRecord {
    private long id;
    private String subject;
    private String description;
    private TaskStatus status;
    @JsonProperty("blockedBy")
    private List<Long> blockedBy;
    private List<Long> blocks;
    private String owner;

    public static TaskRecord create(long id, String subject, String description) {
        TaskRecord record = new TaskRecord();
        record.setId(id);
        record.setSubject(subject);
        record.setDescription(description);
        record.setStatus(TaskStatus.PENDING);
        record.setBlockedBy(new ArrayList<>());
        record.setBlocks(new ArrayList<>());
        record.setOwner("");
        return record;
    }
}
