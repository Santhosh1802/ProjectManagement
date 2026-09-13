package com.Santhosh1802.ProjectManagement.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private Integer status;
    private String message;
    private T data;
}
