package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.context.ApplicationEventPublisher;
import com.Santhosh1802.ProjectManagement.projects.event.ProjectMemberAddedEvent;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectMemberService(
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            @Autowired(required = false) ApplicationEventPublisher eventPublisher
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Converts a {@link Page} of {@link ProjectMember} entities into a
     * {@link PageResponse} containing {@link GetProjectMemberResponse} objects.
     *
     * <p>
     * Each {@link ProjectMember} entity is mapped to its corresponding response
     * DTO using {@link #returnGetProjectMemberResponse(ProjectMember)}.
     * Pagination metadata such as the current page, page size, total pages,
     * total elements, first-page status, and last-page status is also included.
     * </p>
     *
     * @param page the paginated project member entities to convert
     * @return a {@link PageResponse} containing project member response DTOs
     */
    private PageResponse<GetProjectMemberResponse> createPageResponse(
            Page<ProjectMember> page
    ) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::returnGetProjectMemberResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    /**
     * Converts a {@link ProjectMember} entity into a
     * {@link GetProjectMemberResponse} DTO.
     *
     * <p>
     * The response contains the project member ID, complete user information,
     * project information including the project owner, the member's project role,
     * and the date on which the member joined the project.
     * </p>
     *
     * @param projectMember the project member entity to convert
     * @return the corresponding {@link GetProjectMemberResponse} DTO
     */
    private GetProjectMemberResponse returnGetProjectMemberResponse(ProjectMember projectMember) {
        return new GetProjectMemberResponse(
                projectMember.getId(),
                new GetUserResponse(
                        projectMember.getUser().getId(),
                        projectMember.getUser().getFirstName(),
                        projectMember.getUser().getLastName(),
                        projectMember.getUser().getEmail(),
                        projectMember.getUser().getUserRole(),
                        projectMember.getUser().getIsActive(),
                        projectMember.getUser().getEmailVerified(),
                        projectMember.getUser().getCreatedAt(),
                        projectMember.getUser().getUpdatedAt(),
                        projectMember.getUser().getLastLoginAt()
                ),
                new GetProjectResponse(
                        projectMember.getProject().getId(),
                        projectMember.getProject().getName(),
                        projectMember.getProject().getDescription(),
                        projectMember.getProject().getProjectStatus(),
                        projectMember.getProject().getStartDate(),
                        projectMember.getProject().getDueDate(),
                        new ProjectOwnerResponse(projectMember.getProject().getOwner().getId(),
                                projectMember.getProject().getOwner().getFirstName(),
                                projectMember.getProject().getOwner().getLastName(),
                                projectMember.getProject().getOwner().getEmail()),
                        projectMember.getProject().getCreatedAt(),
                        projectMember.getProject().getUpdatedAt()
                ),
                projectMember.getProjectRole(),
                projectMember.getJoinedAt()
        );
    }

    /**
     * Adds a user as a member of a project.
     *
     * <p>
     * The method verifies that the requested user and project exist before
     * creating the project membership. It also prevents the same user from
     * being added to the same project more than once.
     * </p>
     *
     * <p>
     * Access is restricted to administrators, project owners, and project
     * managers through method-level authorization.
     * </p>
     *
     * @param createProjectMemberRequest request containing the user ID,
     *                                   project ID, and project role
     * @return the newly created project member details
     * @throws UserNotFoundException              if the requested user does not exist
     * @throws ProjectNotFoundException           if the requested project does not exist
     * @throws ProjectMemberAlreadyExistException if the user is already a
     *                                            member of the project
     * @apiNote This method is used by the project member creation endpoint:
     * {@code POST /api/project-member/create}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectMemberAuthorization.canManageProject(" +
                    "#createProjectMemberRequest.projectId, authentication)"
    )
    @Transactional
    public GetProjectMemberResponse addMember(@P("createProjectMemberRequest") CreateProjectMemberRequest createProjectMemberRequest) {
        ProjectMember projectMember = new ProjectMember();
        User user = userRepository.findById(createProjectMemberRequest.getProjectMemberId()).orElseThrow(() ->
                new UserNotFoundException("User of id: " + createProjectMemberRequest.getProjectMemberId() + " not found.")
        );
        Project project = projectRepository.findById(createProjectMemberRequest.getProjectId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id: " + createProjectMemberRequest.getProjectId() + " not found")
        );
        projectMember.setUser(user);
        projectMember.setProject(project);
        boolean memberExist = projectMemberRepository.existsByUserIdAndProjectId(user.getId(), project.getId());
        if (memberExist) {
            throw new ProjectMemberAlreadyExistException("Project Member of user: " + user.getId() + " already exist.");
        }
        projectMember.setProjectRole(createProjectMemberRequest.getProjectRole());
        ProjectMember savedMember = projectMemberRepository.save(projectMember);

        if (eventPublisher != null) {
            eventPublisher.publishEvent(ProjectMemberAddedEvent.of(
                    savedMember.getId(),
                    project.getId(),
                    user.getId(),
                    savedMember.getProjectRole()
            ));
        }

        return returnGetProjectMemberResponse(savedMember);
    }

    /**
     * Retrieves a project membership using its unique ID.
     *
     * <p>
     * The method verifies that the project membership exists and converts the
     * entity into a {@link GetProjectMemberResponse}.
     * </p>
     *
     * <p>
     * Access is restricted to administrators and users who are authorized to
     * view the project associated with the membership.
     * </p>
     *
     * @param getByIdRequest request containing the project membership ID
     * @return the project member details
     * @throws ProjectMemberNotFoundException if the project membership does
     *                                        not exist
     * @apiNote This method is used by:
     * {@code GET /api/project-member/id?id={id}}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectMemberAuthorization.canViewMember(" +
                    "#getByIdRequest.id, authentication)"
    )
    @Transactional(readOnly = true)
    public GetProjectMemberResponse getMemberById(@P("getByIdRequest") GetByIdRequest getByIdRequest) {
        ProjectMember projectMember = projectMemberRepository.findById(getByIdRequest.getId()).orElseThrow(() -> new ProjectMemberNotFoundException("Project Member with id: " + getByIdRequest.getId() + " not found."));
        return returnGetProjectMemberResponse(projectMember);
    }

    /**
     * Retrieves the members belonging to a specific project using pagination.
     *
     * <p>
     * The method first verifies that the requested project exists and then
     * retrieves its project memberships using the supplied page number and
     * page size.
     * </p>
     *
     * <p>
     * The returned result includes both the project member data and pagination
     * metadata such as total elements, total pages, current page, and whether
     * the current page is the first or last page.
     * </p>
     *
     * <p>
     * Access is restricted to administrators and users who are authorized to
     * view the requested project.
     * </p>
     *
     * @param getByIdRequest request containing the project ID
     * @param page           zero-based page number
     * @param size           number of project members to return per page
     * @return paginated project member details
     * @throws ProjectNotFoundException if the requested project does not exist
     * @apiNote This method is used by:
     * {@code GET /api/project-member/project?id={id}&page={page}&size={size}}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectMemberAuthorization.canViewProject(" +
                    "#getByIdRequest.id, authentication)"
    )
    @Transactional(readOnly = true)
    public PageResponse<GetProjectMemberResponse> getMembersByProject(
            @P("getByIdRequest")
            GetByIdRequest getByIdRequest,
            int page,
            int size
    ) {
        projectRepository.findById(getByIdRequest.getId())
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project of id: " +
                                        getByIdRequest.getId() +
                                        " not found."
                        )
                );

        Pageable pageable = PageRequest.of(page, size);

        Page<ProjectMember> projectMemberPage =
                projectMemberRepository.findByProjectId(
                        getByIdRequest.getId(),
                        pageable
                );

        return createPageResponse(projectMemberPage);
    }

    /**
     * Retrieves the projects in which a specific user is a member using
     * pagination.
     *
     * <p>
     * The method first verifies that the requested user exists and then
     * retrieves all project memberships associated with that user.
     * </p>
     *
     * <p>
     * Access is restricted to administrators or the user whose memberships
     * are being requested. This prevents users from retrieving another user's
     * project membership information.
     * </p>
     *
     * <p>
     * The result contains pagination metadata along with the project membership
     * details.
     * </p>
     *
     * @param getByIdRequest request containing the user ID
     * @param page           zero-based page number
     * @param size           number of project memberships to return per page
     * @return paginated project membership details for the requested user
     * @throws UserNotFoundException if the requested user does not exist
     * @apiNote This method is used by:
     * {@code GET /api/project-member/member?id={id}&page={page}&size={size}}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@userAuthorization.isCurrentUser(" +
                    "#getByIdRequest.id, authentication)"
    )
    @Transactional(readOnly = true)
    public PageResponse<GetProjectMemberResponse> getProjectsByUser(
            @P("getByIdRequest")
            GetByIdRequest getByIdRequest,
            int page,
            int size
    ) {
        User user = userRepository.findById(getByIdRequest.getId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id: " +
                                        getByIdRequest.getId() +
                                        " not found."
                        )
                );

        Pageable pageable = PageRequest.of(page, size);

        Page<ProjectMember> projectMemberPage =
                projectMemberRepository.findByUserId(
                        user.getId(),
                        pageable
                );

        return createPageResponse(projectMemberPage);
    }

    /**
     * Updates the project role of an existing project member.
     *
     * <p>
     * The method verifies that the project membership exists, updates its
     * project role, and persists the modified membership.
     * </p>
     *
     * <p>
     * Access is restricted to administrators and users who have permission
     * to manage the specified project membership.
     * </p>
     *
     * @param updateProjectMemberRoleRequest request containing the project
     *                                       membership ID and new project role
     * @return the updated project member details
     * @throws ProjectMemberNotFoundException if the requested project
     *                                        membership does not exist
     * @apiNote This method is used by:
     * {@code PUT /api/project-member/update-role}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectMemberAuthorization.canManageProjectMember(" +
                    "#updateProjectMemberRoleRequest.id, " +
                    "#updateProjectMemberRoleRequest.projectRole, " +
                    "authentication)"
    )
    @Transactional
    public GetProjectMemberResponse updateMemberRole(@P("updateProjectMemberRoleRequest") UpdateProjectMemberRoleRequest updateProjectMemberRoleRequest) {
        ProjectMember projectMember = projectMemberRepository.findById(updateProjectMemberRoleRequest.getId()).orElseThrow(() -> new ProjectMemberNotFoundException("Project member with id: " + updateProjectMemberRoleRequest.getId() + " not found."));
        projectMember.setProjectRole(updateProjectMemberRoleRequest.getProjectRole());
        projectMemberRepository.save(projectMember);
        return returnGetProjectMemberResponse(projectMember);
    }

    /**
     * Removes an existing member from a project.
     *
     * <p>
     * The method verifies that the requested project membership exists and
     * permanently removes it from the project.
     * </p>
     *
     * <p>
     * Access is restricted to administrators and users who have permission
     * to manage the specified project membership.
     * </p>
     *
     * @param deleteProjectMemberRequest request containing the project
     *                                   membership ID to remove
     * @return {@code true} when the project membership is successfully removed
     * @throws ProjectMemberNotFoundException if the requested project
     *                                        membership does not exist
     * @apiNote This method is used by:
     * {@code DELETE /api/project-member/remove}
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@projectMemberAuthorization.canManageMember(" +
                    "#deleteProjectMemberRequest.id, authentication)"
    )
    @Transactional
    public boolean removeMember(@P("deleteProjectMemberRequest") DeleteProjectMemberRequest deleteProjectMemberRequest) {
        projectMemberRepository.delete(projectMemberRepository.findById(deleteProjectMemberRequest.getId()).orElseThrow(() -> new ProjectMemberNotFoundException("Project Member of id: " + deleteProjectMemberRequest.getId() + " not found.")));
        return true;
    }
}
