package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.auth.LoginUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Santhosh1802.ProjectManagement.dto.request.auth.RegisterUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.auth.RegisterUserResponse;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.UserRole;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_shouldRegisterSuccessfully() {

        RegisterUserRequest request = new RegisterUserRequest();

        request.setFirstName("Santhosh");
        request.setLastName("K");
        request.setEmail("santhosh@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        User savedUser = new User();

        savedUser.setId(UUID.randomUUID());
        savedUser.setFirstName("Santhosh");
        savedUser.setLastName("K");
        savedUser.setEmail("santhosh@example.com");
        savedUser.setPassword("encoded-password");
        savedUser.setUserRole(UserRole.USER);
        savedUser.setIsActive(true);
        savedUser.setEmailVerified(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterUserResponse response =
                authService.registerUser(request);

        assertNotNull(response);

        assertEquals(
                "santhosh@example.com",
                response.getEmail()
        );

        assertEquals(
                UserRole.USER,
                response.getUserRole()
        );

        assertTrue(response.getIsActive());

        assertFalse(response.getEmailVerified());

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {

        RegisterUserRequest request = new RegisterUserRequest();

        request.setFirstName("Santhosh");
        request.setLastName("K");
        request.setEmail("santhosh@example.com");
        request.setPassword("Password@123");

        User existingUser = new User();

        existingUser.setEmail("santhosh@example.com");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UserAlreadyExistException.class,
                () -> authService.registerUser(request)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void registerUser_shouldEncodePassword() {

        RegisterUserRequest request = new RegisterUserRequest();

        request.setFirstName("Santhosh");
        request.setLastName("K");
        request.setEmail("santhosh@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");

        User savedUser = new User();

        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("santhosh@example.com");
        savedUser.setPassword("encoded-password");
        savedUser.setUserRole(UserRole.USER);
        savedUser.setIsActive(true);
        savedUser.setEmailVerified(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.registerUser(request);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User user = captor.getValue();

        assertEquals(
                "encoded-password",
                user.getPassword()
        );

        assertNotEquals(
                "Password@123",
                user.getPassword()
        );
    }

    @Test
    void loginUser_shouldUpdateLastLoginTime() {

        // Arrange
        LoginUserRequest request = new LoginUserRequest(
                "santhosh@example.com",
                "Password@123"
        );

        User user = new User();

        user.setId(UUID.randomUUID());
        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhosh@example.com");
        user.setPassword("encoded-password");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        assertNull(user.getLastLoginAt());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenReturn(user);


        // Act
        GetUserResponse response =
                authService.updateLoginDetails(request);


        // Assert
        assertNotNull(response);

        assertNotNull(user.getLastLoginAt());

        assertEquals(
                "santhosh@example.com",
                response.getEmail()
        );

        assertEquals(
                UserRole.USER,
                response.getUserRole()
        );

        verify(userRepository)
                .findByEmail("santhosh@example.com");

        verify(userRepository)
                .save(user);
    }

    @Test
    void loginUser_shouldThrowException_whenUserDoesNotExist() {

        // Arrange
        LoginUserRequest request = new LoginUserRequest(
                "unknown@example.com",
                "Password@123"
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());


        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> authService.updateLoginDetails(request)
        );


        verify(userRepository)
                .findByEmail("unknown@example.com");

        verify(userRepository, never())
                .save(any(User.class));
    }

}