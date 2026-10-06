package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.GetTaskByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.dto.response.task.GetTaskResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.task.TaskNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Converts a Task entity's creator into a GetUserResponse.
     *
     * @param task task containing the creator
     * @return creator information
     */
    private GetUserResponse getCreatedByResponse(Task task) {

        User createdBy = task.getCreatedBy();

        return new GetUserResponse(
                createdBy.getId(),
                createdBy.getFirstName(),
                createdBy.getLastName(),
                createdBy.getEmail(),
                createdBy.getUserRole(),
                createdBy.getIsActive(),
                createdBy.getEmailVerified(),
                createdBy.getCreatedAt(),
                createdBy.getUpdatedAt(),
                createdBy.getLastLoginAt()
        );
    }


    /**
     * Converts a Task entity's assigned user into a GetUserResponse.
     *
     * @param task task containing the assigned user
     * @return assigned user information
     */
    private GetUserResponse getAssignedToResponse(Task task) {

        User assignedTo = task.getAssignedTo();

        return new GetUserResponse(
                assignedTo.getId(),
                assignedTo.getFirstName(),
                assignedTo.getLastName(),
                assignedTo.getEmail(),
                assignedTo.getUserRole(),
                assignedTo.getIsActive(),
                assignedTo.getEmailVerified(),
                assignedTo.getCreatedAt(),
                assignedTo.getUpdatedAt(),
                assignedTo.getLastLoginAt()
        );
    }


    /**
     * Converts a Task entity's project into a GetProjectResponse.
     *
     * @param task task containing the project
     * @return project information
     */
    private GetProjectResponse getProjectResponse(Task task) {

        Project project = task.getProject();

        return new GetProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectStatus(),
                project.getStartDate(),
                project.getDueDate(),
                new ProjectOwnerResponse(
                        project.getOwner().getId(),
                        project.getOwner().getFirstName(),
                        project.getOwner().getLastName(),
                        project.getOwner().getEmail()
                ),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }


    /**
     * Converts a Task entity into a GetTaskResponse.
     *
     * @param task task entity
     * @return task response
     */
    private GetTaskResponse returnGetTaskResponse(Task task) {

        GetProjectResponse projectResponse = getProjectResponse(task);
        GetUserResponse assignedToResponse = getAssignedToResponse(task);
        GetUserResponse createdByResponse = getCreatedByResponse(task);

        return new GetTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskStatus(),
                task.getTaskPriority(),
                task.getDueDate(),
                projectResponse,
                assignedToResponse,
                createdByResponse,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }


    /**
     * Converts a page of Task entities into a paginated task response.
     *
     * @param taskPage page containing Task entities
     * @return paginated task response
     */
    private PageResponse<GetTaskResponse> createPageResponse(
            Page<Task> taskPage
    ) {

        return new PageResponse<>(
                taskPage.getContent()
                        .stream()
                        .map(this::returnGetTaskResponse)
                        .toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isFirst(),
                taskPage.isLast()
        );
    }


    /**
     * Creates a new task.
     *
     * @param createTaskRequest task creation details
     * @return created task
     * @throws ProjectNotFoundException if the project does not exist
     * @throws UserNotFoundException if the assigned user or creator does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canManageProject(
        #createTaskRequest.projectId,
        authentication
    )
    """)
    @Transactional
    public GetTaskResponse createTask(
            @P("createTaskRequest")
            CreateTaskRequest createTaskRequest
    ) {

        Project project = projectRepository
                .findById(createTaskRequest.getProjectId())
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project of id : "
                                        + createTaskRequest.getProjectId()
                                        + " not found"
                        )
                );

        User assignedTo = userRepository
                .findById(createTaskRequest.getAssignedToId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + createTaskRequest.getAssignedToId()
                                        + " not found"
                        )
                );

        User createdBy = userRepository
                .findById(createTaskRequest.getCreatedById())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + createTaskRequest.getCreatedById()
                                        + " not found"
                        )
                );

        Task task = new Task();

        task.setTitle(createTaskRequest.getTitle());
        task.setDescription(createTaskRequest.getDescription());
        task.setTaskStatus(createTaskRequest.getTaskStatus());
        task.setTaskPriority(createTaskRequest.getTaskPriority());
        task.setDueDate(createTaskRequest.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);

        return returnGetTaskResponse(
                taskRepository.save(task)
        );
    }


    /**
     * Retrieves a task by its ID.
     *
     * @param getTaskByIdRequest request containing task ID
     * @return task details
     * @throws TaskNotFoundException if the task does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canViewTask(
        #getTaskByIdRequest.id,
        authentication
    )
    """)
    public GetTaskResponse getTaskById(
            @P("getTaskByIdRequest")
            GetTaskByIdRequest getTaskByIdRequest
    ) {

        Task task = taskRepository
                .findById(getTaskByIdRequest.getId())
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task of id : "
                                        + getTaskByIdRequest.getId()
                                        + " not found"
                        )
                );

        return returnGetTaskResponse(task);
    }


    /**
     * Retrieves all tasks using pagination.
     *
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated task response
     */
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<GetTaskResponse> getAllTasks(
            int page,
            int size
    ) {

        Page<Task> tasks = taskRepository.findAll(
                PageRequest.of(page, size)
        );

        return createPageResponse(tasks);
    }


    /**
     * Retrieves all tasks belonging to a project using pagination.
     *
     * @param getTaskByIdRequest request containing project ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated task response
     * @throws ProjectNotFoundException if the project does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canViewProjectTasks(
        #getTaskByIdRequest.id,
        authentication
    )
    """)
    public PageResponse<GetTaskResponse> getTasksByProject(
            @P("getTaskByIdRequest")
            GetTaskByIdRequest getTaskByIdRequest,
            int page,
            int size
    ) {

        Project project = projectRepository
                .findById(getTaskByIdRequest.getId())
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project of id : "
                                        + getTaskByIdRequest.getId()
                                        + " not found"
                        )
                );

        Page<Task> tasks = taskRepository.findByProject(
                project,
                PageRequest.of(page, size)
        );

        return createPageResponse(tasks);
    }


    /**
     * Retrieves tasks assigned to a user using pagination.
     *
     * @param getTaskByIdRequest request containing user ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated task response
     * @throws UserNotFoundException if the user does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canViewAssignedTasks(
        #getTaskByIdRequest.id,
        authentication
    )
    """)
    public PageResponse<GetTaskResponse> getTasksAssignedToUser(
            @P("getTaskByIdRequest")
            GetTaskByIdRequest getTaskByIdRequest,
            int page,
            int size
    ) {

        User user = userRepository
                .findById(getTaskByIdRequest.getId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + getTaskByIdRequest.getId()
                                        + " not found"
                        )
                );

        Page<Task> tasks = taskRepository.findByAssignedTo(
                user,
                PageRequest.of(page, size)
        );

        return createPageResponse(tasks);
    }


    /**
     * Retrieves tasks created by a user using pagination.
     *
     * @param getTaskByIdRequest request containing user ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated task response
     * @throws UserNotFoundException if the user does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canViewCreatedTasks(
        #getTaskByIdRequest.id,
        authentication
    )
    """)
    public PageResponse<GetTaskResponse> getTasksCreatedByUser(
            @P("getTaskByIdRequest")
            GetTaskByIdRequest getTaskByIdRequest,
            int page,
            int size
    ) {

        User user = userRepository
                .findById(getTaskByIdRequest.getId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + getTaskByIdRequest.getId()
                                        + " not found"
                        )
                );

        Page<Task> tasks = taskRepository.findByCreatedBy(
                user,
                PageRequest.of(page, size)
        );

        return createPageResponse(tasks);
    }


    /**
     * Updates the complete details of an existing task.
     *
     * @param updateTaskDetailsRequest updated task details
     * @return updated task
     * @throws TaskNotFoundException if the task does not exist
     * @throws ProjectNotFoundException if the project does not exist
     * @throws UserNotFoundException if the assigned user or creator does not exist
     */

    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canManageTask(
        #updateTaskDetailsRequest.id,
        authentication
    )
    """)
    @Transactional
    public GetTaskResponse updateTaskDetails(
            @P("updateTaskDetailsRequest")
            UpdateTaskDetailsRequest updateTaskDetailsRequest
    ) {

        Task task = taskRepository
                .findById(updateTaskDetailsRequest.getId())
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task of id : "
                                        + updateTaskDetailsRequest.getId()
                                        + " not found"
                        )
                );

        Project project = projectRepository
                .findById(updateTaskDetailsRequest.getProjectId())
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project of id : "
                                        + updateTaskDetailsRequest.getProjectId()
                                        + " not found"
                        )
                );

        User assignedTo = userRepository
                .findById(updateTaskDetailsRequest.getAssignedToId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + updateTaskDetailsRequest.getAssignedToId()
                                        + " not found"
                        )
                );

        User createdBy = userRepository
                .findById(updateTaskDetailsRequest.getCreatedById())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User of id : "
                                        + updateTaskDetailsRequest.getCreatedById()
                                        + " not found"
                        )
                );

        task.setTitle(updateTaskDetailsRequest.getTitle());
        task.setDescription(updateTaskDetailsRequest.getDescription());
        task.setTaskStatus(updateTaskDetailsRequest.getTaskStatus());
        task.setTaskPriority(updateTaskDetailsRequest.getTaskPriority());
        task.setDueDate(updateTaskDetailsRequest.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);

        taskRepository.save(task);

        return returnGetTaskResponse(task);
    }


    /**
     * Updates the status of an existing task.
     *
     * @param updateTaskStatusRequest request containing task ID and new status
     * @return updated task
     * @throws TaskNotFoundException if the task does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canUpdateStatus(
        #updateTaskStatusRequest.id,
        authentication
    )
    """)
    @Transactional
    public GetTaskResponse updateTaskStatus(
            @P("updateTaskStatusRequest")
            UpdateTaskStatusRequest updateTaskStatusRequest
    ) {

        Task task = taskRepository
                .findById(updateTaskStatusRequest.getId())
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task of id : "
                                        + updateTaskStatusRequest.getId()
                                        + " not found"
                        )
                );

        task.setTaskStatus(updateTaskStatusRequest.getStatus());

        taskRepository.save(task);

        return returnGetTaskResponse(task);
    }


    /**
     * Deletes a task by its ID.
     *
     * @param getTaskByIdRequest request containing task ID
     * @return true when the task is successfully deleted
     * @throws TaskNotFoundException if the task does not exist
     */
    @PreAuthorize("""
    hasRole('ADMIN')
    or @taskAuthorization.canManageTask(
        #getTaskByIdRequest.id,
        authentication
    )
    """)
    @Transactional
    public boolean deleteTask(
            GetTaskByIdRequest getTaskByIdRequest
    ) {

        Task task = taskRepository
                .findById(getTaskByIdRequest.getId())
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task of id : "
                                        + getTaskByIdRequest.getId()
                                        + " not found"
                        )
                );

        taskRepository.delete(task);

        return true;
    }
}