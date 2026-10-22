package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.dto.response.task.GetTaskResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.service.TaskService;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private UUID taskId;
    private UUID projectId;
    private UUID assignedToId;
    private UUID createdById;

    private GetTaskResponse taskResponse;

    @BeforeEach
    void setUp() {

        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        assignedToId = UUID.randomUUID();
        createdById = UUID.randomUUID();

        Instant dueDate =
                Instant.parse("2026-12-31T10:00:00Z");

        Instant createdAt =
                Instant.parse("2026-10-01T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-02T10:00:00Z");

        ProjectOwnerResponse ownerResponse =
                new ProjectOwnerResponse(
                        UUID.randomUUID(),
                        "Project",
                        "Owner",
                        "owner@example.com"
                );

        GetProjectResponse projectResponse =
                new GetProjectResponse(
                        projectId,
                        "DevFlow",
                        "Project management application",
                        ProjectStatus.ACTIVE,
                        createdAt,
                        dueDate,
                        ownerResponse,
                        createdAt,
                        updatedAt
                );

        GetUserResponse assignedToResponse =
                new GetUserResponse(
                        assignedToId,
                        "Assigned",
                        "User",
                        "assigned@example.com",
                        null,
                        true,
                        true,
                        createdAt,
                        updatedAt,
                        null
                );

        GetUserResponse createdByResponse =
                new GetUserResponse(
                        createdById,
                        "Created",
                        "User",
                        "creator@example.com",
                        null,
                        true,
                        true,
                        createdAt,
                        updatedAt,
                        null
                );

        taskResponse =
                new GetTaskResponse(
                        taskId,
                        "Implement authentication",
                        "Implement session based authentication",
                        TaskStatus.IN_PROGRESS,
                        TaskPriority.HIGH,
                        dueDate,
                        projectResponse,
                        assignedToResponse,
                        createdByResponse,
                        createdAt,
                        updatedAt
                );
    }

    @Test
    void createTask_shouldReturnCreated() throws Exception {

        when(taskService.createTask(any(CreateTaskRequest.class), any()))
                .thenReturn(taskResponse);

        mockMvc.perform(
                        post("/api/tasks/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "title": "Implement authentication",
                                      "description": "Implement session based authentication",
                                      "taskStatus": "TODO",
                                      "taskPriority": "HIGH",
                                      "dueDate": "2026-12-31T10:00:00Z",
                                      "projectId": "%s",
                                      "assignedToId": "%s",
                                      "createdById": "%s"
                                    }
                                    """.formatted(
                                        projectId,
                                        assignedToId,
                                        createdById
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Task created successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(taskId.toString()))
                .andExpect(jsonPath("$.data.title")
                        .value("Implement authentication"));

        verify(taskService)
                .createTask(any(CreateTaskRequest.class), any());
    }
    @Test
    void getTaskById_shouldReturnOk() throws Exception {

        when(taskService.getTaskById(any()))
                .thenReturn(taskResponse);

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", taskId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Task found"))
                .andExpect(jsonPath("$.data.id")
                        .value(taskId.toString()))
                .andExpect(jsonPath("$.data.title")
                        .value("Implement authentication"));

        verify(taskService)
                .getTaskById(any());
    }

    @Test
    void getAllTasks_shouldReturnPaginatedTasks() throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(taskService.getAllTasks(0, 10))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/all")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Tasks found"))
                .andExpect(jsonPath("$.data.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.data.page")
                        .value(0))
                .andExpect(jsonPath("$.data.size")
                        .value(10))
                .andExpect(jsonPath("$.data.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.data.totalPages")
                        .value(1))
                .andExpect(jsonPath("$.data.first")
                        .value(true))
                .andExpect(jsonPath("$.data.last")
                        .value(true));

        verify(taskService)
                .getAllTasks(0, 10);
    }

    @Test
    void getAllTasks_shouldUseDefaultPagination() throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(taskService.getAllTasks(0, 10))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/all")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page")
                        .value(0))
                .andExpect(jsonPath("$.data.size")
                        .value(10));

        verify(taskService)
                .getAllTasks(0, 10);
    }

    @Test
    void getTasksByProject_shouldReturnPaginatedTasks() throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        1,
                        5,
                        6,
                        2,
                        false,
                        true
                );

        when(taskService.getTasksByProject(
                any(),
                eq(1),
                eq(5),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/by-project")
                                .param("projectId", projectId.toString())
                                .param("page", "1")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Tasks found"))
                .andExpect(jsonPath("$.data.page")
                        .value(1))
                .andExpect(jsonPath("$.data.size")
                        .value(5))
                .andExpect(jsonPath("$.data.totalElements")
                        .value(6))
                .andExpect(jsonPath("$.data.totalPages")
                        .value(2));

        verify(taskService)
                .getTasksByProject(any(), eq(1), eq(5), any());
    }

    @Test
    void getTasksByProject_shouldUseDefaultPagination() throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(taskService.getTasksByProject(
                any(),
                eq(0),
                eq(10),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/by-project")
                                .param("projectId", projectId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page")
                        .value(0))
                .andExpect(jsonPath("$.data.size")
                        .value(10));

        verify(taskService)
                .getTasksByProject(any(), eq(0), eq(10), any());
    }

    @Test
    void getTasksByAssignedToUser_shouldReturnPaginatedTasks()
            throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(taskService.getTasksAssignedToUser(
                any(),
                eq(0),
                eq(10)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/assigned-to")
                                .param(
                                        "assignedToId",
                                        assignedToId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Tasks found"))
                .andExpect(jsonPath("$.data.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.data.content[0].id")
                        .value(taskId.toString()));

        verify(taskService)
                .getTasksAssignedToUser(any(), eq(0), eq(10));
    }

    @Test
    void getTasksCreatedByUser_shouldReturnPaginatedTasks()
            throws Exception {

        PageResponse<GetTaskResponse> pageResponse =
                new PageResponse<>(
                        List.of(taskResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(taskService.getTasksCreatedByUser(
                any(),
                eq(0),
                eq(10)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/tasks/created-by")
                                .param(
                                        "createdByUserId",
                                        createdById.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Tasks found"))
                .andExpect(jsonPath("$.data.content.length()")
                        .value(1));

        verify(taskService)
                .getTasksCreatedByUser(any(), eq(0), eq(10));
    }

    @Test
    void updateTaskDetails_shouldReturnOk() throws Exception {

        when(taskService.updateTaskDetails(
                any(UpdateTaskDetailsRequest.class)
        )).thenReturn(taskResponse);

        mockMvc.perform(
                        put("/api/tasks/update-details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "title": "Updated task",
                                          "description": "Updated task description",
                                          "taskStatus": "DONE",
                                          "taskPriority": "URGENT",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        taskId,
                                        projectId,
                                        assignedToId,
                                        createdById
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Task updated successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(taskId.toString()));

        verify(taskService)
                .updateTaskDetails(
                        any(UpdateTaskDetailsRequest.class)
                );
    }

    @Test
    void updateTaskStatus_shouldReturnOk() throws Exception {

        when(taskService.updateTaskStatus(
                any(UpdateTaskStatusRequest.class)
        )).thenReturn(taskResponse);

        mockMvc.perform(
                        put("/api/tasks/update-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "status": "DONE"
                                        }
                                        """.formatted(taskId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Task status updated successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(taskId.toString()));

        verify(taskService)
                .updateTaskStatus(
                        any(UpdateTaskStatusRequest.class)
                );
    }

    @Test
    void deleteTaskById_shouldReturnOk() throws Exception {

        when(taskService.deleteTask(any()))
                .thenReturn(true);

        mockMvc.perform(
                        delete("/api/tasks/delete")
                                .param("id", taskId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Task deleted successfully"))
                .andExpect(jsonPath("$.data")
                        .value(true));

        verify(taskService)
                .deleteTask(any());
    }

    @Test
    void createTask_shouldReturnBadRequestForInvalidRequest()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "",
                                          "description": "",
                                          "taskStatus": null,
                                          "taskPriority": null,
                                          "dueDate": null,
                                          "projectId": null,
                                          "assignedToId": null,
                                          "createdById": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .createTask(any());
    }

    @Test
    void updateTaskDetails_shouldReturnBadRequestForInvalidRequest()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": null,
                                          "title": "",
                                          "description": "",
                                          "taskStatus": null,
                                          "taskPriority": null,
                                          "dueDate": null,
                                          "projectId": null,
                                          "assignedToId": null,
                                          "createdById": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .updateTaskDetails(any());
    }

    @Test
    void updateTaskStatus_shouldReturnBadRequestForInvalidRequest()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": null,
                                          "status": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .updateTaskStatus(any());
    }

    @Test
    void getTaskById_shouldReturnBadRequestWhenIdMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTaskById(any());
    }

    @Test
    void getTasksByProject_shouldReturnBadRequestWhenProjectIdMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/by-project")
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTasksByProject(any(), anyInt(), anyInt());
    }

    @Test
    void getTasksByAssignedToUser_shouldReturnBadRequestWhenIdMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/assigned-to")
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTasksAssignedToUser(any(), anyInt(), anyInt());
    }

    @Test
    void getTasksCreatedByUser_shouldReturnBadRequestWhenIdMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/created-by")
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .getTasksCreatedByUser(any(), anyInt(), anyInt());
    }

    @Test
    void deleteTaskById_shouldReturnBadRequestWhenIdMissing()
            throws Exception {

        mockMvc.perform(
                        delete("/api/tasks/delete")
                )
                .andExpect(status().isBadRequest());

        verify(taskService, never())
                .deleteTask(any());
    }
}
