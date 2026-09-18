package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.GetTaskByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.dto.response.task.GetTaskResponse;
import com.Santhosh1802.ProjectManagement.service.TaskService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GetTaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        GetTaskResponse getTaskResponse = taskService.createTask(createTaskRequest);
        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "Task created successfully",
                getTaskResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<GetTaskResponse>> getTaskById(@RequestParam UUID id) {
        GetTaskByIdRequest getTaskByIdRequest = new GetTaskByIdRequest(id);
        GetTaskResponse getTaskResponse = taskService.getTaskById(getTaskByIdRequest);
        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task status updated",
                getTaskResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetTaskResponse>>> getAllTasks() {
        List<GetTaskResponse> getTaskResponseList = taskService.getAllTasks();
        ApiResponse<List<GetTaskResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks found",
                getTaskResponseList
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/by-project")
    public ResponseEntity<ApiResponse<List<GetTaskResponse>>> getTasksByProject(@RequestParam UUID projectId) {
        GetTaskByIdRequest getTaskByIdRequest = new GetTaskByIdRequest(projectId);
        List<GetTaskResponse> getTaskResponseList = taskService.getTasksByProject(getTaskByIdRequest);
        ApiResponse<List<GetTaskResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks found",
                getTaskResponseList
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
    @GetMapping("/assigned-to")
    public ResponseEntity<ApiResponse<List<GetTaskResponse>>> getTasksByAssignedToUser(@RequestParam UUID assignedToId) {
        GetTaskByIdRequest getTaskByIdRequest = new GetTaskByIdRequest(assignedToId);
        List<GetTaskResponse> getTaskResponseList = taskService.getTasksAssignedToUser(getTaskByIdRequest);
        ApiResponse<List<GetTaskResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks found",
                getTaskResponseList
                );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/created-by")
    public ResponseEntity<ApiResponse<List<GetTaskResponse>>> getTasksCreatedByUser(@RequestParam UUID createdByUserId) {
        GetTaskByIdRequest getTaskByIdRequest = new GetTaskByIdRequest(createdByUserId);
        List<GetTaskResponse> getTaskResponseList = taskService.getTasksCreatedByUser(getTaskByIdRequest);
        ApiResponse<List<GetTaskResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks found",
                getTaskResponseList
                );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/update-details")
    public ResponseEntity<ApiResponse<GetTaskResponse>> updateTaskDetails(@Valid @RequestBody UpdateTaskDetailsRequest updateTaskDetailsRequest) {
        GetTaskResponse getTaskResponse = taskService.updateTaskDetails(updateTaskDetailsRequest);
        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks updated successfully",
                getTaskResponse
                );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<GetTaskResponse>> updateTaskStatus(@Valid @RequestBody UpdateTaskStatusRequest updateTaskStatusRequest) {
        GetTaskResponse getTaskResponse = taskService.updateTaskStatus(updateTaskStatusRequest);
        ApiResponse<GetTaskResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Tasks status updated successfully",
                getTaskResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteTaskById(@RequestParam UUID id) {
        GetTaskByIdRequest getTaskByIdRequest = new GetTaskByIdRequest(id);
        Boolean deleteTaskResponse = taskService.deleteTask(getTaskByIdRequest);
        ApiResponse<Boolean> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Task deleted successfully",
                deleteTaskResponse
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }
}
