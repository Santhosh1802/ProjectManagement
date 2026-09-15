package com.Santhosh1802.ProjectManagement.service;


import com.Santhosh1802.ProjectManagement.dto.request.user.CreateUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.GetUserByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.response.user.CreateUserResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_shouldReturnUser_whenUserExists(){

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhoshkasirajan18@gmail.com");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        GetUserByIdRequest request = new GetUserByIdRequest();
        request.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        GetUserResponse response = userService.getUserById(request);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Santhosh",response.getFirstName());
        assertEquals("K",response.getLastName());
        assertEquals("santhoshkasirajan18@gmail.com",response.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist(){

        UUID userId = UUID.randomUUID();

        GetUserByIdRequest request = new GetUserByIdRequest();
        request.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                ()->userService.getUserById(request)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void createUser_shouldCreateUser_whenEmailDoesNoExist(){

        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Santhosh");
        request.setLastName("K");
        request.setEmail("santhoshkasirajan18@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("santhoshkasirajan18@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setFirstName("Santhosh");
        savedUser.setLastName("K");
        savedUser.setEmail("santhoshkasirajan18@gmail.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setUserRole(UserRole.USER);
        savedUser.setIsActive(true);
        savedUser.setEmailVerified(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        CreateUserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Santhosh",response.getFirstName());
        assertEquals("K",response.getLastName());
        assertEquals("santhoshkasirajan18@gmail.com",response.getEmail());
        assertEquals(UserRole.USER,response.getUserRole());

        verify(userRepository).findByEmail("santhoshkasirajan18@gmail.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));

    }

}
