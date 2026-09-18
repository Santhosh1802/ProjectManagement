package com.Santhosh1802.ProjectManagement.dto.request.task;

import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "id is required")
    private UUID id;
    @NotNull(message = "status is required")
    private TaskStatus status;
}
