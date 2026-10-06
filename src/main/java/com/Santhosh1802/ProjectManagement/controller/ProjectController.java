package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.project.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller responsible for project-related operations.
 *
 * <p>
 * Provides endpoints for creating projects, retrieving projects,
 * searching and filtering projects, updating project details and
 * ownership, and deleting projects.
 * </p>
 *
 * <p>
 * Project access and authorization are delegated to the service layer,
 * where Spring Security method-level authorization is applied.
 * </p>
 *
 * <p>
 * Base endpoint: {@code /api/projects}
 * </p>
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates a new project for the currently authenticated user.
     *
     * <p>
     * The project creation request is validated using Jakarta Bean Validation
     * before being passed to the service layer. The authenticated user is
     * provided through Spring Security and is used by the service to determine
     * the project owner.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 201 Created} together with
     * the newly created project.
     * </p>
     *
     * @param request project creation details
     * @param authentication currently authenticated Spring Security user
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the created {@link GetProjectResponse}
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException
     *         if the authenticated user cannot be found
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException
     *         if a project with the supplied name already exists
     *
     * @throws IllegalArgumentException
     *         if the project due date is earlier than the start date
     *
     * @apiNote
     * Endpoint: {@code POST /api/projects}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GetProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {

        GetProjectResponse project =
                projectService.createProject(request, authentication);

        ApiResponse<GetProjectResponse> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value(),
                        "Project created successfully",
                        project
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves a project using its unique identifier.
     *
     * <p>
     * The project identifier is received as a request parameter and converted
     * into a {@link GetProjectByIdRequest} before being passed to the service
     * layer.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with the requested
     * project information.
     * </p>
     *
     * @param id unique identifier of the project
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the requested {@link GetProjectResponse}
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if no project exists with the supplied identifier
     *
     * @throws org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
     *         if the supplied project identifier cannot be converted to
     *         {@link UUID}
     *
     * @apiNote
     * Endpoint: {@code GET /api/projects/id?id={id}}
     */
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

    /**
     * Searches, filters, sorts, and paginates projects.
     *
     * <p>
     * Search parameters are received through the request query parameters
     * and bound to {@link GetProjectSearchRequest}. The request supports
     * project filtering, sorting, and pagination.
     * </p>
     *
     * <p>
     * The authenticated user is passed to the service layer so that the
     * service can determine which projects the user is authorized to access.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} together with a
     * paginated {@link PageResponse} containing the matching projects.
     * </p>
     *
     * @param request project search, filtering, sorting, and pagination parameters
     * @param authentication currently authenticated Spring Security user
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         paginated project results
     *
     * @throws org.springframework.validation.BindException
     *         if request parameters fail Bean Validation
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException
     *         if the authenticated user cannot be found
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user attempts to access projects they are
     *         not authorized to access
     *
     * @apiNote
     * Endpoint: {@code GET /api/projects}
     *
     * Example:
     * {@code GET /api/projects?page=0&size=10&keyword=management}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GetProjectResponse>>>
    getProjects(
            @Valid @ModelAttribute GetProjectSearchRequest request,
            Authentication authentication
    ) {

        PageResponse<GetProjectResponse> projects =
                projectService.searchProjects(
                        request,
                        authentication
                );

        ApiResponse<PageResponse<GetProjectResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Projects fetched successfully",
                        projects
                );

        return ResponseEntity.ok(response);
    }


    /**
     * Updates the details of an existing project.
     *
     * <p>
     * The request body is validated using Jakarta Bean Validation and then
     * delegated to the project service for processing.
     * </p>
     *
     * <p>
     * The service layer is responsible for verifying project existence,
     * checking project name uniqueness, applying the requested changes,
     * and enforcing project-level authorization.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with the updated
     * project information.
     * </p>
     *
     * @param updateProjectDetailsRequest request containing the project
     *                                    identifier and updated project details
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the updated {@link GetProjectResponse}
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if the project does not exist
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException
     *         if the requested project name is already used by another project
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to update the project
     *
     * @apiNote
     * Endpoint: {@code PUT /api/projects/update/details}
     */
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
    /**
     * Updates the owner of an existing project.
     *
     * <p>
     * The request body contains the project identifier and the identifier
     * of the user who should become the new project owner.
     * </p>
     *
     * <p>
     * The service layer verifies that both the project and requested owner
     * exist before performing the ownership change. Project-level
     * authorization is also enforced by the service layer.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} with the updated
     * project information.
     * </p>
     *
     * @param updateProjectOwnerRequest request containing the project
     *                                  identifier and new owner identifier
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the updated {@link GetProjectResponse}
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if the project does not exist
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException
     *         if the requested new owner does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to transfer
     *         project ownership
     *
     * @apiNote
     * Endpoint: {@code PUT /api/projects/update/owner}
     */
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

    /**
     * Deletes an existing project.
     *
     * <p>
     * The request contains the identifier of the project to be deleted.
     * The service layer verifies that the project exists and that the
     * authenticated user is authorized to delete it before performing
     * the deletion.
     * </p>
     *
     * <p>
     * A successful request returns HTTP {@code 200 OK} together with a
     * boolean value indicating that the deletion was completed.
     * </p>
     *
     * @param deleteProjectRequest request containing the project identifier
     *                             to delete
     * @return {@link ResponseEntity} containing an {@link ApiResponse}
     *         with {@code true} when the project is successfully deleted
     *
     * @throws jakarta.validation.ConstraintViolationException
     *         if validation constraints are violated
     *
     * @throws com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException
     *         if the project does not exist
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the authenticated user is not authorized to delete the project
     *
     * @apiNote
     * Endpoint: {@code DELETE /api/projects/delete}
     */
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
