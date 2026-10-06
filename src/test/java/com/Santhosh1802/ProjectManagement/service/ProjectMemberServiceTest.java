package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
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
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private UUID userId;
    private UUID projectId;
    private UUID projectMemberId;

    private User user;
    private User projectOwner;
    private Project project;
    private ProjectMember projectMember;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        projectMemberId = UUID.randomUUID();

        projectOwner = new User();
        projectOwner.setId(UUID.randomUUID());
        projectOwner.setFirstName("Owner");
        projectOwner.setLastName("User");
        projectOwner.setEmail("owner@example.com");
        projectOwner.setUserRole(UserRole.USER);
        projectOwner.setIsActive(true);
        projectOwner.setEmailVerified(true);
        projectOwner.setCreatedAt(Instant.now());
        projectOwner.setUpdatedAt(Instant.now());
        projectOwner.setLastLoginAt(Instant.now());

        user = new User();
        user.setId(userId);
        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhosh@example.com");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setLastLoginAt(Instant.now());

        project = new Project();
        project.setId(projectId);
        project.setName("DevFlow");
        project.setDescription("Project Management System");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setStartDate(Instant.now());
        project.setDueDate(Instant.now().plus(Duration.ofDays(30)));
        project.setOwner(projectOwner);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        projectMember = new ProjectMember();
        projectMember.setId(projectMemberId);
        projectMember.setUser(user);
        projectMember.setProject(project);
        projectMember.setProjectRole(ProjectRole.MEMBER);
        projectMember.setJoinedAt(Instant.now());
    }

    // -------------------------------------------------------------------------
    // addMember()
    // -------------------------------------------------------------------------

    @Test
    void addMember_shouldCreateProjectMember_whenUserAndProjectExist() {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        userId,
                        projectId,
                        ProjectRole.MEMBER
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.existsByUserIdAndProjectId(
                userId,
                projectId
        )).thenReturn(false);

        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenAnswer(invocation -> {
                    ProjectMember saved = invocation.getArgument(0);
                    saved.setId(projectMemberId);
                    saved.setJoinedAt(Instant.now());
                    return saved;
                });

        GetProjectMemberResponse response =
                projectMemberService.addMember(request);

        assertNotNull(response);
        assertEquals(projectMemberId, response.getId());
        assertEquals(userId, response.getUser().getId());
        assertEquals(projectId, response.getProject().getId());
        assertEquals(ProjectRole.MEMBER, response.getProjectRole());

        verify(userRepository).findById(userId);
        verify(projectRepository).findById(projectId);
        verify(projectMemberRepository)
                .existsByUserIdAndProjectId(userId, projectId);
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void addMember_shouldThrowUserNotFoundException_whenUserDoesNotExist() {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        userId,
                        projectId,
                        ProjectRole.MEMBER
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> projectMemberService.addMember(request)
        );

        verify(userRepository).findById(userId);

        verify(projectRepository, never())
                .findById(any(UUID.class));

        verify(projectMemberRepository, never())
                .save(any(ProjectMember.class));
    }

    @Test
    void addMember_shouldThrowProjectNotFoundException_whenProjectDoesNotExist() {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        userId,
                        projectId,
                        ProjectRole.MEMBER
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectMemberService.addMember(request)
        );

        verify(userRepository).findById(userId);
        verify(projectRepository).findById(projectId);

        verify(projectMemberRepository, never())
                .existsByUserIdAndProjectId(any(UUID.class), any(UUID.class));

        verify(projectMemberRepository, never())
                .save(any(ProjectMember.class));
    }

    @Test
    void addMember_shouldThrowProjectMemberAlreadyExistException_whenMemberAlreadyExists() {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        userId,
                        projectId,
                        ProjectRole.MEMBER
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.existsByUserIdAndProjectId(
                userId,
                projectId
        )).thenReturn(true);

        assertThrows(
                ProjectMemberAlreadyExistException.class,
                () -> projectMemberService.addMember(request)
        );

        verify(projectMemberRepository)
                .existsByUserIdAndProjectId(userId, projectId);

        verify(projectMemberRepository, never())
                .save(any(ProjectMember.class));
    }

    // -------------------------------------------------------------------------
    // getMemberById()
    // -------------------------------------------------------------------------

    @Test
    void getMemberById_shouldReturnProjectMember_whenMemberExists() {

        GetByIdRequest request =
                new GetByIdRequest(projectMemberId);

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.of(projectMember));

        GetProjectMemberResponse response =
                projectMemberService.getMemberById(request);

        assertNotNull(response);
        assertEquals(projectMemberId, response.getId());
        assertEquals(userId, response.getUser().getId());
        assertEquals(projectId, response.getProject().getId());
        assertEquals(ProjectRole.MEMBER, response.getProjectRole());

        verify(projectMemberRepository)
                .findById(projectMemberId);
    }

    @Test
    void getMemberById_shouldThrowProjectMemberNotFoundException_whenMemberDoesNotExist() {

        GetByIdRequest request =
                new GetByIdRequest(projectMemberId);

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectMemberNotFoundException.class,
                () -> projectMemberService.getMemberById(request)
        );

        verify(projectMemberRepository)
                .findById(projectMemberId);
    }

    // -------------------------------------------------------------------------
    // getMembersByProject()
    // -------------------------------------------------------------------------

    @Test
    void getMembersByProject_shouldReturnPaginatedMembers_whenProjectExists() {

        GetByIdRequest request =
                new GetByIdRequest(projectId);

        Page<ProjectMember> projectMemberPage =
                new PageImpl<>(
                        List.of(projectMember),
                        Pageable.ofSize(10),
                        1
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectId(
                eq(projectId),
                any(Pageable.class)
        )).thenReturn(projectMemberPage);

        var response =
                projectMemberService.getMembersByProject(
                        request,
                        0,
                        10
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(projectMemberId, response.getContent().getFirst().getId());

        verify(projectRepository)
                .findById(projectId);

        verify(projectMemberRepository)
                .findByProjectId(
                        eq(projectId),
                        any(Pageable.class)
                );
    }

    @Test
    void getMembersByProject_shouldThrowProjectNotFoundException_whenProjectDoesNotExist() {

        GetByIdRequest request =
                new GetByIdRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectMemberService.getMembersByProject(
                        request,
                        0,
                        10
                )
        );

        verify(projectRepository)
                .findById(projectId);

        verify(projectMemberRepository, never())
                .findByProjectId(
                        any(UUID.class),
                        any(Pageable.class)
                );
    }

    // -------------------------------------------------------------------------
    // getProjectsByUser()
    // -------------------------------------------------------------------------

    @Test
    void getProjectsByUser_shouldReturnPaginatedMemberships_whenUserExists() {

        GetByIdRequest request =
                new GetByIdRequest(userId);

        Page<ProjectMember> projectMemberPage =
                new PageImpl<>(
                        List.of(projectMember),
                        Pageable.ofSize(10),
                        1
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(projectMemberRepository.findByUserId(
                eq(userId),
                any(Pageable.class)
        )).thenReturn(projectMemberPage);

        var response =
                projectMemberService.getProjectsByUser(
                        request,
                        0,
                        10
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(projectMemberId, response.getContent().getFirst().getId());

        verify(userRepository)
                .findById(userId);

        verify(projectMemberRepository)
                .findByUserId(
                        eq(userId),
                        any(Pageable.class)
                );
    }

    @Test
    void getProjectsByUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {

        GetByIdRequest request =
                new GetByIdRequest(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> projectMemberService.getProjectsByUser(
                        request,
                        0,
                        10
                )
        );

        verify(userRepository)
                .findById(userId);

        verify(projectMemberRepository, never())
                .findByUserId(
                        any(UUID.class),
                        any(Pageable.class)
                );
    }

    // -------------------------------------------------------------------------
    // updateMemberRole()
    // -------------------------------------------------------------------------

    @Test
    void updateMemberRole_shouldUpdateRole_whenMemberExists() {

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        projectMemberId,
                        ProjectRole.MANAGER
                );

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.of(projectMember));

        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GetProjectMemberResponse response =
                projectMemberService.updateMemberRole(request);

        assertNotNull(response);
        assertEquals(ProjectRole.MANAGER, response.getProjectRole());

        assertEquals(
                ProjectRole.MANAGER,
                projectMember.getProjectRole()
        );

        verify(projectMemberRepository)
                .findById(projectMemberId);

        verify(projectMemberRepository)
                .save(projectMember);
    }

    @Test
    void updateMemberRole_shouldThrowProjectMemberNotFoundException_whenMemberDoesNotExist() {

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        projectMemberId,
                        ProjectRole.MANAGER
                );

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectMemberNotFoundException.class,
                () -> projectMemberService.updateMemberRole(request)
        );

        verify(projectMemberRepository)
                .findById(projectMemberId);

        verify(projectMemberRepository, never())
                .save(any(ProjectMember.class));
    }

    // -------------------------------------------------------------------------
    // removeMember()
    // -------------------------------------------------------------------------

    @Test
    void removeMember_shouldDeleteMember_whenMemberExists() {

        DeleteProjectMemberRequest request =
                new DeleteProjectMemberRequest(projectMemberId);

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.of(projectMember));

        boolean result =
                projectMemberService.removeMember(request);

        assertTrue(result);

        verify(projectMemberRepository)
                .findById(projectMemberId);

        verify(projectMemberRepository)
                .delete(projectMember);
    }

    @Test
    void removeMember_shouldThrowProjectMemberNotFoundException_whenMemberDoesNotExist() {

        DeleteProjectMemberRequest request =
                new DeleteProjectMemberRequest(projectMemberId);

        when(projectMemberRepository.findById(projectMemberId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectMemberNotFoundException.class,
                () -> projectMemberService.removeMember(request)
        );

        verify(projectMemberRepository)
                .findById(projectMemberId);

        verify(projectMemberRepository, never())
                .delete(any(ProjectMember.class));
    }
}