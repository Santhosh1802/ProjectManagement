package com.Santhosh1802.ProjectManagement.dto.request.user;


import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserBySearchRequest {
    private String keyword;
    @Min(value = 0,message = "page should be at least 0")
    private Integer page;
    @Min(value = 10,message = "size should be at least 10")
    private Integer size;
}
