package com.Santhosh1802.ProjectManagement.analytics.service;

import com.Santhosh1802.ProjectManagement.analytics.dto.response.*;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public AnalyticsService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Resolves all projects accessible to the current user (owned + member), or all projects for ADMIN.
     */
    private List<Project> resolveAccessibleProjects(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return projectRepository.findAll();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));

        List<Project> owned = projectRepository.findProjectsByOwner(user);
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Project> shared = memberships.stream().map(ProjectMember::getProject).toList();

        Map<UUID, Project> projectMap = new LinkedHashMap<>();
        for (Project p : owned) {
            projectMap.put(p.getId(), p);
        }
        for (Project p : shared) {
            projectMap.putIfAbsent(p.getId(), p);
        }

        return new ArrayList<>(projectMap.values());
    }

    /**
     * Overview analytics across accessible projects.
     */
    @Transactional(readOnly = true)
    public OverviewAnalyticsResponse getOverviewAnalytics(Authentication authentication) {
        List<Project> projects = resolveAccessibleProjects(authentication);

        if (projects.isEmpty()) {
            return OverviewAnalyticsResponse.builder()
                    .totalProjects(0)
                    .activeProjects(0)
                    .completedProjects(0)
                    .planningProjects(0)
                    .totalTasks(0)
                    .completedTasks(0)
                    .inProgressTasks(0)
                    .todoTasks(0)
                    .cancelledTasks(0)
                    .overdueTasks(0)
                    .completionPercentage(0)
                    .tasksByStatus(initStatusMap())
                    .tasksByPriority(initPriorityMap())
                    .projectProgress(Collections.emptyList())
                    .overdueTasksList(Collections.emptyList())
                    .upcomingTasksList(Collections.emptyList())
                    .build();
        }

        long totalProjects = projects.size();
        long activeProjects = projects.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getProjectStatus().name())).count();
        long completedProjects = projects.stream().filter(p -> "COMPLETED".equalsIgnoreCase(p.getProjectStatus().name())).count();
        long planningProjects = projects.stream().filter(p -> "PLANNING".equalsIgnoreCase(p.getProjectStatus().name())).count();

        List<UUID> projectIds = projects.stream().map(Project::getId).toList();

        // Database aggregation for status
        Map<String, Long> tasksByStatus = initStatusMap();
        List<Object[]> statusCounts = taskRepository.countTasksByStatusForProjects(projectIds);
        long totalTasks = 0;
        long completedTasks = 0;
        long inProgressTasks = 0;
        long todoTasks = 0;
        long cancelledTasks = 0;

        for (Object[] row : statusCounts) {
            TaskStatus status = (TaskStatus) row[0];
            long count = ((Number) row[1]).longValue();
            tasksByStatus.put(status.name(), count);
            totalTasks += count;
            if (status == TaskStatus.DONE) completedTasks = count;
            else if (status == TaskStatus.IN_PROGRESS) inProgressTasks = count;
            else if (status == TaskStatus.TODO) todoTasks = count;
            else if (status == TaskStatus.CANCELLED) cancelledTasks = count;
        }

        // Database aggregation for priority
        Map<String, Long> tasksByPriority = initPriorityMap();
        List<Object[]> priorityCounts = taskRepository.countTasksByPriorityForProjects(projectIds);
        for (Object[] row : priorityCounts) {
            TaskPriority priority = (TaskPriority) row[0];
            long count = ((Number) row[1]).longValue();
            tasksByPriority.put(priority.name(), count);
        }

        Instant now = Instant.now();
        List<Task> overdueTaskList = taskRepository.findOverdueTasksForProjects(projectIds, now, PageRequest.of(0, 10));
        long overdueCount = overdueTaskList.size();

        List<Task> upcomingTaskList = taskRepository.findUpcomingTasksForProjects(projectIds, now, PageRequest.of(0, 10));

        // Calculate project progress list
        List<Task> allAccessibleTasks = taskRepository.findByProjectIdIn(projectIds);
        Map<UUID, List<Task>> tasksByProject = allAccessibleTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getProject().getId()));

        List<ProjectProgressDto> projectProgressList = new ArrayList<>();
        for (Project p : projects) {
            List<Task> pTasks = tasksByProject.getOrDefault(p.getId(), Collections.emptyList());
            long pTotal = pTasks.size();
            long pDone = pTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.DONE).count();
            int pPercentage = pTotal > 0 ? (int) Math.round(((double) pDone / pTotal) * 100.0) : 0;

            projectProgressList.add(ProjectProgressDto.builder()
                    .projectId(p.getId())
                    .projectName(p.getName())
                    .projectStatus(p.getProjectStatus().name())
                    .totalTasks(pTotal)
                    .completedTasks(pDone)
                    .completionPercentage(pPercentage)
                    .dueDate(p.getDueDate())
                    .build());
        }

        int completionPercentage = totalTasks > 0 ? (int) Math.round(((double) completedTasks / totalTasks) * 100.0) : 0;

        return OverviewAnalyticsResponse.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .planningProjects(planningProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueCount)
                .completionPercentage(completionPercentage)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .projectProgress(projectProgressList)
                .overdueTasksList(overdueTaskList.stream().map(this::toOverdueDto).toList())
                .upcomingTasksList(upcomingTaskList.stream().map(this::toUpcomingDto).toList())
                .build();
    }

    /**
     * Analytics for a specific project.
     */
    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id: " + projectId + " not found"));

        validateProjectAccess(project, authentication);

        Map<String, Long> tasksByStatus = initStatusMap();
        List<Object[]> statusCounts = taskRepository.countTasksByStatusForProject(projectId);
        long totalTasks = 0;
        long completedTasks = 0;
        long inProgressTasks = 0;
        long todoTasks = 0;
        long cancelledTasks = 0;

        for (Object[] row : statusCounts) {
            TaskStatus status = (TaskStatus) row[0];
            long count = ((Number) row[1]).longValue();
            tasksByStatus.put(status.name(), count);
            totalTasks += count;
            if (status == TaskStatus.DONE) completedTasks = count;
            else if (status == TaskStatus.IN_PROGRESS) inProgressTasks = count;
            else if (status == TaskStatus.TODO) todoTasks = count;
            else if (status == TaskStatus.CANCELLED) cancelledTasks = count;
        }

        Map<String, Long> tasksByPriority = initPriorityMap();
        List<Object[]> priorityCounts = taskRepository.countTasksByPriorityForProject(projectId);
        for (Object[] row : priorityCounts) {
            TaskPriority priority = (TaskPriority) row[0];
            long count = ((Number) row[1]).longValue();
            tasksByPriority.put(priority.name(), count);
        }

        Instant now = Instant.now();
        long overdueTasks = taskRepository.findOverdueTasksForProject(projectId, now).size();
        int completionPercentage = totalTasks > 0 ? (int) Math.round(((double) completedTasks / totalTasks) * 100.0) : 0;

        String ownerName = project.getOwner() != null
                ? project.getOwner().getFirstName() + (project.getOwner().getLastName() != null ? " " + project.getOwner().getLastName() : "")
                : "Unknown";

        return ProjectAnalyticsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .projectStatus(project.getProjectStatus().name())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .ownerName(ownerName)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .todoTasks(todoTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTasks)
                .completionPercentage(completionPercentage)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .build();
    }

    /**
     * Team workload summary for accessible projects.
     */
    @Transactional(readOnly = true)
    public WorkloadAnalyticsResponse getWorkloadAnalytics(UUID projectIdFilter, Authentication authentication) {
        List<Project> projects;
        if (projectIdFilter != null) {
            Project project = projectRepository.findById(projectIdFilter)
                    .orElseThrow(() -> new ProjectNotFoundException("Project with id: " + projectIdFilter + " not found"));
            validateProjectAccess(project, authentication);
            projects = List.of(project);
        } else {
            projects = resolveAccessibleProjects(authentication);
        }

        if (projects.isEmpty()) {
            return WorkloadAnalyticsResponse.builder().members(Collections.emptyList()).build();
        }

        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        List<Task> tasks = taskRepository.findByProjectIdIn(projectIds);

        Instant now = Instant.now();

        // Map userId -> workload stats
        Map<UUID, User> memberUsers = new LinkedHashMap<>();
        Map<UUID, Long> totalAssigned = new HashMap<>();
        Map<UUID, Long> completedMap = new HashMap<>();
        Map<UUID, Long> inProgressMap = new HashMap<>();
        Map<UUID, Long> todoMap = new HashMap<>();
        Map<UUID, Long> overdueMap = new HashMap<>();

        // Also add project owners and members even if they have 0 tasks assigned
        for (Project p : projects) {
            if (p.getOwner() != null) {
                memberUsers.putIfAbsent(p.getOwner().getId(), p.getOwner());
            }
            List<ProjectMember> members = projectMemberRepository.findByProjectId(p.getId());
            for (ProjectMember pm : members) {
                if (pm.getUser() != null) {
                    memberUsers.putIfAbsent(pm.getUser().getId(), pm.getUser());
                }
            }
        }

        for (Task t : tasks) {
            User assignee = t.getAssignedTo();
            if (assignee != null) {
                UUID uId = assignee.getId();
                memberUsers.putIfAbsent(uId, assignee);
                totalAssigned.merge(uId, 1L, Long::sum);

                if (t.getTaskStatus() == TaskStatus.DONE) {
                    completedMap.merge(uId, 1L, Long::sum);
                } else if (t.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                    inProgressMap.merge(uId, 1L, Long::sum);
                } else if (t.getTaskStatus() == TaskStatus.TODO) {
                    todoMap.merge(uId, 1L, Long::sum);
                }

                if (t.getDueDate() != null && t.getDueDate().isBefore(now)
                        && t.getTaskStatus() != TaskStatus.DONE && t.getTaskStatus() != TaskStatus.CANCELLED) {
                    overdueMap.merge(uId, 1L, Long::sum);
                }
            }
        }

        List<MemberWorkloadDto> workloadList = new ArrayList<>();
        for (Map.Entry<UUID, User> entry : memberUsers.entrySet()) {
            UUID uId = entry.getKey();
            User u = entry.getValue();
            String name = u.getFirstName() + (u.getLastName() != null ? " " + u.getLastName() : "");

            workloadList.add(MemberWorkloadDto.builder()
                    .userId(uId)
                    .name(name)
                    .email(u.getEmail())
                    .totalAssigned(totalAssigned.getOrDefault(uId, 0L))
                    .completedTasks(completedMap.getOrDefault(uId, 0L))
                    .inProgressTasks(inProgressMap.getOrDefault(uId, 0L))
                    .todoTasks(todoMap.getOrDefault(uId, 0L))
                    .overdueTasks(overdueMap.getOrDefault(uId, 0L))
                    .build());
        }

        // Sort descending by totalAssigned
        workloadList.sort((a, b) -> Long.compare(b.getTotalAssigned(), a.getTotalAssigned()));

        return WorkloadAnalyticsResponse.builder().members(workloadList).build();
    }

    private void validateProjectAccess(Project project, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }

        String email = authentication.getName();
        boolean isOwner = project.getOwner() != null && project.getOwner().getEmail().equals(email);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserEmail(project.getId(), email);

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("You do not have access to this project's analytics");
        }
    }

    private OverdueTaskDto toOverdueDto(Task task) {
        String assignedName = task.getAssignedTo() != null
                ? task.getAssignedTo().getFirstName() + (task.getAssignedTo().getLastName() != null ? " " + task.getAssignedTo().getLastName() : "")
                : "Unassigned";

        return OverdueTaskDto.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .taskPriority(task.getTaskPriority().name())
                .taskStatus(task.getTaskStatus().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedToName(assignedName)
                .build();
    }

    private UpcomingTaskDto toUpcomingDto(Task task) {
        String assignedName = task.getAssignedTo() != null
                ? task.getAssignedTo().getFirstName() + (task.getAssignedTo().getLastName() != null ? " " + task.getAssignedTo().getLastName() : "")
                : "Unassigned";

        return UpcomingTaskDto.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .taskPriority(task.getTaskPriority().name())
                .taskStatus(task.getTaskStatus().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedToName(assignedName)
                .build();
    }

    private Map<String, Long> initStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            map.put(status.name(), 0L);
        }
        return map;
    }

    private Map<String, Long> initPriorityMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            map.put(priority.name(), 0L);
        }
        return map;
    }
}
