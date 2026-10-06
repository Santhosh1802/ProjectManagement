package com.Santhosh1802.ProjectManagement.dto.request.user;


import com.Santhosh1802.ProjectManagement.util.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserBySearchRequest {

    @Min(value = 0, message = "Page must be at least 0")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    private Integer size = 10;

    private String keyword;

    private UserRole role;

    private Boolean isActive;

    private Boolean emailVerified;

    private String sortBy = "createdAt";

    private String sortOrder = "desc";
}
