package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserOwnedProjectsResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.service.UserService;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.UserRole;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    // ============================================================
    // CREATE
    // ============================================================

    @Test
    void createUser_shouldReturnCreated() throws Exception {

        GetUserResponse response = createUserResponse();

        when(userService.createUser(any()))
                .thenReturn(response);

        String request = """
                {
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "Password@123",
                    "role": "USER"
                }
                """;

        mockMvc.perform(
                        post("/api/users/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        verify(userService)
                .createUser(any());
    }


    @Test
    void createUser_shouldReturnBadRequest_whenFirstNameIsMissing()
            throws Exception {

        String request = """
                {
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/create")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any());
    }


    @Test
    void createUser_shouldReturnBadRequest_whenEmailIsInvalid()
            throws Exception {

        String request = """
                {
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "invalid-email",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/create")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any());
    }


    @Test
    void createUser_shouldReturnBadRequest_whenPasswordIsTooShort()
            throws Exception {

        String request = """
                {
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/create")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any());
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getUserById_shouldReturnOk() throws Exception {

        UUID id = UUID.randomUUID();

        when(userService.getUserById(any()))
                .thenReturn(createUserResponse());

        mockMvc.perform(
                        get("/api/users/id")
                                .param("id", id.toString())
                )
                .andExpect(status().isOk());

        verify(userService)
                .getUserById(any());
    }


    @Test
    void getUserById_shouldReturnBadRequest_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/id")
                                .param("id", "invalid-uuid")
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .getUserById(any());
    }


    @Test
    void getUserById_shouldReturnBadRequest_whenIdIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/id")
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .getUserById(any());
    }


    // ============================================================
    // GET BY EMAIL
    // ============================================================

    @Test
    void getUserByEmail_shouldReturnOk() throws Exception {

        when(userService.getUserByEmail(any()))
                .thenReturn(createUserResponse());

        mockMvc.perform(
                        get("/api/users/email")
                                .param(
                                        "email",
                                        "santhosh@example.com"
                                )
                )
                .andExpect(status().isOk());

        verify(userService)
                .getUserByEmail(any());
    }


    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    @Test
    void updateUserProfile_shouldReturnOk() throws Exception {

        when(userService.updateUserProfile(any()))
                .thenReturn(createUserResponse());

        UUID id = UUID.randomUUID();

        String request = """
                {
                    "id": "%s",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "updated@example.com"
                }
                """.formatted(id);

        mockMvc.perform(
                        put("/api/users/profile")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());

        verify(userService)
                .updateUserProfile(any());
    }


    @Test
    void updateUserProfile_shouldReturnBadRequest_whenEmailIsInvalid()
            throws Exception {

        UUID id = UUID.randomUUID();

        String request = """
                {
                    "id": "%s",
                    "firstName": "Updated",
                    "lastName": "User",
                    "email": "invalid-email"
                }
                """.formatted(id);

        mockMvc.perform(
                        put("/api/users/profile")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .updateUserProfile(any());
    }


    // ============================================================
    // UPDATE PASSWORD
    // ============================================================

    @Test
    void updateUserPassword_shouldReturnOk() throws Exception {

        when(userService.updateUserPassword(any()))
                .thenReturn(createUserResponse());

        UUID id = UUID.randomUUID();

        String request = """
                {
                    "id": "%s",
                    "oldPassword": "OldPassword@123",
                    "newPassword": "NewPassword@123"
                }
                """.formatted(id);

        mockMvc.perform(
                        put("/api/users/password")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());

        verify(userService)
                .updateUserPassword(any());
    }


    @Test
    void updateUserPassword_shouldReturnBadRequest_whenNewPasswordIsTooShort()
            throws Exception {

        UUID id = UUID.randomUUID();

        String request = """
                {
                    "id": "%s",
                    "oldPassword": "OldPassword@123",
                    "newPassword": "123"
                }
                """.formatted(id);

        mockMvc.perform(
                        put("/api/users/password")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .updateUserPassword(any());
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Test
    void deleteUser_shouldReturnOk() throws Exception {

        when(userService.deleteUser(any()))
                .thenReturn(true);

        UUID id = UUID.randomUUID();

        String request = """
                {
                    "id": "%s"
                }
                """.formatted(id);

        mockMvc.perform(
                        delete("/api/users/delete")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());

        verify(userService)
                .deleteUser(any());
    }


    @Test
    void deleteUser_shouldReturnBadRequest_whenIdIsMissing()
            throws Exception {

        String request = """
                {}
                """;

        mockMvc.perform(
                        delete("/api/users/delete")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .deleteUser(any());
    }


    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    void getUsersBySearch_shouldReturnOk() throws Exception {

        PageResponse<GetUserResponse> pageResponse =
                new PageResponse<>(
                        List.of(createUserResponse()),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(userService.getUsersBySearch(any()))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/users/all")
                                .param("keyword", "san")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk());

        verify(userService)
                .getUsersBySearch(any());
    }

    @Test
    void getUsersBySearch_shouldReturnOk_withDefaultPagination()
            throws Exception {

        PageResponse<GetUserResponse> pageResponse =
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0,
                        true,
                        true
                );

        when(userService.getUsersBySearch(any()))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/users/all")
                )
                .andExpect(status().isOk());

        verify(userService)
                .getUsersBySearch(any());
    }

    // ============================================================
    // OWNED PROJECTS
    // ============================================================

    @Test
    void getUserOwnedProjects_shouldReturnOk() throws Exception {

        UUID id = UUID.randomUUID();

        GetUserOwnedProjectsResponse response =
                new GetUserOwnedProjectsResponse(
                        id,
                        List.of()
                );

        when(userService.getOwnedProjects(any()))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/owned/projects")
                                .param("id", id.toString())
                )
                .andExpect(status().isOk());

        verify(userService)
                .getOwnedProjects(any());
    }


    @Test
    void getUserOwnedProjects_shouldReturnBadRequest_whenIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/owned/projects")
                                .param("id", "invalid")
                )
                .andExpect(status().isBadRequest());

        verify(userService, never())
                .getOwnedProjects(any());
    }


    // ============================================================
    // TEST DATA
    // ============================================================

    private GetUserResponse createUserResponse() {

        Instant now = Instant.now();

        return new GetUserResponse(
                UUID.randomUUID(),
                "Santhosh",
                "K",
                "santhosh@example.com",
                UserRole.USER,
                true,
                false,
                now,
                now,
                null
        );
    }
}