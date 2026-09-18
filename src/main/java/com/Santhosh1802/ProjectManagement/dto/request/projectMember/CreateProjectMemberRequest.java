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
public class CreateProjectMemberRequest {

    @NotNull(message = "projectMemberId is required")
    private UUID projectMemberId;
    @NotNull(message = "projectId i required")
    private UUID projectId;
    @NotNull(message = "projectRole is required")
    private ProjectRole projectRole;
}
