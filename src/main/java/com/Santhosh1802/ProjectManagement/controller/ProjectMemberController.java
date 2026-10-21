package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectMemberService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-member")
@Validated
public class ProjectMemberController {

    @Autowired
    private ProjectMemberService projectMemberService;

    /**
     * Adds a user as a member of a project.
     *
     * <p>
     * The request body is validated using Jakarta Bean Validation and then
     * delegated to the project member service for processing.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the user and project
     * exist, checking for an existing membership, assigning the requested
     * project role, and enforcing project-level authorization.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 201 Created} with the newly
     * created project membership information.
     * </p>
     *
     * @param createProjectMemberRequest request containing the user identifier,
     *                                   project identifier, and project role
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the created {@link GetProjectMemberResponse}
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException
     *         if the specified user does not exist
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if the specified project does not exist
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberAlreadyExistException
     *         if the user is already a member of the project
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to add a project member
     *
     * @apiNote
     * Endpoint: {@code POST /api/project-member/create}
     */
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


    /**
     * Retrieves a project membership by its identifier.
     *
     * <p>
     * The membership identifier is converted into a request object and
     * delegated to the project member service.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the project
     * membership exists and enforcing the appropriate project-level
     * authorization.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with the requested
     * project membership information.
     * </p>
     *
     * @param id project membership identifier
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the requested {@link GetProjectMemberResponse}
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException
     *         if the project membership does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to view the
     *         project membership
     *
     * @apiNote
     * Endpoint: {@code GET /api/project-member/id?id={id}}
     */
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

    /**
     * Retrieves the members belonging to a project using pagination.
     *
     * <p>
     * The project identifier is converted into a request object and delegated
     * to the project member service along with the requested page and page size.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the project exists,
     * retrieving its memberships, applying pagination, and enforcing
     * project-level authorization.
     * </p>
     *
     * <p>
     * The page number is zero-based. The default page number is {@code 0}
     * and the default page size is {@code 10}.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with a paginated list
     * of project memberships.
     * </p>
     *
     * @param id project identifier
     * @param page zero-based page number
     * @param size number of project memberships to return per page
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the paginated {@link GetProjectMemberResponse} results
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if the project does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to view the
     *         project's members
     *
     * @apiNote
     * Endpoint: {@code GET /api/project-member/project?id={id}&page={page}&size={size}}
     */
    @GetMapping("/project")
    public ResponseEntity<ApiResponse<PageResponse<GetProjectMemberResponse>>>
    getMembersByProject(
            @RequestParam UUID id,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") int size
    ) {
        GetByIdRequest request = new GetByIdRequest(id);

        PageResponse<GetProjectMemberResponse> responseData =
                projectMemberService.getMembersByProject(
                        request,
                        page,
                        size
                );

        ApiResponse<PageResponse<GetProjectMemberResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Project members fetched",
                        responseData
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the project memberships associated with a user using pagination.
     *
     * <p>
     * The user identifier is converted into a request object and delegated
     * to the project member service along with the requested page and page size.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the user exists,
     * retrieving the user's project memberships, applying pagination, and
     * enforcing user-level authorization.
     * </p>
     *
     * <p>
     * The page number is zero-based. The default page number is {@code 0}
     * and the default page size is {@code 10}.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with a paginated list
     * of the user's project memberships.
     * </p>
     *
     * @param id user identifier
     * @param page zero-based page number
     * @param size number of project memberships to return per page
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the paginated {@link GetProjectMemberResponse} results
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException
     *         if the specified user does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to view the
     *         requested user's project memberships
     *
     * @apiNote
     * Endpoint: {@code GET /api/project-member/member?id={id}&page={page}&size={size}}
     */
    @GetMapping("/member")
    public ResponseEntity<ApiResponse<PageResponse<GetProjectMemberResponse>>>
    getProjectsByUser(
            @RequestParam UUID id,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") int size
    ) {
        GetByIdRequest request = new GetByIdRequest(id);

        PageResponse<GetProjectMemberResponse> responseData =
                projectMemberService.getProjectsByUser(
                        request,
                        page,
                        size
                );

        ApiResponse<PageResponse<GetProjectMemberResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Project memberships fetched",
                        responseData
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the role assigned to an existing project member.
     *
     * <p>
     * The request body is validated using Jakarta Bean Validation and then
     * delegated to the project member service for processing.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the project
     * membership exists, applying the new project role, and enforcing
     * project-level authorization.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with the updated
     * project membership information.
     * </p>
     *
     * @param updateProjectMemberRoleRequest request containing the project
     *                                       membership identifier and new role
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the updated {@link GetProjectMemberResponse}
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException
     *         if the project membership does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to update the
     *         project member role
     *
     * @apiNote
     * Endpoint: {@code PUT /api/project-member/update-role}
     */
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

    /**
     * Removes a user from a project.
     *
     * <p>
     * The request body is validated using Jakarta Bean Validation and then
     * delegated to the project member service for processing.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying that the project
     * membership exists, removing the membership, and enforcing
     * project-level authorization.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} indicating that the
     * project membership was successfully removed.
     * </p>
     *
     * @param deleteProjectMemberRequest request containing the project
     *                                   membership identifier
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         a boolean indicating whether the membership was removed
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException
     *         if the project membership does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to remove the
     *         project member
     *
     * @apiNote
     * Endpoint: {@code DELETE /api/project-member/remove}
     */
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
