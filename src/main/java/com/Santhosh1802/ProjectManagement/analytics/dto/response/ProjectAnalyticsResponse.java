package com.Santhosh1802.ProjectManagement.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAnalyticsResponse {
    private UUID projectId;
    private String projectName;
    private String projectStatus;
    private Instant startDate;
    private Instant dueDate;
    private String ownerName;

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long todoTasks;
    private long cancelledTasks;
    private long overdueTasks;
    private int completionPercentage;

    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
}
