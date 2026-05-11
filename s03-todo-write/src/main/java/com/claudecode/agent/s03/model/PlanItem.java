package com.claudecode.agent.s03.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanItem {
    private String content;
    private PlanItemStatus status;
    private String activeForm;

    @Override
    public String toString() {
        String marker = status.getMarker();
        if (activeForm != null && !activeForm.isEmpty() && status == PlanItemStatus.IN_PROGRESS) {
            return String.format("%s %s (%s)", marker, content, activeForm);
        }
        return String.format("%s %s", marker, content);
    }
}
