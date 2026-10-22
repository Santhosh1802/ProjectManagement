package com.Santhosh1802.ProjectManagement.audit.controller;

import com.Santhosh1802.ProjectManagement.audit.dto.response.ActivityLogResponse;
import com.Santhosh1802.ProjectManagement.audit.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskAuthorization.canViewTask(#taskId, authentication)")
    public ResponseEntity<List<ActivityLogResponse>> getTaskActivity(@PathVariable UUID taskId) {
        return ResponseEntity.ok(activityLogService.getTaskActivity(taskId));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectAuthorization.canView(#projectId, authentication)")
    public ResponseEntity<List<ActivityLogResponse>> getProjectActivity(@PathVariable UUID projectId) {
        return ResponseEntity.ok(activityLogService.getProjectActivity(projectId));
    }
}
