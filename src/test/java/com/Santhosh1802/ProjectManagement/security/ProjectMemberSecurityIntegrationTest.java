package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.authorization.ProjectMemberAuthorization;
import com.Santhosh1802.ProjectManagement.authorization.UserAuthorization;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectMemberSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectMemberAuthorization projectMemberAuthorization;

    @MockitoBean
    private UserAuthorization userAuthorization;


    // =========================================================
    // AUTHENTICATION
    // =========================================================

    @Test
    void unauthenticatedUser_shouldReceive401() throws Exception {

        mockMvc.perform(
                        get("/api/project-member/id")
                                .param(
                                        "id",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // CSRF
    // =========================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void authenticatedPostWithoutCsrf_shouldReceive403()
            throws Exception {

        mockMvc.perform(
                        post("/api/project-member/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMemberJson())
                )
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void authenticatedPostWithCsrf_shouldPassCsrfFilter()
            throws Exception {

        mockMvc.perform(
                        post("/api/project-member/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMemberJson())
                )
                .andExpect(status().isNotFound());
    }


    // =========================================================
    // CREATE MEMBER
    // =========================================================

    @Test
    @WithMockUser(username = "owner@example.com")
    void projectOwner_shouldBeAbleToCreateMember()
            throws Exception {

        when(projectMemberAuthorization.canManageProject(
                any(UUID.class),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        post("/api/project-member/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMemberJson())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "manager@example.com")
    void projectManager_shouldBeAbleToCreateMember()
            throws Exception {

        when(projectMemberAuthorization.canManageProject(
                any(UUID.class),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        post("/api/project-member/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMemberJson())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "member@example.com")
    void normalMember_shouldNotBeAbleToCreateMember()
            throws Exception {

        when(projectMemberAuthorization.canManageProject(
                any(UUID.class),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        post("/api/project-member/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createMemberJson())
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // GET MEMBER BY ID
    // =========================================================

    @Test
    @WithMockUser(username = "member@example.com")
    void memberWhoCanView_shouldBeAbleToGetMember()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canViewMember(
                eq(memberId),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/project-member/id")
                                .param("id", memberId.toString())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "unrelated@example.com")
    void unrelatedUser_shouldNotBeAbleToGetMember()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canViewMember(
                eq(memberId),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        get("/api/project-member/id")
                                .param("id", memberId.toString())
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // GET MEMBERS BY PROJECT
    // =========================================================

    @Test
    @WithMockUser(username = "owner@example.com")
    void projectOwner_shouldBeAbleToViewProjectMembers()
            throws Exception {

        UUID projectId = UUID.randomUUID();

        when(projectMemberAuthorization.canViewProject(
                eq(projectId),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", projectId.toString())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "member@example.com")
    void projectMember_shouldBeAbleToViewProjectMembers()
            throws Exception {

        UUID projectId = UUID.randomUUID();

        when(projectMemberAuthorization.canViewProject(
                eq(projectId),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", projectId.toString())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "unrelated@example.com")
    void unrelatedUser_shouldNotBeAbleToViewProjectMembers()
            throws Exception {

        UUID projectId = UUID.randomUUID();

        when(projectMemberAuthorization.canViewProject(
                eq(projectId),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        get("/api/project-member/project")
                                .param("id", projectId.toString())
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // GET PROJECTS BY USER
    // =========================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void currentUser_shouldBeAbleToGetOwnProjects()
            throws Exception {

        UUID userId = UUID.randomUUID();

        when(userAuthorization.isCurrentUser(
                eq(userId),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/project-member/member")
                                .param("id", userId.toString())
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "user@example.com")
    void user_shouldNotBeAbleToGetAnotherUsersProjects()
            throws Exception {

        UUID userId = UUID.randomUUID();

        when(userAuthorization.isCurrentUser(
                eq(userId),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        get("/api/project-member/member")
                                .param("id", userId.toString())
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // UPDATE MEMBER ROLE
    // =========================================================

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAbleToUpdateMemberRole()
            throws Exception {

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s",
                                        "projectRole": "MANAGER"
                                    }
                                    """.formatted(
                                        UUID.randomUUID()
                                ))
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "manager@example.com")
    void projectManager_shouldBeAbleToUpdateMemberRole()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canManageProjectMember(
                eq(memberId),
                eq(ProjectRole.MANAGER),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s",
                                        "projectRole": "MANAGER"
                                    }
                                    """.formatted(memberId))
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "member@example.com")
    void normalMember_shouldNotBeAbleToUpdateMemberRole()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canManageProjectMember(
                eq(memberId),
                eq(ProjectRole.MANAGER),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        put("/api/project-member/update-role")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s",
                                        "projectRole": "MANAGER"
                                    }
                                    """.formatted(memberId))
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldBeAbleToRemoveMember()
            throws Exception {

        mockMvc.perform(
                        delete("/api/project-member/remove")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s"
                                    }
                                    """.formatted(
                                        UUID.randomUUID()
                                ))
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "manager@example.com")
    void projectManager_shouldBeAbleToRemoveMember()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canManageMember(
                eq(memberId),
                any()
        )).thenReturn(true);

        mockMvc.perform(
                        delete("/api/project-member/remove")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s"
                                    }
                                    """.formatted(memberId))
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "member@example.com")
    void normalMember_shouldNotBeAbleToRemoveMember()
            throws Exception {

        UUID memberId = UUID.randomUUID();

        when(projectMemberAuthorization.canManageMember(
                eq(memberId),
                any()
        )).thenReturn(false);

        mockMvc.perform(
                        delete("/api/project-member/remove")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "id": "%s"
                                    }
                                    """.formatted(memberId))
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String createMemberJson() {

        return """
                {
                    "projectMemberId": "%s",
                    "projectId": "%s",
                    "projectRole": "MEMBER"
                }
                """.formatted(
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}