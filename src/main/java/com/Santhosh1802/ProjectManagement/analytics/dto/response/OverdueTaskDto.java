package com.Santhosh1802.ProjectManagement.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverdueTaskDto {
    private UUID taskId;
    private String title;
    private String taskPriority;
    private String taskStatus;
    private Instant dueDate;
    private UUID projectId;
    private String projectName;
    private String assignedToName;
}
