package com.Santhosh1802.ProjectManagement.analytics.service;

import com.Santhosh1802.ProjectManagement.analytics.dto.response.*;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User owner;
    private Project project;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@example.com");
        owner.setFirstName("Alice");
        owner.setLastName("Smith");

        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setName("Alpha");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);
        project.setStartDate(Instant.now().minus(10, ChronoUnit.DAYS));
        project.setDueDate(Instant.now().plus(10, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("Overview analytics returns 0 metrics when user has no accessible projects")
    void testGetOverviewAnalytics_EmptyProjects() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getName()).thenReturn("owner@example.com");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findProjectsByOwner(owner)).thenReturn(Collections.emptyList());
        when(projectMemberRepository.findByUserId(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        OverviewAnalyticsResponse response = analyticsService.getOverviewAnalytics(authentication);

        assertNotNull(response);
        assertEquals(0, response.getTotalProjects());
        assertEquals(0, response.getTotalTasks());
        assertEquals(0, response.getCompletionPercentage());
        assertTrue(response.getProjectProgress().isEmpty());
    }

    @Test
    @DisplayName("Overview analytics correctly calculates status distribution and completion percentage")
    void testGetOverviewAnalytics_CalculatesCorrectly() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getName()).thenReturn("owner@example.com");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findProjectsByOwner(owner)).thenReturn(List.of(project));
        when(projectMemberRepository.findByUserId(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<Object[]> statusCounts = new ArrayList<>();
        statusCounts.add(new Object[]{TaskStatus.DONE, 3L});
        statusCounts.add(new Object[]{TaskStatus.IN_PROGRESS, 1L});
        when(taskRepository.countTasksByStatusForProjects(anyCollection())).thenReturn(statusCounts);

        List<Object[]> priorityCounts = new ArrayList<>();
        priorityCounts.add(new Object[]{TaskPriority.HIGH, 2L});
        when(taskRepository.countTasksByPriorityForProjects(anyCollection())).thenReturn(priorityCounts);

        when(taskRepository.findOverdueTasksForProjects(anyCollection(), any(Instant.class), any()))
                .thenReturn(Collections.emptyList());
        when(taskRepository.findUpcomingTasksForProjects(anyCollection(), any(Instant.class), any()))
                .thenReturn(Collections.emptyList());

        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setProject(project);
        t1.setTaskStatus(TaskStatus.DONE);

        when(taskRepository.findByProjectIdIn(anyCollection())).thenReturn(List.of(t1));

        OverviewAnalyticsResponse response = analyticsService.getOverviewAnalytics(authentication);

        assertEquals(1, response.getTotalProjects());
        assertEquals(4, response.getTotalTasks());
        assertEquals(3, response.getCompletedTasks());
        assertEquals(1, response.getInProgressTasks());
        // 3 of 4 = 75%
        assertEquals(75, response.getCompletionPercentage());
        assertEquals(1, response.getProjectProgress().size());
        assertEquals(100, response.getProjectProgress().get(0).getCompletionPercentage());
    }

    @Test
    @DisplayName("Project analytics succeeds for authorized owner")
    void testGetProjectAnalytics_AuthorizedOwner() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<Object[]> statusCounts = new ArrayList<>();
        statusCounts.add(new Object[]{TaskStatus.DONE, 2L});
        when(taskRepository.countTasksByStatusForProject(projectId)).thenReturn(statusCounts);
        when(taskRepository.countTasksByPriorityForProject(projectId)).thenReturn(Collections.emptyList());
        when(taskRepository.findOverdueTasksForProject(eq(projectId), any(Instant.class))).thenReturn(Collections.emptyList());

        ProjectAnalyticsResponse response = analyticsService.getProjectAnalytics(projectId, authentication);

        assertNotNull(response);
        assertEquals("Alpha", response.getProjectName());
        assertEquals(2, response.getTotalTasks());
        assertEquals(2, response.getCompletedTasks());
        assertEquals(100, response.getCompletionPercentage());
    }

    @Test
    @DisplayName("Project analytics throws AccessDeniedException for unauthorized non-member")
    void testGetProjectAnalytics_AccessDenied() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getName()).thenReturn("stranger@example.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "stranger@example.com")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                analyticsService.getProjectAnalytics(projectId, authentication));
    }

    @Test
    @DisplayName("Project analytics throws ProjectNotFoundException for non-existent project")
    void testGetProjectAnalytics_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(projectRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () ->
                analyticsService.getProjectAnalytics(randomId, authentication));
    }

    @Test
    @DisplayName("Workload analytics correctly summarizes assigned and completed counts per user")
    void testGetWorkloadAnalytics_CalculatesCorrectly() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        when(projectRepository.findAll()).thenReturn(List.of(project));

        User assignee = new User();
        assignee.setId(UUID.randomUUID());
        assignee.setFirstName("Bob");
        assignee.setLastName("Jones");
        assignee.setEmail("bob@example.com");

        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setProject(project);
        t1.setAssignedTo(assignee);
        t1.setTaskStatus(TaskStatus.DONE);

        Task t2 = new Task();
        t2.setId(UUID.randomUUID());
        t2.setProject(project);
        t2.setAssignedTo(assignee);
        t2.setTaskStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findByProjectIdIn(anyCollection())).thenReturn(List.of(t1, t2));
        when(projectMemberRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        WorkloadAnalyticsResponse response = analyticsService.getWorkloadAnalytics(null, authentication);

        assertNotNull(response);
        assertFalse(response.getMembers().isEmpty());
        MemberWorkloadDto bob = response.getMembers().stream()
                .filter(m -> m.getUserId().equals(assignee.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(bob);
        assertEquals(2, bob.getTotalAssigned());
        assertEquals(1, bob.getCompletedTasks());
        assertEquals(1, bob.getInProgressTasks());
    }
}
