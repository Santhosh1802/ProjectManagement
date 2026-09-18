package com.Santhosh1802.ProjectManagement.dto.request.projectMember;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteProjectMemberRequest {

    @NotNull(message = "id is required")
    private UUID id;

}
