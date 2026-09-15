package com.Santhosh1802.ProjectManagement.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOwnerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
