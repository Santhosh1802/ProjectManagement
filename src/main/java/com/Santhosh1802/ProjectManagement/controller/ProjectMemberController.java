package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectMemberService;
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
@RequestMapping("/api/project-member")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GetProjectMemberResponse>> createProjectMember(@Valid @RequestBody CreateProjectMemberRequest createProjectMemberRequest){
        GetProjectMemberResponse getProjectMemberResponse = projectMemberService.addMember(createProjectMemberRequest);
        ApiResponse<GetProjectMemberResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "Project Member Added",
                getProjectMemberResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<GetProjectMemberResponse>> getMemberById(@RequestParam UUID id){
        GetByIdRequest getByIdRequest = new GetByIdRequest(id);
        GetProjectMemberResponse getProjectMemberResponse = projectMemberService.getMemberById(getByIdRequest);
        ApiResponse<GetProjectMemberResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project Member Found",
                getProjectMemberResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/project")
    public ResponseEntity<ApiResponse<List<GetProjectMemberResponse>>> getMembersByProject(@RequestParam UUID id){
        GetByIdRequest getByIdRequest = new GetByIdRequest(id);
        List<GetProjectMemberResponse> getProjectMemberResponses = projectMemberService.getMembersByProject(getByIdRequest);
        ApiResponse<List<GetProjectMemberResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project members fetched",
                getProjectMemberResponses
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/member")
    public ResponseEntity<ApiResponse<List<GetProjectMemberResponse>>> getProjectsByUser(@RequestParam UUID id){
        GetByIdRequest getByIdRequest = new GetByIdRequest(id);
        List<GetProjectMemberResponse> getProjectMemberResponses = projectMemberService.getProjectsByUser(getByIdRequest);
        ApiResponse<List<GetProjectMemberResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project members fetched",
                getProjectMemberResponses
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/update-role")
    public ResponseEntity<ApiResponse<GetProjectMemberResponse>> updateMemberRole(@Valid @RequestBody UpdateProjectMemberRoleRequest updateProjectMemberRoleRequest){
        GetProjectMemberResponse getProjectMemberResponse = projectMemberService.updateMemberRole(updateProjectMemberRoleRequest);
        ApiResponse<GetProjectMemberResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project Member Role Updated",
                getProjectMemberResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<Boolean>> removeMember(@Valid @RequestBody DeleteProjectMemberRequest deleteProjectMemberRequest){
        Boolean deleteProjectMemberResponse = projectMemberService.removeMember(deleteProjectMemberRequest);
        ApiResponse<Boolean> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Project member deleted",
                deleteProjectMemberResponse
        );
        return  ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
