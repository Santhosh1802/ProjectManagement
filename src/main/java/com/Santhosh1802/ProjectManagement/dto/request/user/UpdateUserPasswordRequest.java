package com.Santhosh1802.ProjectManagement.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPasswordRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Old Password is required")
    @Size(min = 8,max = 100,message = "Old Password must be between 8 and 100 characters")
    private String oldPassword;

    @NotBlank(message = "New Password is required")
    @Size(min = 8,max = 100,message = "New Password must be between 8 and 100 characters")
    private String newPassword;

}
