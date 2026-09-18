package com.Santhosh1802.ProjectManagement.dto.request.projectMember;

import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectMemberRoleRequest {
    @NotNull(message = "id is required")
    private UUID id;
    @NotNull(message = "role is required")
    private ProjectRole projectRole;
}
