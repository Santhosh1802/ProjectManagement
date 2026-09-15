package com.Santhosh1802.ProjectManagement.dto.request.project;

import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectDetailsRequest {
    private UUID id;
    private String name;
    private String description;
    private ProjectStatus projectStatus;
    private Instant dueDate;

}
