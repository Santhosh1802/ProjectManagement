package com.Santhosh1802.ProjectManagement.dto.response.user;

import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetUserOwnedProjectsResponse {
    private UUID id;
    private List<GetProjectResponse> ownedProjects;
}
