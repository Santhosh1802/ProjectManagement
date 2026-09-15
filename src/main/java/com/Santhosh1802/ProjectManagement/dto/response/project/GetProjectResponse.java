package com.Santhosh1802.ProjectManagement.dto.response.project;

import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private ProjectStatus projectStatus;
    private Instant startDate;
    private Instant dueDate;
    private ProjectOwnerResponse owner;
    private Instant createdAt;
    private Instant updatedAt;
}
