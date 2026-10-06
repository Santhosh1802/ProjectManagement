package com.Santhosh1802.ProjectManagement.controller;


import com.Santhosh1802.ProjectManagement.dto.request.projectMember.CreateProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.DeleteProjectMemberRequest;
import com.Santhosh1802.ProjectManagement.dto.request.projectMember.UpdateProjectMemberRoleRequest;
import com.Santhosh1802.ProjectManagement.dto.response.projectMember.GetProjectMemberResponse;
import com.Santhosh1802.ProjectManagement.service.ProjectMemberService;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    private final UUID projectMemberId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // createProjectMember()
    // -------------------------------------------------------------------------

    @Test
    void createProjectMember_shouldReturn201_whenRequestIsValid()
            throws Exception {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        userId,
                        projectId,
                        ProjectRole.MEMBER
                );

        GetProjectMemberResponse serviceResponse =
                new GetProjectMemberResponse();

        when(projectMemberService.addMember(any(CreateProjectMemberRequest.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/project-member/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(projectMemberService)
                .addMember(any(CreateProjectMemberRequest.class));
    }

    @Test
    void createProjectMember_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        CreateProjectMemberRequest request =
                new CreateProjectMemberRequest(
                        null,
                        projectId,
                        ProjectRole.MEMBER
                );

        mockMvc.perform(
                        post("/api/project-member/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .addMember(any(CreateProjectMemberRequest.class));
    }

    // -------------------------------------------------------------------------
    // getMemberById()
    // -------------------------------------------------------------------------

    @Test
    void getMemberById_shouldReturn200_whenIdIsValid()
            throws Exception {

        GetProjectMemberResponse serviceResponse =
                new GetProjectMemberResponse();

        when(projectMemberService.getMemberById(any()))
                .thenReturn(serviceResponse);

        mockMvc.perform(
                        get("/api/project-member/id")
                                .param("id", projectMemberId.toString())
                )
                .andExpect(status().isOk());

        ArgumentCaptor<com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest>
                captor =
                ArgumentCaptor.forClass(
                        com.Santhosh1802.ProjectManagement.dto.request.projectMember.GetByIdRequest.class
                );

        verify(projectMemberService)
                .getMemberById(captor.capture());

        assertEquals(
                projectMemberId,
                captor.getValue().getId()
        );
    }

    @Test
    void getMemberById_shouldReturn400_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/project-member/id")
                                .param("id", "invalid-uuid")
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .getMemberById(any());
    }

    // -------------------------------------------------------------------------
    // getMembersByProject()
    // -------------------------------------------------------------------------

    @Test
    void getMembersByProject_shouldReturn200_withDefaultPagination()
            throws Exception {

        PageResponse<GetProjectMemberResponse> pageResponse =
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0,
                        true,
                        true
                );

        when(projectMemberService.getMembersByProject(
                any(),
                eq(0),
                eq(10)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", projectId.toString())
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .getMembersByProject(
                        any(),
                        eq(0),
                        eq(10)
                );
    }

    @Test
    void getMembersByProject_shouldPassPaginationParameters()
            throws Exception {

        PageResponse<GetProjectMemberResponse> pageResponse =
                new PageResponse<>(
                        List.of(),
                        2,
                        20,
                        3,
                        50,
                        false,
                        false
                );

        when(projectMemberService.getMembersByProject(
                any(),
                eq(2),
                eq(20)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", projectId.toString())
                                .param("page", "2")
                                .param("size", "20")
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .getMembersByProject(
                        any(),
                        eq(2),
                        eq(20)
                );
    }

    @Test
    void getMembersByProject_shouldReturn400_whenProjectIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", "invalid-uuid")
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .getMembersByProject(
                        any(),
                        anyInt(),
                        anyInt()
                );
    }

    // -------------------------------------------------------------------------
    // getProjectsByUser()
    // -------------------------------------------------------------------------

    @Test
    void getProjectsByUser_shouldReturn200_withDefaultPagination()
            throws Exception {

        PageResponse<GetProjectMemberResponse> pageResponse =
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0,
                        true,
                        true
                );

        when(projectMemberService.getProjectsByUser(
                any(),
                eq(0),
                eq(10)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/project-member/member")
                                .param("id", userId.toString())
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .getProjectsByUser(
                        any(),
                        eq(0),
                        eq(10)
                );
    }

    @Test
    void getProjectsByUser_shouldPassPaginationParameters()
            throws Exception {

        PageResponse<GetProjectMemberResponse> pageResponse =
                new PageResponse<>(
                        List.of(),
                        1,
                        5,
                        2,
                        8,
                        false,
                        false
                );

        when(projectMemberService.getProjectsByUser(
                any(),
                eq(1),
                eq(5)
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/project-member/member")
                                .param("id", userId.toString())
                                .param("page", "1")
                                .param("size", "5")
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .getProjectsByUser(
                        any(),
                        eq(1),
                        eq(5)
                );
    }

    @Test
    void getProjectsByUser_shouldReturn400_whenUserIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/project-member/member")
                                .param("id", "invalid-uuid")
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .getProjectsByUser(
                        any(),
                        anyInt(),
                        anyInt()
                );
    }

    // -------------------------------------------------------------------------
    // updateMemberRole()
    // -------------------------------------------------------------------------

    @Test
    void updateMemberRole_shouldReturn200_whenRequestIsValid()
            throws Exception {

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        projectMemberId,
                        ProjectRole.MANAGER
                );

        GetProjectMemberResponse serviceResponse =
                new GetProjectMemberResponse();

        when(projectMemberService.updateMemberRole(
                any(UpdateProjectMemberRoleRequest.class)
        )).thenReturn(serviceResponse);

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .updateMemberRole(any(UpdateProjectMemberRoleRequest.class));
    }

    @Test
    void updateMemberRole_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        null,
                        ProjectRole.MANAGER
                );

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .updateMemberRole(
                        any(UpdateProjectMemberRoleRequest.class)
                );
    }

    @Test
    void updateMemberRole_shouldReturn400_whenRoleIsMissing()
            throws Exception {

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        projectMemberId,
                        null
                );

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .updateMemberRole(
                        any(UpdateProjectMemberRoleRequest.class)
                );
    }

    // -------------------------------------------------------------------------
    // removeMember()
    // -------------------------------------------------------------------------

    @Test
    void removeMember_shouldReturn200_whenRequestIsValid()
            throws Exception {

        DeleteProjectMemberRequest request =
                new DeleteProjectMemberRequest(projectMemberId);

        when(projectMemberService.removeMember(
                any(DeleteProjectMemberRequest.class)
        )).thenReturn(true);

        mockMvc.perform(
                        delete("/api/project-member/remove")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(projectMemberService)
                .removeMember(any(DeleteProjectMemberRequest.class));
    }

    @Test
    void removeMember_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        DeleteProjectMemberRequest request =
                new DeleteProjectMemberRequest(null);

        mockMvc.perform(
                        delete("/api/project-member/remove")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(projectMemberService, never())
                .removeMember(any(DeleteProjectMemberRequest.class));
    }
}
