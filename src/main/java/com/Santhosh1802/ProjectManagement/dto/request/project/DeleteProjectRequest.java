package com.Santhosh1802.ProjectManagement.dto.request.project;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProjectRequest {

    @NotNull(message = "Project id is required")
    private UUID id;
}
