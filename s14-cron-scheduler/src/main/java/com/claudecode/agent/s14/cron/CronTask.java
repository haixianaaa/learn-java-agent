package com.claudecode.agent.s14.cron;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CronTask {
    private String id;
    private String name;
    private String cronExpression;
    private String prompt;
    private boolean enabled;
    private Instant lastRun;
    private Instant nextRun;

    public static CronTask create(String name, String cronExpression, String prompt) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return new CronTask(id, name, cronExpression, prompt, true, null, null);
    }

    public CronTask withLastRun(Instant time) {
        return new CronTask(id, name, cronExpression, prompt, enabled, time, nextRun);
    }

    public CronTask withNextRun(Instant time) {
        return new CronTask(id, name, cronExpression, prompt, enabled, lastRun, time);
    }

    public CronTask disable() {
        return new CronTask(id, name, cronExpression, prompt, false, lastRun, nextRun);
    }
}
