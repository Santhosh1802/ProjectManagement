package com.Santhosh1802.ProjectManagement.dto.request.project;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectOwnerRequest {

    @NotNull(message = "Project id is required")
    private UUID id;

    @NotNull(message = "Owner id is required")
    private UUID ownerId;
}