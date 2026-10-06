package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.dto.request.auth.LoginUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.service.AuthService;
import com.Santhosh1802.ProjectManagement.service.CustomUserDetailsService;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthService authService;

    @Test
    void me_shouldReturnUnauthorized_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturnUnauthorized_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void csrf_shouldBeAccessibleWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/csrf")
        ).andExpect(status().isOk());
    }

    @Test
    void login_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType("application/json")
                        .content(request)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "santhosh@example.com")
    void getCurrentUser_shouldReturnOk_whenAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldCreateAuthenticatedSession()
            throws Exception {

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("santhosh@example.com");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authService.updateLoginDetails(
                any(LoginUserRequest.class)
        )).thenReturn(createUserResponse());

        String loginRequest = """
                {
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        Object securityContext = session.getAttribute(
                HttpSessionSecurityContextRepository
                        .SPRING_SECURITY_CONTEXT_KEY
        );

        assertNotNull(securityContext);
    }

    @Test
    void logout_shouldInvalidateSession()
            throws Exception {

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("santhosh@example.com");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authService.updateLoginDetails(
                any(LoginUserRequest.class)
        )).thenReturn(createUserResponse());

        String loginRequest = """
                {
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        mockMvc.perform(
                post("/api/auth/logout")
                        .with(csrf())
                        .session(session)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/auth/me")
                        .session(session)
        ).andExpect(status().isUnauthorized());
    }

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
                now
        );
    }
}
