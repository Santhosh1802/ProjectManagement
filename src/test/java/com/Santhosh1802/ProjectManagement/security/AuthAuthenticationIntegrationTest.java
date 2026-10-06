package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    private final String password = "Password@123";


    @BeforeEach
    void setUp() {

        testUser = new User();

        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testUser.setEmail(
                "test-" + UUID.randomUUID() + "@example.com"
        );

        testUser.setPassword(
                passwordEncoder.encode(password)
        );

        testUser.setUserRole(UserRole.USER);
        testUser.setIsActive(true);
        testUser.setEmailVerified(false);

        testUser = userRepository.save(testUser);
    }


    @AfterEach
    void tearDown() {

        if (testUser != null && testUser.getId() != null) {
            userRepository.deleteById(testUser.getId());
        }
    }


    @Test
    void login_shouldSucceed_whenCredentialsAreCorrect()
            throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                testUser.getEmail(),
                password
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());
    }


    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsIncorrect()
            throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "WrongPassword@123"
                }
                """.formatted(
                testUser.getEmail()
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_shouldReturnUnauthorized_whenUserDoesNotExist()
            throws Exception {

        String request = """
                {
                    "email": "does-not-exist@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_shouldReturnUnauthorized_whenUserIsInactive()
            throws Exception {

        testUser.setIsActive(false);

        testUser = userRepository.save(testUser);

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                testUser.getEmail(),
                password
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_shouldReturnForbidden_whenCsrfTokenIsMissing()
            throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                testUser.getEmail(),
                password
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void login_shouldCreateSecurityContext_whenCredentialsAreCorrect()
            throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                testUser.getEmail(),
                password
        );

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        Object securityContext =
                session.getAttribute(
                        HttpSessionSecurityContextRepository
                                .SPRING_SECURITY_CONTEXT_KEY
                );

        assertNotNull(securityContext);
    }


    @Test
    void login_shouldUpdateLastLoginAt_whenCredentialsAreCorrect()
            throws Exception {

        assertNull(testUser.getLastLoginAt());

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                testUser.getEmail(),
                password
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk());

        User updatedUser =
                userRepository.findByEmail(testUser.getEmail())
                        .orElseThrow();

        assertNotNull(updatedUser.getLastLoginAt());
        assertTrue(
                updatedUser.getLastLoginAt()
                        .isAfter(Instant.EPOCH)
        );
    }


    @Test
    void login_shouldNotUpdateLastLoginAt_whenPasswordIsIncorrect()
            throws Exception {

        assertNull(testUser.getLastLoginAt());

        String request = """
                {
                    "email": "%s",
                    "password": "WrongPassword@123"
                }
                """.formatted(
                testUser.getEmail()
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isUnauthorized());

        User updatedUser =
                userRepository.findByEmail(testUser.getEmail())
                        .orElseThrow();

        assertNull(updatedUser.getLastLoginAt());
    }
}