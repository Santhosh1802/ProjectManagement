package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.project.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectService;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    private final UUID projectId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProjectService projectService;

    private GetProjectResponse createProjectResponse() {
        return new GetProjectResponse(
                projectId,
                "DevFlow",
                "Project management application",
                ProjectStatus.PLANNING,
                Instant.parse("2026-10-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                null,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void createProject_shouldReturnCreated() throws Exception {

        GetProjectResponse response = createProjectResponse();

        when(projectService.createProject(
                any(CreateProjectRequest.class),
                any()
        )).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "DevFlow",
                                "description": "Project management application",
                                "startDate": "2026-10-01T00:00:00Z",
                                "dueDate": "2026-12-31T00:00:00Z"
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).createProject(
                any(CreateProjectRequest.class),
                any()
        );
    }

    @Test
    void getProjectById_shouldReturnOk() throws Exception {

        GetProjectResponse response = createProjectResponse();

        when(projectService.getProjectById(any(GetProjectByIdRequest.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/projects/id")
                        .param("id", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).getProjectById(
                any(GetProjectByIdRequest.class)
        );
    }

    @Test
    void getProjectById_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(get("/api/projects/id")
                        .param("id", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    void getProjects_shouldReturnOk() throws Exception {

        PageResponse<GetProjectResponse> pageResponse =
                new PageResponse<>(
                        List.of(createProjectResponse()),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(projectService.searchProjects(
                any(GetProjectSearchRequest.class),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/projects")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).searchProjects(
                any(GetProjectSearchRequest.class),
                any()
        );
    }

    @Test
    void updateProjectDetails_shouldReturnOk() throws Exception {

        GetProjectResponse response = createProjectResponse();

        when(projectService.updateProjectDetails(
                any(UpdateProjectDetailsRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/projects/update/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "id": "%s",
                                "name": "DevFlow Updated",
                                "description": "Updated description",
                                "projectStatus": "ACTIVE",
                                "dueDate": "2026-12-31T00:00:00Z"
                            }
                        """.formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).updateProjectDetails(
                any(UpdateProjectDetailsRequest.class)
        );
    }

    @Test
    void updateProjectOwner_shouldReturnOk() throws Exception {

        GetProjectResponse response = createProjectResponse();

        when(projectService.updateProjectOwner(
                any(UpdateProjectOwnerRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/projects/update/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "%s",
                                    "ownerId": "%s"
                                }
                                """.formatted(projectId, ownerId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).updateProjectOwner(
                any(UpdateProjectOwnerRequest.class)
        );
    }

    @Test
    void deleteProject_shouldReturnOk() throws Exception {

        when(projectService.deleteProject(
                any(DeleteProjectRequest.class)
        )).thenReturn(true);

        mockMvc.perform(delete("/api/projects/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "%s"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(projectService).deleteProject(
                any(DeleteProjectRequest.class)
        );
    }
}