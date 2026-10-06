package com.Santhosh1802.ProjectManagement.dto.request.project;

import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
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
public class UpdateProjectDetailsRequest {

    @NotNull(message = "Project id is required")
    private UUID id;

    @NotBlank(message = "Project name is required")
    @Size(
            min = 5,
            max = 50,
            message = "Project name must be between 5 to 50 characters"
    )
    private String name;

    @Size(
            min = 10,
            max = 200,
            message = "Project description must be between 10 to 200 characters"
    )
    private String description;

    private ProjectStatus projectStatus;

    private Instant dueDate;
}