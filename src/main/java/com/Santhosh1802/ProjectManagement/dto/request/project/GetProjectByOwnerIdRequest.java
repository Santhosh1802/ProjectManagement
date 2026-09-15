package com.Santhosh1802.ProjectManagement.dto.request.project;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectByOwnerIdRequest {

    @NotNull(message = "Owner id is required")
    private UUID ownerId;
}
