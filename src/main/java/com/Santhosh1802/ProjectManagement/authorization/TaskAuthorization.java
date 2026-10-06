package com.Santhosh1802.ProjectManagement.authorization;

import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("taskAuthorization")
public class TaskAuthorization {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserAuthorization userAuthorization;

    public TaskAuthorization(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserAuthorization userAuthorization
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userAuthorization = userAuthorization;
    }


    /**
     * Checks whether the authenticated user can view a task.
     *
     * <p>A user can view a task when they are the project owner,
     * a project member, or the user assigned to the task.</p>
     *
     * @param taskId task ID
     * @param authentication current authentication
     * @return true when the user can view the task
     */
    public boolean canViewTask(
            UUID taskId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        String email = authentication.getName();

        return taskRepository.findById(taskId)
                .map(Task::getProject)
                .map(project ->
                        projectRepository.findById(project.getId())
                                .map(projectEntity ->
                                        projectEntity.getOwner()
                                                .getEmail()
                                                .equals(email)
                                )
                                .orElse(false)
                                ||
                                projectMemberRepository
                                        .existsByProjectIdAndUserEmail(
                                                project.getId(),
                                                email
                                        )
                )
                .orElse(false)
                ||
                taskRepository.findById(taskId)
                        .map(Task::getAssignedTo)
                        .map(user -> user.getEmail().equals(email))
                        .orElse(false);
    }


    /**
     * Checks whether the authenticated user can manage tasks
     * within a project.
     *
     * <p>Project owners and project managers can manage tasks.</p>
     *
     * @param projectId project ID
     * @param authentication current authentication
     * @return true when the user can manage tasks
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
                .map(project ->
                        project.getOwner()
                                .getEmail()
                                .equals(email)
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
     * Checks whether the authenticated user can update
     * the status of a task.
     *
     * <p>Project owners, project managers, and the user assigned
     * to the task can update its status.</p>
     *
     * @param taskId task ID
     * @param authentication current authentication
     * @return true when the user can update the task status
     */
    public boolean canUpdateStatus(
            UUID taskId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        String email = authentication.getName();

        return taskRepository.findById(taskId)
                .map(task -> {

                    if (task.getAssignedTo() != null
                            && task.getAssignedTo()
                            .getEmail()
                            .equals(email)) {
                        return true;
                    }

                    return canManageProject(
                            task.getProject().getId(),
                            authentication
                    );
                })
                .orElse(false);
    }


    /**
     * Checks whether the authenticated user can view
     * tasks belonging to a project.
     *
     * @param projectId project ID
     * @param authentication current authentication
     * @return true when the user can view project tasks
     */
    public boolean canViewProjectTasks(
            UUID projectId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        String email = authentication.getName();

        boolean owner = projectRepository.findById(projectId)
                .map(project ->
                        project.getOwner()
                                .getEmail()
                                .equals(email)
                )
                .orElse(false);

        if (owner) {
            return true;
        }

        return projectMemberRepository
                .existsByProjectIdAndUserEmail(
                        projectId,
                        email
                );
    }


    /**
     * Checks whether the authenticated user can view
     * tasks assigned to a user.
     *
     * <p>Administrators can view any user's assigned tasks.
     * A normal user can only view their own assigned tasks.</p>
     *
     * @param userId user ID
     * @param authentication current authentication
     * @return true when access is allowed
     */
    public boolean canViewAssignedTasks(
            UUID userId,
            Authentication authentication
    ) {

        return userAuthorization.isCurrentUser(
                userId,
                authentication
        );
    }


    /**
     * Checks whether the authenticated user can view
     * tasks created by a user.
     *
     * <p>Administrators can view any user's created tasks.
     * A normal user can only view their own created tasks.</p>
     *
     * @param userId user ID
     * @param authentication current authentication
     * @return true when access is allowed
     */
    public boolean canViewCreatedTasks(
            UUID userId,
            Authentication authentication
    ) {

        return userAuthorization.isCurrentUser(
                userId,
                authentication
        );
    }


    /**
     * Checks whether the authenticated user is the specified user.
     *
     * @param userId user ID
     * @param authentication current authentication
     * @return true when the authenticated user owns the ID
     */
    private boolean isCurrentUser(
            UUID userId,
            Authentication authentication
    ) {

        return userAuthorization.isCurrentUser(
                userId,
                authentication
        );
    }


    /**
     * Checks whether an authentication object represents
     * an authenticated user.
     *
     * @param authentication current authentication
     * @return true when authenticated
     */
    private boolean isAuthenticated(
            Authentication authentication
    ) {

        return authentication != null
                && authentication.isAuthenticated();
    }
    /**
     * Checks whether the authenticated user can manage a task.
     *
     * <p>Project owners and project managers can manage tasks.</p>
     *
     * @param taskId task ID
     * @param authentication current authentication
     * @return true when the user can manage the task
     */
    public boolean canManageTask(
            UUID taskId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        return taskRepository.findById(taskId)
                .map(task ->
                        canManageProject(
                                task.getProject().getId(),
                                authentication
                        )
                )
                .orElse(false);
    }
}