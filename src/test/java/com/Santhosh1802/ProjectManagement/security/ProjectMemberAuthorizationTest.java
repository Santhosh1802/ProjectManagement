package com.Santhosh1802.ProjectManagement.security;


import com.Santhosh1802.ProjectManagement.authorization.ProjectMemberAuthorization;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberAuthorizationTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    private ProjectMemberAuthorization authorization;

    private UUID projectId;
    private UUID memberId;

    private User owner;
    private User manager;
    private User member;

    private Authentication ownerAuth;
    private Authentication managerAuth;
    private Authentication memberAuth;

    @BeforeEach
    void setUp() {
        authorization = new ProjectMemberAuthorization(
                projectMemberRepository,
                projectRepository
        );

        projectId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        owner = new User();
        owner.setEmail("owner@example.com");

        manager = new User();
        manager.setEmail("manager@example.com");

        member = new User();
        member.setEmail("member@example.com");

        ownerAuth = authentication("owner@example.com");
        managerAuth = authentication("manager@example.com");
        memberAuth = authentication("member@example.com");
    }

    private Authentication authentication(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                java.util.Collections.emptyList()
        );
    }

    // ---------------------------------------------------------
    // canViewProject()
    // ---------------------------------------------------------

    @Test
    void canViewProject_shouldReturnTrue_whenUserIsProjectOwner() {

        Project project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result = authorization.canViewProject(
                projectId,
                ownerAuth
        );

        assertTrue(result);
    }

    @Test
    void canViewProject_shouldReturnTrue_whenUserIsProjectMember() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        "member@example.com"
                ))
                .thenReturn(true);

        boolean result = authorization.canViewProject(
                projectId,
                memberAuth
        );

        assertTrue(result);
    }

    @Test
    void canViewProject_shouldReturnFalse_whenUserIsNotMember() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        "member@example.com"
                ))
                .thenReturn(false);

        boolean result = authorization.canViewProject(
                projectId,
                memberAuth
        );

        assertFalse(result);
    }

    @Test
    void canViewProject_shouldReturnFalse_whenUnauthenticated() {

        boolean result = authorization.canViewProject(
                projectId,
                null
        );

        assertFalse(result);

        verifyNoInteractions(
                projectRepository,
                projectMemberRepository
        );
    }

    // ---------------------------------------------------------
    // canManageProject()
    // ---------------------------------------------------------

    @Test
    void canManageProject_shouldReturnTrue_whenUserIsOwner() {

        Project project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result = authorization.canManageProject(
                projectId,
                ownerAuth
        );

        assertTrue(result);
    }

    @Test
    void canManageProject_shouldReturnTrue_whenUserIsManager() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        "manager@example.com",
                        ProjectRole.MANAGER
                ))
                .thenReturn(true);

        boolean result = authorization.canManageProject(
                projectId,
                managerAuth
        );

        assertTrue(result);
    }

    @Test
    void canManageProject_shouldReturnFalse_whenUserIsOnlyMember() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        "member@example.com",
                        ProjectRole.MANAGER
                ))
                .thenReturn(false);

        boolean result = authorization.canManageProject(
                projectId,
                memberAuth
        );

        assertFalse(result);
    }

    @Test
    void canManageProject_shouldReturnFalse_whenUnauthenticated() {

        boolean result = authorization.canManageProject(
                projectId,
                null
        );

        assertFalse(result);

        verifyNoInteractions(
                projectRepository,
                projectMemberRepository
        );
    }

    // ---------------------------------------------------------
    // canViewMember()
    // ---------------------------------------------------------

    @Test
    void canViewMember_shouldReturnTrue_whenUserCanViewProject() {

        Project project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(memberId);
        projectMember.setProject(project);

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.of(projectMember));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result = authorization.canViewMember(
                memberId,
                ownerAuth
        );

        assertTrue(result);
    }

    @Test
    void canViewMember_shouldReturnFalse_whenMemberDoesNotExist() {

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.empty());

        boolean result = authorization.canViewMember(
                memberId,
                ownerAuth
        );

        assertFalse(result);
    }

    @Test
    void canViewMember_shouldReturnFalse_whenUnauthenticated() {

        boolean result = authorization.canViewMember(
                memberId,
                null
        );

        assertFalse(result);

        verifyNoInteractions(
                projectRepository,
                projectMemberRepository
        );
    }

    // ---------------------------------------------------------
    // canManageProjectMember()
    // ---------------------------------------------------------

    @Test
    void canManageProjectMember_shouldReturnTrue_whenUserCanManageProject() {

        Project project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(memberId);
        projectMember.setProject(project);

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.of(projectMember));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result = authorization.canManageProjectMember(
                memberId,
                ProjectRole.MEMBER,
                ownerAuth
        );

        assertTrue(result);
    }

    @Test
    void canManageProjectMember_shouldReturnFalse_whenUserCannotManageProject() {

        Project project = new Project();
        project.setId(projectId);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(memberId);
        projectMember.setProject(project);

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.of(projectMember));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        "member@example.com",
                        ProjectRole.MANAGER
                ))
                .thenReturn(false);

        boolean result = authorization.canManageProjectMember(
                memberId,
                ProjectRole.MEMBER,
                memberAuth
        );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canManageMember()
    // ---------------------------------------------------------

    @Test
    void canManageMember_shouldReturnTrue_whenUserCanManageProject() {

        Project project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(memberId);
        projectMember.setProject(project);

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.of(projectMember));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result = authorization.canManageMember(
                memberId,
                ownerAuth
        );

        assertTrue(result);
    }

    @Test
    void canManageMember_shouldReturnFalse_whenMemberDoesNotExist() {

        when(projectMemberRepository.findById(memberId))
                .thenReturn(Optional.empty());

        boolean result = authorization.canManageMember(
                memberId,
                ownerAuth
        );

        assertFalse(result);
    }

    @Test
    void canManageMember_shouldReturnFalse_whenUnauthenticated() {

        boolean result = authorization.canManageMember(
                memberId,
                null
        );

        assertFalse(result);

        verifyNoInteractions(
                projectRepository,
                projectMemberRepository
        );
    }
}
