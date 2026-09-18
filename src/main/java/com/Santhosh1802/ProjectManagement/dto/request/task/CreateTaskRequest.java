package com.Santhosh1802.ProjectManagement.dto.request.task;

import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "title is required")
    @Size(min = 1, max = 100,message = "title should be of length between 1 and 100 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(min = 10, max = 300 , message = "description should be of length between 10 and 300 characters")
    private String description;

    @NotNull(message = "taskStatus is required")
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @NotNull(message = "taskPriority is required")
    @Enumerated(EnumType.STRING)
    private TaskPriority taskPriority;

    @NotNull(message = "dueDate is required")
    private Instant dueDate;

    @NotNull(message = "project id is required")
    private UUID projectId;

    @NotNull(message = "assigned to id is required")
    private UUID assignedToId;

    @NotNull(message = "created by id is required")
    private UUID createdById;





}
