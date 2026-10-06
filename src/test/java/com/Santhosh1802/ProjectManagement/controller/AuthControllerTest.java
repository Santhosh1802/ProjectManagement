package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.config.SecurityConfig;
import com.Santhosh1802.ProjectManagement.dto.request.auth.LoginUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.auth.RegisterUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.auth.RegisterUserResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.service.AuthService;
import com.Santhosh1802.ProjectManagement.service.CustomUserDetailsService;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void register_shouldReturnBadRequest_whenFirstNameIsBlank()
            throws Exception {

        String request = """
                {
                    "firstName": "",
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(anonymous())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(RegisterUserRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailIsInvalid()
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
                        post("/api/auth/register")
                                .with(anonymous())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(RegisterUserRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsTooShort()
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
                        post("/api/auth/register")
                                .with(anonymous())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerUser(any(RegisterUserRequest.class));
    }

    @Test
    void register_shouldCallAuthService_whenRequestIsValid()
            throws Exception {

        RegisterUserResponse response =
                new RegisterUserResponse(
                        UUID.randomUUID(),
                        "Santhosh",
                        "K",
                        "santhosh@example.com",
                        UserRole.USER,
                        true,
                        false,
                        Instant.now(),
                        Instant.now(),
                        null
                );

        when(authService.registerUser(any(RegisterUserRequest.class)))
                .thenReturn(response);

        String request = """
                {
                    "firstName": "Santhosh",
                    "lastName": "K",
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(anonymous())
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isCreated());

        verify(authService)
                .registerUser(any(RegisterUserRequest.class));
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() throws Exception {

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        GetUserResponse userResponse = new GetUserResponse(
                UUID.randomUUID(),
                "Santhosh",
                "K",
                "santhosh@example.com",
                UserRole.USER,
                true,
                false,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );

        when(authService.updateLoginDetails(any(LoginUserRequest.class)))
                .thenReturn(userResponse);

        String request = """
                {
                    "email": "santhosh@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(anonymous())
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());


        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(authService).updateLoginDetails(
                any(LoginUserRequest.class)
        );
    }


}
