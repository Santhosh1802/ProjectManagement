package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.authorization.TaskAuthorization;
import com.Santhosh1802.ProjectManagement.authorization.UserAuthorization;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
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
class TaskAuthorizationTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserAuthorization userAuthorization;

    private TaskAuthorization taskAuthorization;

    private UUID taskId;
    private UUID projectId;
    private UUID ownerId;
    private UUID managerId;
    private UUID memberId;
    private UUID assignedUserId;
    private UUID normalUserId;

    private User owner;
    private User manager;
    private User member;
    private User assignedUser;
    private User normalUser;

    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {

        taskAuthorization = new TaskAuthorization(
                taskRepository,
                projectRepository,
                projectMemberRepository,
                userAuthorization
        );

        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        ownerId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        assignedUserId = UUID.randomUUID();
        normalUserId = UUID.randomUUID();

        owner = createUser(ownerId, "owner@example.com");
        manager = createUser(managerId, "manager@example.com");
        member = createUser(memberId, "member@example.com");
        assignedUser = createUser(
                assignedUserId,
                "assigned@example.com"
        );
        normalUser = createUser(
                normalUserId,
                "user@example.com"
        );

        project = new Project();
        project.setId(projectId);
        project.setOwner(owner);

        task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(owner);
    }

    private User createUser(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private Authentication authentication(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                java.util.Collections.emptyList()
        );
    }

    // ---------------------------------------------------------
    // canViewTask()
    // ---------------------------------------------------------

    @Test
    void ownerCanViewTask() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void managerCanViewTask() {

        Authentication authentication =
                authentication(manager.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        manager.getEmail()
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void memberCanViewTask() {

        Authentication authentication =
                authentication(member.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        member.getEmail()
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void assignedUserCanViewTask() {

        Authentication authentication =
                authentication(assignedUser.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        assignedUser.getEmail()
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void unrelatedUserCannotViewTask() {

        Authentication authentication =
                authentication(normalUser.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        normalUser.getEmail()
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertFalse(result);
    }

    @Test
    void unauthenticatedUserCannotViewTask() {

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        null
                );

        assertFalse(result);

        verifyNoInteractions(
                taskRepository,
                projectRepository,
                projectMemberRepository
        );
    }

    // ---------------------------------------------------------
    // canManageProject()
    // ---------------------------------------------------------

    @Test
    void projectOwnerCanManageProject() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                taskAuthorization.canManageProject(
                        projectId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectManagerCanManageProject() {

        Authentication authentication =
                authentication(manager.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        manager.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canManageProject(
                        projectId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectMemberCannotManageProject() {

        Authentication authentication =
                authentication(member.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        member.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canManageProject(
                        projectId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canManageTask()
    // ---------------------------------------------------------

    @Test
    void projectOwnerCanManageTask() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                taskAuthorization.canManageTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectManagerCanManageTask() {

        Authentication authentication =
                authentication(manager.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        manager.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canManageTask(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectMemberCannotManageTask() {

        Authentication authentication =
                authentication(member.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        member.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canManageTask(
                        taskId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canUpdateStatus()
    // ---------------------------------------------------------

    @Test
    void assignedUserCanUpdateTaskStatus() {

        Authentication authentication =
                authentication(assignedUser.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        boolean result =
                taskAuthorization.canUpdateStatus(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectOwnerCanUpdateTaskStatus() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                taskAuthorization.canUpdateStatus(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectManagerCanUpdateTaskStatus() {

        Authentication authentication =
                authentication(manager.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        manager.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canUpdateStatus(
                        taskId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectMemberCannotUpdateTaskStatus() {

        Authentication authentication =
                authentication(member.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmailAndProjectRole(
                        projectId,
                        member.getEmail(),
                        ProjectRole.MANAGER
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canUpdateStatus(
                        taskId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canViewProjectTasks()
    // ---------------------------------------------------------

    @Test
    void projectOwnerCanViewProjectTasks() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        boolean result =
                taskAuthorization.canViewProjectTasks(
                        projectId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void projectMemberCanViewProjectTasks() {

        Authentication authentication =
                authentication(member.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        member.getEmail()
                ))
                .thenReturn(true);

        boolean result =
                taskAuthorization.canViewProjectTasks(
                        projectId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void unrelatedUserCannotViewProjectTasks() {

        Authentication authentication =
                authentication(normalUser.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        normalUser.getEmail()
                ))
                .thenReturn(false);

        boolean result =
                taskAuthorization.canViewProjectTasks(
                        projectId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canViewAssignedTasks()
    // ---------------------------------------------------------

    @Test
    void userCanViewOwnAssignedTasks() {

        Authentication authentication =
                authentication(assignedUser.getEmail());

        when(userAuthorization.isCurrentUser(
                assignedUserId,
                authentication
        )).thenReturn(true);

        boolean result =
                taskAuthorization.canViewAssignedTasks(
                        assignedUserId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void userCannotViewAnotherUsersAssignedTasks() {

        Authentication authentication =
                authentication(normalUser.getEmail());

        when(userAuthorization.isCurrentUser(
                assignedUserId,
                authentication
        )).thenReturn(false);

        boolean result =
                taskAuthorization.canViewAssignedTasks(
                        assignedUserId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // canViewCreatedTasks()
    // ---------------------------------------------------------

    @Test
    void userCanViewOwnCreatedTasks() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(userAuthorization.isCurrentUser(
                ownerId,
                authentication
        )).thenReturn(true);

        boolean result =
                taskAuthorization.canViewCreatedTasks(
                        ownerId,
                        authentication
                );

        assertTrue(result);
    }

    @Test
    void userCannotViewAnotherUsersCreatedTasks() {

        Authentication authentication =
                authentication(normalUser.getEmail());

        when(userAuthorization.isCurrentUser(
                ownerId,
                authentication
        )).thenReturn(false);

        boolean result =
                taskAuthorization.canViewCreatedTasks(
                        ownerId,
                        authentication
                );

        assertFalse(result);
    }

    // ---------------------------------------------------------
    // Missing task/project cases
    // ---------------------------------------------------------

    @Test
    void cannotViewNonExistingTask() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        boolean result =
                taskAuthorization.canViewTask(
                        taskId,
                        authentication
                );

        assertFalse(result);
    }

    @Test
    void cannotManageNonExistingTask() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        boolean result =
                taskAuthorization.canManageTask(
                        taskId,
                        authentication
                );

        assertFalse(result);
    }

    @Test
    void cannotManageNonExistingProject() {

        Authentication authentication =
                authentication(owner.getEmail());

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        boolean result =
                taskAuthorization.canManageProject(
                        projectId,
                        authentication
                );

        assertFalse(result);
    }

    @Test
    void unauthenticatedUserCannotManageTask() {

        boolean result =
                taskAuthorization.canManageTask(
                        taskId,
                        null
                );

        assertFalse(result);

        verifyNoInteractions(
                taskRepository,
                projectRepository,
                projectMemberRepository
        );
    }
}
