package com.Santhosh1802.ProjectManagement.dto.response.auth;

import com.Santhosh1802.ProjectManagement.util.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole;
    private Boolean isActive;
    private Boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
