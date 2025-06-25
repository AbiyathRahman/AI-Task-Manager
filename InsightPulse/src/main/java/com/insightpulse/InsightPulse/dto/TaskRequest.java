package com.insightpulse.InsightPulse.dto;

import com.insightpulse.InsightPulse.model.Priority;
import com.insightpulse.InsightPulse.model.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private String taskName;
    private String taskDescription;
    private String taskDueDate;
    private Priority taskPriority;
    private Status taskStatus;
    private Long userId;
}
