package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.project.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GetProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest){
        GetProjectResponse getProjectResponse = projectService.createProject(createProjectRequest);

        ApiResponse<GetProjectResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "Project created successfully",
                getProjectResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<GetProjectResponse>> getProjectById(@RequestParam UUID id){
        GetProjectByIdRequest getProjectByIdRequest = new GetProjectByIdRequest(id);
        GetProjectResponse getProjectResponse = projectService.getProjectById(getProjectByIdRequest);

        ApiResponse<GetProjectResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project found successfully",
                getProjectResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetProjectResponse>>> getAllProjects(){
        List<GetProjectResponse> getProjectResponseList = projectService.getAllProjects();
        ApiResponse<List<GetProjectResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Fetched projects",
                getProjectResponseList
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<GetProjectResponse>>> getProjectByOwner(@Valid @RequestBody GetProjectByOwnerIdRequest getProjectByOwnerIdRequest){
        List<GetProjectResponse> getProjectResponseList = projectService.getProjectsByOwner(getProjectByOwnerIdRequest);
        ApiResponse<List<GetProjectResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Fetched projects",
                getProjectResponseList
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/update/details")
    public ResponseEntity<ApiResponse<GetProjectResponse>> updateProjectDetails(@Valid @RequestBody UpdateProjectDetailsRequest updateProjectDetailsRequest){
        GetProjectResponse getProjectResponse = projectService.updateProjectDetails(updateProjectDetailsRequest);
        ApiResponse<GetProjectResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project Details updated successfully",
                getProjectResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }
    @PutMapping("/update/owner")
    public ResponseEntity<ApiResponse<GetProjectResponse>> updateProjectOwner(@Valid @RequestBody UpdateProjectOwnerRequest updateProjectOwnerRequest){
        GetProjectResponse getProjectResponse = projectService.updateProjectOwner(updateProjectOwnerRequest);
        ApiResponse<GetProjectResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project Details updated successfully",
                getProjectResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteProject(@Valid @RequestBody DeleteProjectRequest deleteProjectRequest){
        Boolean deleteProjectResponse = projectService.deleteProject(deleteProjectRequest);
        ApiResponse<Boolean> response = new ApiResponse<>(
            LocalDateTime.now(),
            HttpStatus.OK.value(),
            "Project deleted successfully",
            deleteProjectResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }




}
