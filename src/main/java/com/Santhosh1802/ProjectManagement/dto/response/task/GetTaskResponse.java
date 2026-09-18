package com.Santhosh1802.ProjectManagement.dto.response.task;

import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus taskStatus;
    private TaskPriority taskPriority;
    private Instant dueDate;
    private GetProjectResponse project;
    private GetUserResponse assignedTo;
    private GetUserResponse createdBy;
    private Instant createdAt;
    private Instant updatedAt;

}
