package com.claudecode.agent.s12.task;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TaskUpdate {
    private TaskStatus status;
    private String owner;
    private List<Long> addBlockedBy = new ArrayList<>();
    private List<Long> addBlocks = new ArrayList<>();
}
