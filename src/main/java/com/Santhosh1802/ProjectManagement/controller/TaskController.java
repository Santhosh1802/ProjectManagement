package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.GetTaskByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.dto.response.task.GetTaskResponse;
import com.Santhosh1802.ProjectManagement.service.TaskService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    /**
     * Creates a new task.
     *
     * @param createTaskRequest task creation details
     * @return created task
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GetTaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest createTaskRequest
    ) {

        GetTaskResponse getTaskResponse =
                taskService.createTask(createTaskRequest);

        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "Task created successfully",
                getTaskResponse
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /**
     * Retrieves a task by its ID.
     *
     * @param id task ID
     * @return task details
     */
    @GetMapping("/id")
    public ResponseEntity<ApiResponse<GetTaskResponse>> getTaskById(
            @RequestParam UUID id
    ) {

        GetTaskResponse getTaskResponse =
                taskService.getTaskById(
                        new GetTaskByIdRequest(id)
                );

        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task found",
                getTaskResponse
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Retrieves all tasks using pagination.
     *
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated tasks
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<GetTaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<GetTaskResponse> taskResponse =
                taskService.getAllTasks(page, size);

        ApiResponse<PageResponse<GetTaskResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Tasks found",
                        taskResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Retrieves all tasks belonging to a project using pagination.
     *
     * @param projectId project ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated project tasks
     */
    @GetMapping("/by-project")
    public ResponseEntity<ApiResponse<PageResponse<GetTaskResponse>>> getTasksByProject(
            @RequestParam UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<GetTaskResponse> taskResponse =
                taskService.getTasksByProject(
                        new GetTaskByIdRequest(projectId),
                        page,
                        size
                );

        ApiResponse<PageResponse<GetTaskResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Tasks found",
                        taskResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Retrieves tasks assigned to a user using pagination.
     *
     * @param assignedToId assigned user ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated assigned tasks
     */
    @GetMapping("/assigned-to")
    public ResponseEntity<ApiResponse<PageResponse<GetTaskResponse>>> getTasksByAssignedToUser(
            @RequestParam UUID assignedToId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<GetTaskResponse> taskResponse =
                taskService.getTasksAssignedToUser(
                        new GetTaskByIdRequest(assignedToId),
                        page,
                        size
                );

        ApiResponse<PageResponse<GetTaskResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Tasks found",
                        taskResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Retrieves tasks created by a user using pagination.
     *
     * @param createdByUserId creator user ID
     * @param page zero-based page number
     * @param size number of tasks per page
     * @return paginated created tasks
     */
    @GetMapping("/created-by")
    public ResponseEntity<ApiResponse<PageResponse<GetTaskResponse>>> getTasksCreatedByUser(
            @RequestParam UUID createdByUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<GetTaskResponse> taskResponse =
                taskService.getTasksCreatedByUser(
                        new GetTaskByIdRequest(createdByUserId),
                        page,
                        size
                );

        ApiResponse<PageResponse<GetTaskResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Tasks found",
                        taskResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Updates all editable details of an existing task.
     *
     * @param updateTaskDetailsRequest updated task details
     * @return updated task
     */
    @PutMapping("/update-details")
    public ResponseEntity<ApiResponse<GetTaskResponse>> updateTaskDetails(
            @Valid @RequestBody UpdateTaskDetailsRequest updateTaskDetailsRequest
    ) {

        GetTaskResponse getTaskResponse =
                taskService.updateTaskDetails(updateTaskDetailsRequest);

        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task updated successfully",
                getTaskResponse
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Updates the status of an existing task.
     *
     * @param updateTaskStatusRequest task ID and new status
     * @return updated task
     */
    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<GetTaskResponse>> updateTaskStatus(
            @Valid @RequestBody UpdateTaskStatusRequest updateTaskStatusRequest
    ) {

        GetTaskResponse getTaskResponse =
                taskService.updateTaskStatus(updateTaskStatusRequest);

        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task status updated successfully",
                getTaskResponse
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Deletes a task by its ID.
     *
     * @param id task ID
     * @return deletion result
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteTaskById(
            @RequestParam UUID id
    ) {

        Boolean deleteTaskResponse =
                taskService.deleteTask(
                        new GetTaskByIdRequest(id)
                );

        ApiResponse<Boolean> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task deleted successfully",
                deleteTaskResponse
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}