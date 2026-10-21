package com.Santhosh1802.ProjectManagement.authorization;

import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("projectAuthorization")
public class ProjectAuthorization {

    private final ProjectRepository projectRepository;
    private final com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository projectMemberRepository;

    public ProjectAuthorization(
            ProjectRepository projectRepository,
            com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository projectMemberRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public boolean isOwner(
            UUID projectId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        return projectRepository.findById(projectId)
                .map(project ->
                        project.getOwner() != null &&
                                project.getOwner()
                                        .getEmail()
                                        .equals(authentication.getName())
                )
                .orElse(false);
    }

    public boolean canView(
            UUID projectId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        // ADMIN is handled directly by @PreAuthorize.
        // Project owner can view
        if (isOwner(projectId, authentication)) {
            return true;
        }

        // Project members can view
        return projectMemberRepository.existsByProjectIdAndUserEmail(
                projectId,
                authentication.getName()
        );
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated();
    }
}