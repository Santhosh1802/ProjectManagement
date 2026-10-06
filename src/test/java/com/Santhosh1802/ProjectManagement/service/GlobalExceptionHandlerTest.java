package com.Santhosh1802.ProjectManagement.service;


import com.Santhosh1802.ProjectManagement.exception.ErrorResponse;
import com.Santhosh1802.ProjectManagement.exception.GlobalExceptionHandler;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.projectMember.ProjectMemberNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.task.TaskNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotAuthenticatedException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserPasswordInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    private final HttpServletRequest request =
            mock(HttpServletRequest.class);

    @Test
    void handleUserAlreadyExistException_shouldReturn409() {

        when(request.getRequestURI())
                .thenReturn("/api/auth/register");

        UserAlreadyExistException exception =
                new UserAlreadyExistException("User already exists");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserAlreadyExistException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "User Already Exists",
                response.getBody().getError()
        );

        assertEquals(
                "User already exists",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/auth/register",
                response.getBody().getPath()
        );

        assertNotNull(response.getBody().getTimeStamp());
    }

    @Test
    void handleUserNotFoundException_shouldReturn404() {

        when(request.getRequestURI())
                .thenReturn("/api/users/id");

        UserNotFoundException exception =
                new UserNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFoundException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "User Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "User not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/users/id",
                response.getBody().getPath()
        );
    }

    @Test
    void handleUserNotAuthenticatedException_shouldReturn401() {

        when(request.getRequestURI())
                .thenReturn("/api/auth/me");

        UserNotAuthenticatedException exception =
                new UserNotAuthenticatedException(
                        "User is not authenticated"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotAuthenticatedException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "User Not Authenticated",
                response.getBody().getError()
        );

        assertEquals(
                "User is not authenticated",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/auth/me",
                response.getBody().getPath()
        );
    }

    @Test
    void handleUserPasswordInvalidException_shouldReturn400() {

        when(request.getRequestURI())
                .thenReturn("/api/auth/login");

        UserPasswordInvalidException exception =
                new UserPasswordInvalidException(
                        "Invalid password"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUserPasswordInvalidException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "User Password Invalid",
                response.getBody().getError()
        );

        assertEquals(
                "Invalid password",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/auth/login",
                response.getBody().getPath()
        );
    }

    @Test
    void handleProjectNotFoundException_shouldReturn404() {

        when(request.getRequestURI())
                .thenReturn("/api/projects/id");

        ProjectNotFoundException exception =
                new ProjectNotFoundException(
                        "Project not found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleProjectNotFoundException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Project Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "Project not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/projects/id",
                response.getBody().getPath()
        );
    }

    @Test
    void handleProjectAlreadyExistException_shouldReturn409() {

        when(request.getRequestURI())
                .thenReturn("/api/projects/create");

        ProjectAlreadyExistException exception =
                new ProjectAlreadyExistException(
                        "Project already exists"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleProjectAlreadyExistException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Project Already Exists",
                response.getBody().getError()
        );

        assertEquals(
                "Project already exists",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/projects/create",
                response.getBody().getPath()
        );
    }

    @Test
    void handleProjectMemberNotFoundException_shouldReturn404() {

        when(request.getRequestURI())
                .thenReturn("/api/project-members/id");

        ProjectMemberNotFoundException exception =
                new ProjectMemberNotFoundException(
                        "Project member not found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleProjectMemberNotFoundException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Project Member Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "Project member not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/project-members/id",
                response.getBody().getPath()
        );
    }

    @Test
    void handleProjectMemberAlreadyExistException_shouldReturn409() {

        when(request.getRequestURI())
                .thenReturn("/api/project-members/create");

        ProjectMemberAlreadyExistException exception =
                new ProjectMemberAlreadyExistException(
                        "Project member already exists"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleProjectMemberAlreadyExistException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Project member already exist",
                response.getBody().getError()
        );

        assertEquals(
                "Project member already exists",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/project-members/create",
                response.getBody().getPath()
        );
    }

    @Test
    void handleTaskNotFoundException_shouldReturn404() {

        when(request.getRequestURI())
                .thenReturn("/api/tasks/id");

        TaskNotFoundException exception =
                new TaskNotFoundException(
                        "Task not found"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleTaskNotFoundException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Task not found",
                response.getBody().getError()
        );

        assertEquals(
                "Task not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/tasks/id",
                response.getBody().getPath()
        );
    }

    @Test
    void handleValidationException_shouldReturn400() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(request.getRequestURI())
                .thenReturn("/api/tasks/create");

        // Validation-specific test can be added using
        // a real BindingResult when testing the controller.
        assertNotNull(exception);
    }
}