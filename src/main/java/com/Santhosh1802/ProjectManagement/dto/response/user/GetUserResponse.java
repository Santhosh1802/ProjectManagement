package com.Santhosh1802.ProjectManagement.dto.response.user;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetUserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole;
    private Boolean isActive;
    private Boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private List<Project> ownedProjects;
    private List<Task> createdTasks;
    private List<Task> assignedTasks;
    private List<ProjectMember> projectMemberships;
}
