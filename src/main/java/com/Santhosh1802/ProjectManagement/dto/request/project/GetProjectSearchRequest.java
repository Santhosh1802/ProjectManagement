package com.Santhosh1802.ProjectManagement.dto.request.project;

import com.Santhosh1802.ProjectManagement.util.ProjectDueDateFilter;
import com.Santhosh1802.ProjectManagement.util.ProjectScope;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectSearchRequest {

    @Min(value = 0, message = "Page must be at least 0")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    private Integer size = 10;

    private String keyword;

    private ProjectScope scope = ProjectScope.ALL;

    private ProjectStatus status;

    private UUID ownerId;

    private ProjectDueDateFilter dueDate =
            ProjectDueDateFilter.ALL;

    private String sortBy = "createdAt";

    private String sortOrder = "desc";
}
