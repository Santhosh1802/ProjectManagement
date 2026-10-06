package com.Santhosh1802.ProjectManagement.authorization;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("projectMemberAuthorization")
public class ProjectMemberAuthorization {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    public ProjectMemberAuthorization(
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Determines whether the authenticated user can view
     * members belonging to the specified project.
     *
     * <p>
     * A project owner or any member of the project can view
     * its project-member records.
     * </p>
     *
     * @param projectId project identifier
     * @param authentication current authentication
     * @return {@code true} when the user is the project owner
     *         or a member of the project
     */
    public boolean canViewProject(
            UUID projectId,
            Authentication authentication
    ) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        String email = authentication.getName();

        return projectRepository.findById(projectId)
                .map(Project::getOwner)
                .map(owner -> owner.getEmail().equals(email))
                .orElse(false)
                ||
                projectMemberRepository.existsByProjectIdAndUserEmail(
                        projectId,
                        email
                );
    }

    /**
     * Determines whether the authenticated user can manage
     * members of a project.
     *
     * <p>
     * Project owners and project managers can manage project members.
     * </p>
     *
     * @param projectId project identifier
     * @param authentication current authentication
     * @return {@code true} when the user can manage project members
     */
    public boolean canManageProject(
            UUID projectId,
            Authentication authentication
    ) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        String email = authentication.getName();

        boolean owner = projectRepository.findById(projectId)
                .map(Project::getOwner)
                .map(projectOwner ->
                        projectOwner.getEmail().equals(email)
                )
                .orElse(false);

        if (owner) {
            return true;
        }

        return projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        email,
                        ProjectRole.MANAGER
                );
    }

    /**
     * Determines whether the authenticated user can view
     * a specific project-member record.
     *
     * <p>
     * Access is granted when the authenticated user can view
     * the project associated with the project-member record.
     * </p>
     *
     * @param projectMemberId project-member identifier
     * @param authentication current authentication
     * @return {@code true} when the user can view the associated project
     */
    public boolean canViewMember(
            UUID projectMemberId,
            Authentication authentication
    ) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        return projectMemberRepository.findById(projectMemberId)
                .map(ProjectMember::getProject)
                .map(project ->
                        canViewProject(
                                project.getId(),
                                authentication
                        )
                )
                .orElse(false);
    }

    /**
     * Determines whether the authenticated user can manage
     * a specific project-member record.
     *
     * <p>
     * The user must have project management permission for the
     * project associated with the project-member record.
     * </p>
     *
     * @param projectMemberId project-member identifier
     * @param projectRole role being assigned
     * @param authentication current authentication
     * @return {@code true} when the user can manage the project member
     */
    public boolean canManageProjectMember(
            UUID projectMemberId,
            ProjectRole projectRole,
            Authentication authentication
    ) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        return projectMemberRepository.findById(projectMemberId)
                .map(ProjectMember::getProject)
                .map(project ->
                        canManageProject(
                                project.getId(),
                                authentication
                        )
                )
                .orElse(false);
    }

    /**
     * Determines whether the authenticated user can remove
     * a specific project-member record.
     *
     * <p>
     * The user must have project management permission for the
     * project associated with the project-member record.
     * </p>
     *
     * @param projectMemberId project-member identifier
     * @param authentication current authentication
     * @return {@code true} when the user can remove the project member
     */
    public boolean canManageMember(
            UUID projectMemberId,
            Authentication authentication
    ) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        return projectMemberRepository.findById(projectMemberId)
                .map(ProjectMember::getProject)
                .map(project ->
                        canManageProject(
                                project.getId(),
                                authentication
                        )
                )
                .orElse(false);
    }

    /**
     * Determines whether the supplied authentication represents
     * an authenticated user.
     *
     * @param authentication current authentication
     * @return {@code true} when authentication exists and is authenticated
     */
    private boolean isAuthenticated(
            Authentication authentication
    ) {
        return authentication != null
                && authentication.isAuthenticated();
    }
}