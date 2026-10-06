package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
class UserSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    if (user.getId() == null) {
                        user.setId(UUID.randomUUID());
                    }

                    return user;
                });
    }

    private String createUserRequest(String role) {
        return """
            {
                "firstName": "Santhosh",
                "lastName": "K",
                "email": "newuser@example.com",
                "password": "Password@123",
                "role": "%s"
            }
            """.formatted(role);
    }



    // ============================================================
    // UNAUTHENTICATED ACCESS
    // ============================================================

    @Test
    void getUserById_shouldReturnUnauthorized_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/id")
                                .param(
                                        "id",
                                        "00000000-0000-0000-0000-000000000001"
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void getUserByEmail_shouldReturnUnauthorized_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/email")
                                .param(
                                        "email",
                                        "santhosh@example.com"
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void getAllUsers_shouldReturnUnauthorized_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/all")
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void getOwnedProjects_shouldReturnUnauthorized_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/owned/projects")
                                .param(
                                        "id",
                                        "00000000-0000-0000-0000-000000000001"
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    // ============================================================
    // CSRF PROTECTION
    // ============================================================

    @Test
    void createUser_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/users/create")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void updateProfile_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "id": "00000000-0000-0000-0000-000000000001",
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "santhosh@example.com"
                }
                """;

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void updatePassword_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "id": "00000000-0000-0000-0000-000000000001",
                    "oldPassword": "OldPassword@123",
                    "newPassword": "NewPassword@123"
                }
                """;

        mockMvc.perform(
                        put("/api/users/password")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteUser_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "id": "00000000-0000-0000-0000-000000000001"
                }
                """;

        mockMvc.perform(
                        delete("/api/users/delete")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }


    // ============================================================
    // CSRF TOKEN PRESENT
    // ============================================================

    @Test
    void createUser_shouldNotBeRejectedByCsrf_whenCsrfTokenIsPresent()
            throws Exception {

        /*
         * We are not testing successful authentication here.
         *
         * This test only verifies that CSRF protection allows the
         * request to continue when a valid CSRF token is supplied.
         *
         * Since the request is unauthenticated, the expected result
         * after CSRF passes is 401.
         */

        String request = """
                {
                    "firstName": "Santhosh",
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
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
// CREATE USER AUTHORIZATION
// ============================================================

    @Test
    void createUser_shouldAllowAdminToCreateUser() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("admin@example.com").roles("ADMIN"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("USER"))
                )
                .andExpect(status().isCreated());
    }


    @Test
    void createUser_shouldAllowAdminToCreateManager() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("admin@example.com").roles("ADMIN"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("MANAGER"))
                )
                .andExpect(status().isCreated());
    }


    @Test
    void createUser_shouldAllowAdminToCreateAdmin() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("admin@example.com").roles("ADMIN"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("ADMIN"))
                )
                .andExpect(status().isCreated());
    }


    @Test
    void createUser_shouldAllowManagerToCreateUser() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("manager@example.com").roles("MANAGER"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("USER"))
                )
                .andExpect(status().isCreated());
    }


    @Test
    void createUser_shouldDenyManagerToCreateManager() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("manager@example.com").roles("MANAGER"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("MANAGER"))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void createUser_shouldDenyManagerToCreateAdmin() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("manager@example.com").roles("MANAGER"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("ADMIN"))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void createUser_shouldDenyUserToCreateUser() throws Exception {

        mockMvc.perform(
                        post("/api/users/create")
                                .with(user("user@example.com").roles("USER"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(createUserRequest("USER"))
                )
                .andExpect(status().isForbidden());
    }
    // ============================================================
// GET USER BY ID AUTHORIZATION
// ============================================================

    @Test
    void getUserById_shouldAllowCurrentUser() throws Exception {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/api/users/id")
                                .param("id", userId.toString())
                                .with(user("user@example.com").roles("USER"))
                )
                .andExpect(status().isOk());
    }


    @Test
    void getUserById_shouldDenyOtherUser() throws Exception {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("other@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/api/users/id")
                                .param("id", userId.toString())
                                .with(user("user@example.com").roles("USER"))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void getUserById_shouldAllowAdmin() throws Exception {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("other@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/api/users/id")
                                .param("id", userId.toString())
                                .with(user("admin@example.com").roles("ADMIN"))
                )
                .andExpect(status().isOk());
    }
}
