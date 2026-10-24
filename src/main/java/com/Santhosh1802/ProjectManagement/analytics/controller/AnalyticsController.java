package com.Santhosh1802.ProjectManagement.analytics.controller;

import com.Santhosh1802.ProjectManagement.analytics.dto.response.OverviewAnalyticsResponse;
import com.Santhosh1802.ProjectManagement.analytics.dto.response.ProjectAnalyticsResponse;
import com.Santhosh1802.ProjectManagement.analytics.dto.response.WorkloadAnalyticsResponse;
import com.Santhosh1802.ProjectManagement.analytics.service.AnalyticsService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Overview metrics across all accessible projects.
     * GET /api/analytics/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewAnalyticsResponse>> getOverview(Authentication authentication) {
        OverviewAnalyticsResponse overview = analyticsService.getOverviewAnalytics(authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Overview analytics retrieved successfully",
                overview
        ));
    }

    /**
     * Project specific metrics.
     * GET /api/analytics/projects/{projectId}
     */
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectAuthorization.canView(#projectId, authentication)")
    public ResponseEntity<ApiResponse<ProjectAnalyticsResponse>> getProjectAnalytics(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        ProjectAnalyticsResponse projectAnalytics = analyticsService.getProjectAnalytics(projectId, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project analytics retrieved successfully",
                projectAnalytics
        ));
    }

    /**
     * Team workload summary.
     * GET /api/analytics/workload?projectId=...
     */
    @GetMapping("/workload")
    public ResponseEntity<ApiResponse<WorkloadAnalyticsResponse>> getWorkload(
            @RequestParam(required = false) UUID projectId,
            Authentication authentication
    ) {
        WorkloadAnalyticsResponse workload = analyticsService.getWorkloadAnalytics(projectId, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Workload analytics retrieved successfully",
                workload
        ));
    }
}
