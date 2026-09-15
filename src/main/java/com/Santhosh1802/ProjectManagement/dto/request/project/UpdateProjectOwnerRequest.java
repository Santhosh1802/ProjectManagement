package com.Santhosh1802.ProjectManagement.dto.request.project;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Data
@Service
public class UpdateProjectOwnerRequest {
    private UUID id;
    private UUID ownerId;
}
