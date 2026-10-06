package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.project.CreateProjectRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.DeleteProjectRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.GetProjectByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.GetProjectSearchRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.UpdateProjectDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.project.UpdateProjectOwnerRequest;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

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
        owner.setFirstName("Santhosh");
        owner.setLastName("K");
        owner.setEmail("santhosh@example.com");
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
        project.setCreatedAt(Instant.parse("2026-09-01T00:00:00Z"));
        project.setUpdatedAt(Instant.parse("2026-09-01T00:00:00Z"));
    }

    // -------------------------------------------------------------------------
    // CREATE PROJECT
    // -------------------------------------------------------------------------

    @Test
    void createProject_shouldCreateSuccessfully() {

        CreateProjectRequest request = new CreateProjectRequest(
                "DevFlow",
                "Project management application",
                startDate,
                dueDate
        );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findProjectByName("DevFlow"))
                .thenReturn(Optional.empty());

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        GetProjectResponse response =
                projectService.createProject(request, authentication);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals("DevFlow", response.getName());
        assertEquals(ProjectStatus.PLANNING, response.getProjectStatus());

        verify(userRepository)
                .findByEmail(owner.getEmail());

        verify(projectRepository)
                .findProjectByName("DevFlow");

        verify(projectRepository)
                .save(any(Project.class));
    }

    @Test
    void createProject_shouldThrowWhenAuthenticatedUserNotFound() {

        CreateProjectRequest request = new CreateProjectRequest(
                "DevFlow",
                "Project management application",
                startDate,
                dueDate
        );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> projectService.createProject(
                        request,
                        authentication
                )
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void createProject_shouldThrowWhenProjectAlreadyExists() {

        CreateProjectRequest request = new CreateProjectRequest(
                "DevFlow",
                "Project management application",
                startDate,
                dueDate
        );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findProjectByName("DevFlow"))
                .thenReturn(Optional.of(project));

        assertThrows(
                ProjectAlreadyExistException.class,
                () -> projectService.createProject(
                        request,
                        authentication
                )
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void createProject_shouldThrowWhenDueDateBeforeStartDate() {

        Instant invalidDueDate =
                Instant.parse("2026-09-30T00:00:00Z");

        CreateProjectRequest request = new CreateProjectRequest(
                "DevFlow",
                "Project management application",
                startDate,
                invalidDueDate
        );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.findProjectByName("DevFlow"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(
                        request,
                        authentication
                )
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    // -------------------------------------------------------------------------
    // GET PROJECT
    // -------------------------------------------------------------------------

    @Test
    void getProjectById_shouldReturnProject() {

        GetProjectByIdRequest request =
                new GetProjectByIdRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        GetProjectResponse response =
                projectService.getProjectById(request);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals("DevFlow", response.getName());
        assertEquals("Project management application",
                response.getDescription());

        verify(projectRepository)
                .findById(projectId);
    }

    @Test
    void getProjectById_shouldThrowWhenProjectNotFound() {

        GetProjectByIdRequest request =
                new GetProjectByIdRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.getProjectById(request)
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE PROJECT DETAILS
    // -------------------------------------------------------------------------

    @Test
    void updateProjectDetails_shouldUpdateSuccessfully() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest(
                        projectId,
                        "DevFlow Updated",
                        "Updated project description",
                        ProjectStatus.ACTIVE,
                        dueDate
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.findProjectByName("DevFlow Updated"))
                .thenReturn(Optional.empty());

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        GetProjectResponse response =
                projectService.updateProjectDetails(request);

        assertNotNull(response);
        assertEquals("DevFlow Updated", response.getName());
        assertEquals(
                "Updated project description",
                response.getDescription()
        );
        assertEquals(
                ProjectStatus.ACTIVE,
                response.getProjectStatus()
        );

        verify(projectRepository)
                .findById(projectId);

        verify(projectRepository)
                .findProjectByName("DevFlow Updated");

        verify(projectRepository)
                .save(project);
    }

    @Test
    void updateProjectDetails_shouldThrowWhenProjectNotFound() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest(
                        projectId,
                        "DevFlow Updated",
                        "Updated project description",
                        ProjectStatus.ACTIVE,
                        dueDate
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.updateProjectDetails(request)
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProjectDetails_shouldThrowWhenDueDateBeforeStartDate() {

        Instant invalidDueDate =
                Instant.parse("2026-09-30T00:00:00Z");

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest(
                        projectId,
                        "DevFlow Updated",
                        "Updated project description",
                        ProjectStatus.ACTIVE,
                        invalidDueDate
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.updateProjectDetails(request)
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProjectDetails_shouldThrowWhenNewNameAlreadyExists() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest(
                        projectId,
                        "Existing Project",
                        "Updated project description",
                        ProjectStatus.ACTIVE,
                        dueDate
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.findProjectByName("Existing Project"))
                .thenReturn(Optional.of(new Project()));

        assertThrows(
                ProjectAlreadyExistException.class,
                () -> projectService.updateProjectDetails(request)
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProjectDetails_shouldUpdateWhenNameIsUnchanged() {

        UpdateProjectDetailsRequest request =
                new UpdateProjectDetailsRequest(
                        projectId,
                        "DevFlow",
                        "Updated description",
                        ProjectStatus.COMPLETED,
                        dueDate
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        GetProjectResponse response =
                projectService.updateProjectDetails(request);

        assertNotNull(response);
        assertEquals("DevFlow", response.getName());
        assertEquals(
                "Updated description",
                response.getDescription()
        );
        assertEquals(
                ProjectStatus.COMPLETED,
                response.getProjectStatus()
        );

        verify(projectRepository)
                .save(project);

        verify(projectRepository, never())
                .findProjectByName(anyString());
    }

    // -------------------------------------------------------------------------
    // UPDATE OWNER
    // -------------------------------------------------------------------------

    @Test
    void updateProjectOwner_shouldUpdateSuccessfully() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(newOwnerId))
                .thenReturn(Optional.of(newOwner));

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        GetProjectResponse response =
                projectService.updateProjectOwner(request);

        assertNotNull(response);
        assertEquals(
                newOwnerId,
                response.getOwner().getId()
        );

        verify(projectRepository)
                .findById(projectId);

        verify(userRepository)
                .findById(newOwnerId);

        verify(projectRepository)
                .save(project);
    }

    @Test
    void updateProjectOwner_shouldThrowWhenProjectNotFound() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.updateProjectOwner(request)
        );

        verify(userRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    void updateProjectOwner_shouldThrowWhenOwnerNotFound() {

        UpdateProjectOwnerRequest request =
                new UpdateProjectOwnerRequest(
                        projectId,
                        newOwnerId
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(newOwnerId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> projectService.updateProjectOwner(request)
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    // -------------------------------------------------------------------------
    // DELETE PROJECT
    // -------------------------------------------------------------------------

    @Test
    void deleteProject_shouldDeleteSuccessfully() {

        DeleteProjectRequest request =
                new DeleteProjectRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                projectService.deleteProject(request);

        assertTrue(result);

        verify(projectRepository)
                .findById(projectId);

        verify(projectRepository)
                .deleteById(projectId);
    }

    @Test
    void deleteProject_shouldThrowWhenProjectNotFound() {

        DeleteProjectRequest request =
                new DeleteProjectRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.deleteProject(request)
        );

        verify(projectRepository, never())
                .deleteById(any(UUID.class));
    }

    // -------------------------------------------------------------------------
    // SEARCH PROJECTS
    // -------------------------------------------------------------------------

    @Test
    void searchProjects_shouldSearchAllProjectsForAdmin() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        request.setPage(0);
        request.setSize(10);
        request.setKeyword("  DevFlow  ");
        request.setSortBy("name");
        request.setSortOrder("asc");
        request.setScope(null);

        owner.setUserRole(UserRole.ADMIN);

        Page<Project> page =
                new PageImpl<>(
                        List.of(project)
                );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.searchAllProjects(
                eq("DevFlow"),
                isNull(),
                isNull(),
                any()
        )).thenReturn(page);

        PageResponse<GetProjectResponse> response =
                projectService.searchProjects(
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(
                projectId,
                response.getContent().getFirst().getId()
        );

        verify(projectRepository)
                .searchAllProjects(
                        eq("DevFlow"),
                        isNull(),
                        isNull(),
                        any()
                );
    }

    @Test
    void searchProjects_shouldSearchOwnedProjectsForUser() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        request.setPage(0);
        request.setSize(10);
        request.setKeyword("DevFlow");
        request.setSortBy("name");
        request.setSortOrder("asc");
        request.setScope(ProjectScope.OWNED);

        owner.setUserRole(UserRole.USER);

        Page<Project> page =
                new PageImpl<>(
                        List.of(project)
                );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.searchOwnedProjects(
                eq(ownerId),
                eq("DevFlow"),
                isNull(),
                any()
        )).thenReturn(page);

        PageResponse<GetProjectResponse> response =
                projectService.searchProjects(
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        verify(projectRepository)
                .searchOwnedProjects(
                        eq(ownerId),
                        eq("DevFlow"),
                        isNull(),
                        any()
                );
    }

    @Test
    void searchProjects_shouldSearchSharedProjectsForUser() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        request.setPage(0);
        request.setSize(10);
        request.setKeyword(null);
        request.setSortBy("createdAt");
        request.setSortOrder("desc");
        request.setScope(ProjectScope.SHARED);

        owner.setUserRole(UserRole.USER);

        Page<Project> page =
                new PageImpl<>(
                        List.of(project)
                );

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.searchSharedProjects(
                eq(ownerId),
                isNull(),
                isNull(),
                any()
        )).thenReturn(page);

        PageResponse<GetProjectResponse> response =
                projectService.searchProjects(
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        verify(projectRepository)
                .searchSharedProjects(
                        eq(ownerId),
                        isNull(),
                        isNull(),
                        any()
                );
    }

    @Test
    void searchProjects_shouldThrowWhenUserSearchesAnotherUsersProjects() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        request.setPage(0);
        request.setSize(10);
        request.setOwnerId(UUID.randomUUID());

        owner.setUserRole(UserRole.USER);

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.searchProjects(
                        request,
                        authentication
                )
        );

        verify(projectRepository, never())
                .searchOwnedProjects(
                        any(UUID.class),
                        any(),
                        any(),
                        any()
                );

        verify(projectRepository, never())
                .searchSharedProjects(
                        any(UUID.class),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void searchProjects_shouldThrowWhenAuthenticatedUserNotFound() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        when(authentication.getName())
                .thenReturn("missing@example.com");

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> projectService.searchProjects(
                        request,
                        authentication
                )
        );
    }

    @Test
    void searchProjects_shouldSearchOwnedProjectsForAllScope() {

        GetProjectSearchRequest request =
                new GetProjectSearchRequest();

        request.setPage(0);
        request.setSize(10);
        request.setKeyword(null);
        request.setSortBy("createdAt");
        request.setSortOrder("desc");
        request.setScope(ProjectScope.ALL);

        owner.setUserRole(UserRole.USER);

        Page<Project> page =
                new PageImpl<>(List.of(project));

        when(authentication.getName())
                .thenReturn(owner.getEmail());

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));

        when(projectRepository.searchOwnedProjects(
                eq(ownerId),
                isNull(),
                isNull(),
                any()
        )).thenReturn(page);

        PageResponse<GetProjectResponse> response =
                projectService.searchProjects(
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        verify(projectRepository)
                .searchOwnedProjects(
                        eq(ownerId),
                        isNull(),
                        isNull(),
                        any()
                );
    }
}
