package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.project.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import com.Santhosh1802.ProjectManagement.projects.event.ProjectCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing project-related operations.
 *
 * <p>
 * Provides functionality for creating, retrieving, updating, deleting,
 * searching, filtering, sorting, and paginating projects.
 * </p>
 *
 * <p>
 * Project ownership and access control are enforced through Spring Security
 * method-level authorization where required. Administrative users have
 * broader access, while project owners are authorized to manage their
 * own projects.
 * </p>
 *
 * <p>
 * Read operations use a read-only transaction by default through the
 * class-level {@link Transactional} configuration. Methods that modify
 * persistent data explicitly override this behavior with a read-write
 * transaction.
 * </p>
 */

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            @Autowired(required = false) ApplicationEventPublisher eventPublisher
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Converts a {@link Project} entity into a {@link GetProjectResponse} DTO.
     *
     * <p>
     * The method maps the project's identifying information, description,
     * status, dates, ownership information, and audit timestamps into the
     * response object returned by the API.
     * </p>
     *
     * <p>
     * The project owner is represented using {@link ProjectOwnerResponse}
     * rather than exposing the complete {@link User} entity.
     * </p>
     *
     * @param project project entity to convert
     * @return {@link GetProjectResponse} containing the project's response data
     */
    private GetProjectResponse returnProjectResponse(Project project) {
        return new GetProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectStatus(),
                project.getStartDate(),
                project.getDueDate(),
                new ProjectOwnerResponse(project.getOwner().getId(),
                        project.getOwner().getFirstName(),
                        project.getOwner().getLastName(),
                        project.getOwner().getEmail()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Creates the pagination and sorting configuration used when searching
     * projects.
     *
     * <p>
     * Only supported project fields can be used for sorting. If an unsupported
     * sort field is supplied, {@code createdAt} is used as the default.
     * </p>
     *
     * <p>
     * The requested sort order is converted into a Spring Data
     * {@link Sort.Direction}. The value {@code asc} produces ascending order;
     * any other value results in descending order.
     * </p>
     *
     * @param request project search request containing pagination and sorting
     *                parameters
     * @return configured {@link Pageable} instance
     */
    private Pageable createProjectPageable(
            GetProjectSearchRequest request
    ) {

        String sortField = switch (request.getSortBy()) {

            case "name" -> "name";

            case "startDate" -> "startDate";

            case "dueDate" -> "dueDate";

            case "updatedAt" -> "updatedAt";

            default -> "createdAt";
        };

        Sort.Direction direction =
                "asc".equalsIgnoreCase(request.getSortOrder())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, sortField)
        );
    }

    /**
     * Normalizes a project search keyword before it is passed to the repository.
     *
     * <p>
     * Leading and trailing whitespace is removed from the supplied keyword.
     * If the keyword is {@code null} or becomes empty after trimming,
     * {@code null} is returned so that the repository can treat the search
     * as having no keyword filter.
     * </p>
     *
     * @param keyword raw project search keyword
     * @return normalized keyword, or {@code null} when no keyword is supplied
     */
    private String normalizeKeyword(String keyword) {

        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    /**
     * Creates a new project for the currently authenticated user.
     *
     * <p>
     * The authenticated user's email is obtained from the Spring Security
     * {@link Authentication} object and used to identify the project owner.
     * The owner is therefore determined by the authenticated session rather
     * than being supplied by the client request.
     * </p>
     *
     * <p>
     * Before creating the project, the method verifies that the authenticated
     * user exists, checks that the project name is not already in use, and
     * validates that the due date is not earlier than the start date.
     * </p>
     *
     * <p>
     * Newly created projects are assigned the
     * {@link ProjectStatus#PLANNING} status by default.
     * </p>
     *
     * @param request        project creation details
     * @param authentication currently authenticated Spring Security user
     * @return {@link GetProjectResponse} containing the newly created project
     * @throws UserNotFoundException        if the authenticated user cannot be found
     * @throws ProjectAlreadyExistException if another project already uses the supplied project name
     * @throws IllegalArgumentException     if the project due date is earlier than the project start date
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Transactional
    public GetProjectResponse createProject(
            CreateProjectRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );

        boolean projectExists =
                projectRepository.findProjectByName(request.getName()).isPresent();

        if (projectExists) {
            throw new ProjectAlreadyExistException(
                    "Project with name '" + request.getName() + "' already exists"
            );
        }

        if (request.getDueDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                    "Project due date cannot be before start date"
            );
        }

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setProjectStatus(ProjectStatus.PLANNING);
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);

        if (eventPublisher != null) {
            eventPublisher.publishEvent(ProjectCreatedEvent.of(
                    savedProject.getId(),
                    savedProject.getName(),
                    owner.getId(),
                    savedProject.getProjectStatus(),
                    savedProject.getStartDate(),
                    savedProject.getDueDate()
            ));
        }

        return returnProjectResponse(savedProject);
    }

    /**
     * Retrieves a project using its unique identifier.
     *
     * <p>
     * The method searches the project repository using the supplied project ID
     * and converts the resulting entity into a {@link GetProjectResponse}.
     * </p>
     *
     * @param getProjectByIdRequest request containing the project identifier
     * @return {@link GetProjectResponse} containing the requested project
     * @throws ProjectNotFoundException if no project exists with the supplied identifier
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectAuthorization.canView(#getProjectByIdRequest.id, authentication)"
    )
    @Transactional(readOnly = true)
    public GetProjectResponse getProjectById(@P("getProjectByIdRequest") GetProjectByIdRequest getProjectByIdRequest) {
        Project project = projectRepository.findById(getProjectByIdRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project id " + getProjectByIdRequest.getId() + " not found")
        );
        return returnProjectResponse(project);
    }


    /**
     * Updates the details of an existing project.
     *
     * <p>
     * The method retrieves the project using its unique identifier and updates
     * its name, description, status, and due date.
     * </p>
     *
     * <p>
     * When the project name is changed, the method checks whether another
     * project already uses the requested name before applying the update.
     * </p>
     *
     * <p>
     * Access is restricted to administrators or the current project owner
     * through Spring Security method-level authorization.
     * </p>
     *
     * @param updateProjectDetailsRequest request containing the project
     *                                    identifier and updated project details
     * @return {@link GetProjectResponse} containing the updated project
     * @throws ProjectNotFoundException                                  if no project exists with the supplied identifier
     * @throws ProjectAlreadyExistException                              if the requested project name is already used by another project
     * @throws org.springframework.security.access.AccessDeniedException if the authenticated user is neither an administrator nor
     *                                                                   the project owner
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectAuthorization.isOwner(#updateProjectDetailsRequest.id, authentication)"
    )
    @Transactional
    public GetProjectResponse updateProjectDetails(@P("updateProjectDetailsRequest") UpdateProjectDetailsRequest updateProjectDetailsRequest) {
        Project project = projectRepository.findById(updateProjectDetailsRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project id " + updateProjectDetailsRequest.getId() + " not found")
        );
        if (updateProjectDetailsRequest.getDueDate()
                .isBefore(project.getStartDate())) {
            throw new IllegalArgumentException(
                    "Project due date cannot be before project start date"
            );
        }
        if (!project.getName().equals(updateProjectDetailsRequest.getName())) {
            boolean duplicateProjectName = projectRepository.findProjectByName(updateProjectDetailsRequest.getName()).isPresent();
            if (duplicateProjectName) {
                throw new ProjectAlreadyExistException("Project with name " + updateProjectDetailsRequest.getName() + " already exists");
            } else {
                project.setName(updateProjectDetailsRequest.getName());
                project.setDescription(updateProjectDetailsRequest.getDescription());
                project.setProjectStatus(updateProjectDetailsRequest.getProjectStatus());
                project.setDueDate(updateProjectDetailsRequest.getDueDate());
                projectRepository.save(project);
                return returnProjectResponse(project);
            }

        } else {
            project.setName(updateProjectDetailsRequest.getName());
            project.setDescription(updateProjectDetailsRequest.getDescription());
            project.setProjectStatus(updateProjectDetailsRequest.getProjectStatus());
            project.setDueDate(updateProjectDetailsRequest.getDueDate());
            projectRepository.save(project);
            return returnProjectResponse(project);
        }

    }

    /**
     * Transfers project ownership to another user.
     *
     * <p>
     * The method retrieves the project using its identifier and verifies that
     * the requested new owner exists before assigning the project to that user.
     * </p>
     *
     * <p>
     * Access is restricted to administrators or the current project owner
     * through Spring Security method-level authorization.
     * </p>
     *
     * @param updateProjectOwnerRequest request containing the project identifier
     *                                  and the new owner's user identifier
     * @return {@link GetProjectResponse} containing the updated project
     * @throws ProjectNotFoundException                                  if the project does not exist
     * @throws UserNotFoundException                                     if the requested new owner does not exist
     * @throws org.springframework.security.access.AccessDeniedException if the authenticated user is neither an administrator nor
     *                                                                   the current project owner
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectAuthorization.isOwner(#updateProjectOwnerRequest.id, authentication)"
    )
    @Transactional
    public GetProjectResponse updateProjectOwner(@P("updateProjectOwnerRequest") UpdateProjectOwnerRequest updateProjectOwnerRequest) {
        Project project = projectRepository.findById(updateProjectOwnerRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project id " + updateProjectOwnerRequest.getId() + " not found")
        );
        User owner = userRepository.findById(updateProjectOwnerRequest.getOwnerId()).orElseThrow(() ->
                new UserNotFoundException("User with id " + updateProjectOwnerRequest.getOwnerId() + " not found")
        );
        project.setOwner(owner);
        projectRepository.save(project);
        return returnProjectResponse(project);
    }

    /**
     * Deletes an existing project.
     *
     * <p>
     * The method first verifies that the requested project exists and then
     * permanently removes it from the project repository.
     * </p>
     *
     * <p>
     * Access is restricted to administrators or the current project owner
     * through Spring Security method-level authorization.
     * </p>
     *
     * @param deleteProjectRequest request containing the identifier of the
     *                             project to delete
     * @return {@code true} when the project is successfully deleted
     * @throws ProjectNotFoundException                                  if no project exists with the supplied identifier
     * @throws org.springframework.security.access.AccessDeniedException if the authenticated user is neither an administrator nor
     *                                                                   the project owner
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectAuthorization.isOwner(#deleteProjectRequest.id, authentication)"
    )
    @Transactional
    public boolean deleteProject(@P("deleteProjectRequest") DeleteProjectRequest deleteProjectRequest) {
        projectRepository.findById(deleteProjectRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project id " + deleteProjectRequest.getId() + " not found")
        );

        projectRepository.deleteById(deleteProjectRequest.getId());
        return true;
    }

    /**
     * Searches, filters, sorts, and paginates projects accessible to the
     * authenticated user.
     *
     * <p>
     * The search supports keyword filtering, project status filtering,
     * ownership filtering, pagination, and configurable sorting.
     * </p>
     *
     * <p>
     * Administrators can search projects globally and may optionally filter
     * the results by a specific owner.
     * </p>
     *
     * <p>
     * Non-administrative users are prevented from requesting another user's
     * projects through the {@code ownerId} filter. Their results are determined
     * by the requested project scope.
     * </p>
     *
     * <p>
     * The {@code OWNED} scope returns projects owned by the authenticated user.
     * The {@code SHARED} scope retrieves projects returned by the shared-project
     * repository query.
     * </p>
     *
     * <p>
     * At the current stage, the {@code ALL} scope represents projects owned by
     * the authenticated user. Once project membership is implemented, this
     * scope should be changed to represent all projects accessible to the
     * authenticated user.
     * </p>
     *
     * @param request        project search, filtering, sorting, and pagination parameters
     * @param authentication currently authenticated Spring Security user
     * @return {@link PageResponse} containing the paginated project results
     * @throws UserNotFoundException    if the authenticated user cannot be found
     * @throws AccessDeniedException    if a non-administrative user attempts to search another user's
     *                                  projects
     * @throws IllegalArgumentException if an unsupported project scope is supplied
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PageResponse<GetProjectResponse> searchProjects(
            GetProjectSearchRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );

        Pageable pageable = createProjectPageable(request);

        String keyword = normalizeKeyword(request.getKeyword());

        Page<Project> projectPage;

        boolean isAdmin =
                currentUser.getUserRole() == UserRole.ADMIN;

        if (isAdmin) {

            projectPage = projectRepository.searchAllProjects(
                    keyword,
                    request.getStatus(),
                    request.getOwnerId(),
                    pageable
            );

        } else {

            if (request.getOwnerId() != null &&
                    !request.getOwnerId().equals(currentUser.getId())) {

                throw new AccessDeniedException(
                        "You are not allowed to search another user's projects"
                );
            }

            switch (request.getScope()) {

                case OWNED -> projectPage =
                        projectRepository.searchOwnedProjects(
                                currentUser.getId(),
                                keyword,
                                request.getStatus(),
                                pageable
                        );

                case SHARED -> projectPage =
                        projectRepository.searchSharedProjects(
                                currentUser.getId(),
                                keyword,
                                request.getStatus(),
                                pageable
                        );

                case ALL -> // For now this represents projects owned by the user.
                    // Once ProjectMember is implemented,
                    // this should become "all accessible projects".
                        projectPage =
                                projectRepository.searchOwnedProjects(
                                        currentUser.getId(),
                                        keyword,
                                        request.getStatus(),
                                        pageable
                                );

                default -> throw new IllegalArgumentException(
                        "Invalid project scope"
                );
            }
        }

        List<GetProjectResponse> content =
                projectPage.getContent()
                        .stream()
                        .map(this::returnProjectResponse)
                        .toList();

        return new PageResponse<>(
                content,
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.isFirst(),
                projectPage.isLast()
        );
    }
}
