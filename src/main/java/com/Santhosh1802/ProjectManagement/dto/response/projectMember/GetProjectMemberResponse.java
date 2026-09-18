package com.Santhosh1802.ProjectManagement.dto.response.projectMember;

import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetProjectMemberResponse {
    private UUID id;
    private GetUserResponse user;
    private GetProjectResponse project;
    private ProjectRole projectRole;
    private Instant joinedAt;
}
