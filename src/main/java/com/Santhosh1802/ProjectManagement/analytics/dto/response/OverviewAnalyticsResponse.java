package com.Santhosh1802.ProjectManagement.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverviewAnalyticsResponse {
    private long totalProjects;
    private long activeProjects;
    private long completedProjects;
    private long planningProjects;

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long todoTasks;
    private long cancelledTasks;
    private long overdueTasks;
    private int completionPercentage;

    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;

    private List<ProjectProgressDto> projectProgress;
    private List<OverdueTaskDto> overdueTasksList;
    private List<UpcomingTaskDto> upcomingTasksList;
}
