package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.authorization.ProjectAuthorization;
import com.Santhosh1802.ProjectManagement.dto.request.project.CreateProjectRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.DeleteProjectRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.GetProjectByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.UpdateProjectDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.UpdateProjectOwnerRequest;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.service.ProjectService;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProjectSecurityIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProjectAuthorization projectAuthorization;

    private UUID projectId;
    private UUID ownerId;
    private UUID newOwnerId;

    private User owner;
    private User newOwner;
    private Project project;

    private final Instant startDate =
            Instant.parse("2026-10-01T00:00:00Z");

    private final Instant dueDate =
            Instant.parse("2026-12-31T00:00:00Z");

    @BeforeEach
    void setUp() {

        projectId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        newOwnerId = UUID.randomUUID();

        owner = new User();
        owner.setId(ownerId);
        owner.setFirstName("Owner");
        owner.setLastName("User");
        owner.setEmail("owner@example.com");
        owner.setUserRole(UserRole.USER);
        owner.setIsActive(true);

        newOwner = new User();
        newOwner.setId(newOwnerId);
        newOwner.setFirstName("New");
        newOwner.setLastName("Owner");
        newOwner.setEmail("newowner@example.com");
        newOwner.setUserRole(UserRole.USER);
        newOwner.setIsActive(true);

        project = new Project();
        project.setId(projectId);
        project.setName("DevFlow");
        project.setDescription("Project management application");
        project.setProjectStatus(ProjectStatus.PLANNING);
        project.setStartDate(startDate);
        project.setDueDate(dueDate);
        project.setOwner(owner);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
    }

    // ============================================================
    // CREATE PROJECT
    // ============================================================

    @Test
    @WithMockUser(
            username = "user@example.com",
            roles = "USER"
    )
    void user_shouldBeAllowedToCreateProject() {

        testCreateProject();
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = "MANAGER"
    )
    void manager_shouldBeAllowedToCreateProject() {

        testCreateProject();
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAllowedToCreateProject() {

        testCreateProject();
    }

    private void testCreateProject() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assert authentication != null;
        String email = authentication.getName();

        CreateProjectRequest request =
                new CreateProjectRequest(
                        "New Project",
                        "A new project for testing",
                        startDate,
                        dueDate
                );

        User authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());
        authenticatedUser.setFirstName("Test");
        authenticatedUser.setLastName("User");
        authenticatedUser.setEmail(email);
        authenticatedUser.setUserRole(UserRole.USER);
        authenticatedUser.setIsActive(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(authenticatedUser));

        when(projectRepository.findProjectByName("New Project"))
                .thenReturn(Optional.empty());

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {

                    Project saved =
                            invocation.getArgument(0);

                    saved.setId(UUID.randomUUID());

                    return saved;
                });

        var response =
                projectService.createProject(
                        request,
                        authentication
                );

        assertNotNull(response);

        verify(userRepository)
                .findByEmail(email);

        verify(projectRepository)
                .findProjectByName("New Project");

        verify(projectRepository)
                .save(any(Project.class));
    }

    // ============================================================
    // GET PROJECT
    // ============================================================

    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "USER"
    )
    void owner_shouldBeAllowedToViewProject() {

        GetProjectByIdRequest request =
                new GetProjectByIdRequest(projectId);

        when(projectAuthorization.canView(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(true);

        var response =
                projectService.getProjectById(request);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
    }

    @Test
    @WithMockUser(
            username = "other@example.com",
            roles = "USER"
    )
    void nonOwner_shouldNotBeAllowedToViewProject() {

        GetProjectByIdRequest request =
                new GetProjectByIdRequest(projectId);

        when(projectAuthorization.canView(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.getProjectById(request)
        );

        verify(projectRepository, never())
                .findById(any());
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAllowedToViewAnyProject() {

        GetProjectByIdRequest request =
                new GetProjectByIdRequest(projectId);

        var response =
                projectService.getProjectById(request);

        assertNotNull(response);
        assertEquals(projectId, response.getId());

        verify(projectAuthorization, never())
                .canView(any(), any());
    }

    // ============================================================
    // UPDATE PROJECT DETAILS
    // ============================================================

    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "USER"
    )
    void owner_shouldBeAllowedToUpdateProject() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest();

        request.setId(projectId);
        request.setName("Updated DevFlow");
        request.setDescription("Updated project description");
        request.setProjectStatus(ProjectStatus.ACTIVE);
        request.setDueDate(dueDate);

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(true);

        when(projectRepository.findProjectByName("Updated DevFlow"))
                .thenReturn(Optional.empty());

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        assertDoesNotThrow(() ->
                projectService.updateProjectDetails(request)
        );

        verify(projectRepository)
                .save(project);
    }

    @Test
    @WithMockUser(
            username = "other@example.com",
            roles = "USER"
    )
    void nonOwner_shouldNotBeAllowedToUpdateProject() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest();

        request.setId(projectId);
        request.setName("Updated DevFlow");
        request.setDescription("Updated project description");
        request.setProjectStatus(ProjectStatus.ACTIVE);
        request.setDueDate(dueDate);

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProjectDetails(request)
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAllowedToUpdateAnyProject() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest();

        request.setId(projectId);
        request.setName("Admin Updated Project");
        request.setDescription("Updated by administrator");
        request.setProjectStatus(ProjectStatus.ACTIVE);
        request.setDueDate(dueDate);

        when(projectRepository.findProjectByName(
                "Admin Updated Project"
        )).thenReturn(Optional.empty());

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        assertDoesNotThrow(() ->
                projectService.updateProjectDetails(request)
        );

        verify(projectRepository)
                .save(project);

        verify(projectAuthorization, never())
                .isOwner(any(), any());
    }

    // ============================================================
    // UPDATE PROJECT OWNER
    // ============================================================

    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "USER"
    )
    void owner_shouldBeAllowedToUpdateProjectOwner() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(true);

        when(userRepository.findById(newOwnerId))
                .thenReturn(Optional.of(newOwner));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response =
                projectService.updateProjectOwner(request);

        assertNotNull(response);

        assertEquals(
                newOwnerId,
                project.getOwner().getId()
        );

        verify(userRepository)
                .findById(newOwnerId);

        verify(projectRepository)
                .save(project);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAllowedToUpdateProjectOwner() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(userRepository.findById(newOwnerId))
                .thenReturn(Optional.of(newOwner));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response =
                projectService.updateProjectOwner(request);

        assertNotNull(response);

        assertEquals(
                newOwnerId,
                project.getOwner().getId()
        );

        verify(projectRepository)
                .save(project);

        verify(projectAuthorization, never())
                .isOwner(any(), any());
    }

    @Test
    @WithMockUser(
            username = "other@example.com",
            roles = "USER"
    )
    void nonOwner_shouldNotBeAllowedToUpdateProjectOwner() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProjectOwner(request)
        );

        verify(userRepository, never())
                .findById(any());

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    // ============================================================
    // DELETE PROJECT
    // ============================================================

    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "USER"
    )
    void owner_shouldBeAllowedToDeleteProject() {

        DeleteProjectRequest request =
                new DeleteProjectRequest(projectId);

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(true);

        doNothing()
                .when(projectRepository)
                .deleteById(projectId);

        boolean result =
                projectService.deleteProject(request);

        assertTrue(result);

        verify(projectRepository)
                .deleteById(projectId);
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAllowedToDeleteAnyProject() {

        DeleteProjectRequest request =
                new DeleteProjectRequest(projectId);

        doNothing()
                .when(projectRepository)
                .deleteById(projectId);

        boolean result =
                projectService.deleteProject(request);

        assertTrue(result);

        verify(projectRepository)
                .deleteById(projectId);

        verify(projectAuthorization, never())
                .isOwner(any(), any());
    }

    @Test
    @WithMockUser(
            username = "other@example.com",
            roles = "USER"
    )
    void nonOwner_shouldNotBeAllowedToDeleteProject() {

        DeleteProjectRequest request =
                new DeleteProjectRequest(projectId);

        when(projectAuthorization.isOwner(
                eq(projectId),
                any(Authentication.class)
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteProject(request)
        );

        verify(projectRepository, never())
                .deleteById(any());
    }
}