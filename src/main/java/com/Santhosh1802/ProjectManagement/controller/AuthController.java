package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.auth.LoginUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.auth.RegisterUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.auth.RegisterUserResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotAuthenticatedException;
import com.Santhosh1802.ProjectManagement.service.AuthService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;

/**
 * REST controller responsible for authentication and session management.
 *
 * <p>
 * Provides endpoints for user registration, login, logout, authentication
 * status checks, and CSRF token retrieval.
 * </p>
 *
 * <p>
 * Authentication is handled using Spring Security with a session-based
 * security context. The controller does not directly manage passwords;
 * credential verification is delegated to Spring Security's
 * {@link AuthenticationManager}.
 * </p>
 *
 * @apiNote Base endpoint: {@code /api/auth}
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Registers a new user account.
     *
     * <p>
     * Accepts the user's registration details, validates the request,
     * and delegates user creation to the {@link AuthService}.
     * </p>
     *
     * <p>
     * This endpoint is publicly accessible because the user does not
     * need to be authenticated to create an account.
     * </p>
     *
     * @param registerUserRequest registration details including first name,
     *                            last name, email, and password
     * @return {@link ResponseEntity} containing the registration response
     * with HTTP status {@code 201 CREATED}
     * @apiNote Endpoint: {@code POST /api/auth/register}
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponse>> register(
            @Valid @RequestBody RegisterUserRequest registerUserRequest
    ) {
        RegisterUserResponse registerUserResponse =
                authService.registerUser(registerUserRequest);

        ApiResponse<RegisterUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "User registered successfully",
                registerUserResponse
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticates a user and creates an authenticated session.
     *
     * <p>
     * The supplied email and password are authenticated using Spring
     * Security's {@link AuthenticationManager}. When authentication
     * succeeds, the authenticated {@link SecurityContext} is stored
     * using the configured {@link SecurityContextRepository}, allowing
     * the user to remain authenticated across subsequent requests.
     * </p>
     *
     * <p>
     * After successful authentication, the user's last login timestamp
     * is updated through the {@link AuthService}.
     * </p>
     *
     * @param loginUserRequest login credentials containing the user's
     *                         email and password
     * @param httpRequest      current HTTP request used when storing the
     *                         security context
     * @param httpResponse     current HTTP response used when storing the
     *                         security context
     * @return {@link ResponseEntity} containing the authenticated user's
     * information with HTTP status {@code 200 OK}
     * @throws org.springframework.security.core.AuthenticationException when the supplied credentials are invalid
     * @apiNote Endpoint: {@code POST /api/auth/login}
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<GetUserResponse>> login(
            @Valid @RequestBody LoginUserRequest loginUserRequest,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserRequest.getEmail(),
                        loginUserRequest.getPassword()
                )
        );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        GetUserResponse user =
                authService.updateLoginDetails(loginUserRequest);

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User logged in successfully",
                user
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Logs out the currently authenticated user.
     *
     * <p>
     * Clears the current {@link SecurityContext} and saves an empty
     * security context using the configured {@link SecurityContextRepository}.
     * This removes the authentication associated with the current session.
     * </p>
     *
     * @param httpRequest  current HTTP request associated with the
     *                     authenticated session
     * @param httpResponse current HTTP response used when saving the
     *                     updated security context
     * @return {@link ResponseEntity} containing {@code true} when the
     * logout operation is completed, with HTTP status
     * {@code 200 OK}
     * @apiNote Endpoint: {@code POST /api/auth/logout}
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        SecurityContextHolder.clearContext();

        securityContextRepository.saveContext(
                SecurityContextHolder.createEmptyContext(),
                httpRequest,
                httpResponse
        );

        ApiResponse<Boolean> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User logged out",
                true
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * <p>
     * Spring Security provides the current {@link Authentication}
     * object for the request. If authentication is missing or invalid,
     * a {@link UserNotAuthenticatedException} is thrown.
     * </p>
     *
     * <p>
     * This endpoint is used by the frontend upon page refresh/initialization
     * to restore the authenticated user's profile and roles.
     * </p>
     *
     * @param authentication current Spring Security authentication
     *                       associated with the request
     * @return {@link ResponseEntity} containing the authenticated user's profile,
     * with HTTP status {@code 200 OK}
     * @throws UserNotAuthenticatedException when no authenticated user is associated with the request
     * @apiNote Endpoint: {@code GET /api/auth/me}
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetUserResponse>> getCurrentUser(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException(
                    "User not authenticated"
            );
        }

        GetUserResponse user = authService.getAuthenticatedUser(authentication.getName());

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User Authenticated",
                user
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Returns the CSRF token associated with the current request.
     *
     * <p>
     * The frontend uses this endpoint to obtain the CSRF token required
     * for protected state-changing requests such as POST, PUT, and DELETE.
     * </p>
     *
     * <p>
     * The CSRF token is generated and managed by Spring Security through
     * the configured {@link org.springframework.security.web.csrf.CsrfTokenRepository}.
     * </p>
     *
     * @param csrfToken CSRF token generated or resolved by Spring Security
     * @return the current {@link CsrfToken}
     * @apiNote Endpoint: {@code GET /api/auth/csrf}
     */
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}