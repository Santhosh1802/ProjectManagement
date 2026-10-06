package com.Santhosh1802.ProjectManagement.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
@AllArgsConstructor
@NotNull
public class CreateProjectRequest {
    @NotBlank(message = "project name is required")
    @Size(min = 5,max = 50,message = "Project name must be between 5 to 50 characters")
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(min = 10,max = 200,message = "Project description must be between 10 to 200 characters")
    private String description;

    @NotNull(message = "Project start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant startDate;

    @NotNull(message = "Project due date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant dueDate;

}
