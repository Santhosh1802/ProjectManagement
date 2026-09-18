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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private static @NonNull GetUserResponse getCreatedByResponse(Task task) {
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

    private static @NonNull GetUserResponse getAssignedToResponse(Task task) {
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

    private static @NonNull GetProjectResponse getProjectResponse(Task task) {
        Project project = task.getProject();
        return new GetProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectStatus(),
                project.getStartDate(),
                project.getDueDate(),
                new ProjectOwnerResponse(project.getOwner().getId(),
                        project.getOwner().getFirstName(),
                        project.getOwner().getLastName(),
                        project.getOwner().getEmail()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

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

    @Transactional(rollbackFor = {ProjectNotFoundException.class, UserNotFoundException.class})
    public GetTaskResponse createTask(CreateTaskRequest createTaskRequest) {
        Project project = projectRepository.findById(createTaskRequest.getProjectId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id : " + createTaskRequest.getProjectId() + " not found")
        );
        User assignedTo = userRepository.findById(createTaskRequest.getAssignedToId()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + createTaskRequest.getAssignedToId() + " not found")
        );
        User createdBy = userRepository.findById(createTaskRequest.getCreatedById()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + createTaskRequest.getCreatedById() + " not found")
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
        return returnGetTaskResponse(taskRepository.save(task));
    }

    public GetTaskResponse getTaskById(GetTaskByIdRequest getTaskByIdRequest) {
        Task task = taskRepository.findById(getTaskByIdRequest.getId()).orElseThrow(() ->
                new TaskNotFoundException("Task of id : " + getTaskByIdRequest.getId() + " not found")
        );
        return returnGetTaskResponse(task);
    }

    public List<GetTaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        List<GetTaskResponse> taskResponseList = new ArrayList<>();
        for (Task task : tasks) {
            GetTaskResponse taskResponse = returnGetTaskResponse(task);
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    public List<GetTaskResponse> getTasksByProject(GetTaskByIdRequest getTaskByIdRequest) {
        Project project = projectRepository.findById(getTaskByIdRequest.getId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id : " + getTaskByIdRequest.getId() + " not found")
        );
        List<Task> tasks = taskRepository.findByProject(project);
        List<GetTaskResponse> taskResponseList = new ArrayList<>();
        for (Task task : tasks) {
            GetTaskResponse taskResponse = returnGetTaskResponse(task);
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    public List<GetTaskResponse> getTasksAssignedToUser(GetTaskByIdRequest getTaskByIdRequest) {
        User user = userRepository.findById(getTaskByIdRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + getTaskByIdRequest.getId() + " not found"));
        List<Task> tasks = taskRepository.findByAssignedTo(user);
        List<GetTaskResponse> taskResponseList = new ArrayList<>();
        for (Task task : tasks) {
            GetTaskResponse taskResponse = returnGetTaskResponse(task);
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    public List<GetTaskResponse> getTasksCreatedByUser(GetTaskByIdRequest getTaskByIdRequest) {
        User user = userRepository.findById(getTaskByIdRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + getTaskByIdRequest.getId() + " not found"));
        List<Task> tasks = taskRepository.findByCreatedBy(user);
        List<GetTaskResponse> taskResponseList = new ArrayList<>();
        for (Task task : tasks) {
            GetTaskResponse taskResponse = returnGetTaskResponse(task);
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    @Transactional(rollbackFor = {TaskNotFoundException.class, ProjectNotFoundException.class, UserNotFoundException.class})
    public GetTaskResponse updateTaskDetails(UpdateTaskDetailsRequest updateTaskDetailsRequest) {
        Task task = taskRepository.findById(updateTaskDetailsRequest.getId()).orElseThrow(() ->
                new TaskNotFoundException("Task of id : " + updateTaskDetailsRequest.getId() + " not found")
        );
        Project project = projectRepository.findById(updateTaskDetailsRequest.getProjectId()).orElseThrow(() ->
                new ProjectNotFoundException("Project of id : " + updateTaskDetailsRequest.getId() + " not found")
        );
        User assignedTo = userRepository.findById(updateTaskDetailsRequest.getAssignedToId()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + updateTaskDetailsRequest.getId() + " not found")
        );
        User createdBy = userRepository.findById(updateTaskDetailsRequest.getCreatedById()).orElseThrow(() ->
                new UserNotFoundException("User of id : " + updateTaskDetailsRequest.getId() + " not found")
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

    @Transactional(rollbackFor = {TaskNotFoundException.class})
    public GetTaskResponse updateTaskStatus(UpdateTaskStatusRequest updateTaskStatusRequest) {
        Task task = taskRepository.findById(updateTaskStatusRequest.getId()).orElseThrow(() ->
                new TaskNotFoundException("Task of id : " + updateTaskStatusRequest.getId() + " not found")
        );
        task.setTaskStatus(updateTaskStatusRequest.getStatus());
        taskRepository.save(task);
        return returnGetTaskResponse(task);
    }

    @Transactional(rollbackFor = {TaskNotFoundException.class})
    public boolean deleteTask(GetTaskByIdRequest getTaskByIdRequest) {
        Task task = taskRepository.findById(getTaskByIdRequest.getId()).orElseThrow(() ->
                new TaskNotFoundException("Task of id : " + getTaskByIdRequest.getId() + " not found")
        );
        taskRepository.delete(task);
        return true;
    }
}
