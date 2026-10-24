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
public class ProjectProgressDto {
    private UUID projectId;
    private String projectName;
    private String projectStatus;
    private long totalTasks;
    private long completedTasks;
    private int completionPercentage;
    private Instant dueDate;
}
