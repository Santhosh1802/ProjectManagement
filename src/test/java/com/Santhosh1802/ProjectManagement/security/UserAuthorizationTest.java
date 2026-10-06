package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.authorization.UserAuthorization;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhosh@example.com");
    }

    @Test
    void isCurrentUser_shouldReturnTrue_whenUserOwnsAccount() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("santhosh@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        boolean result =
                new UserAuthorization(userRepository)
                        .isCurrentUser(userId, authentication);

        assertTrue(result);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenUserIsNotOwner() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("other@example.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        boolean result =
                new UserAuthorization(userRepository)
                        .isCurrentUser(userId, authentication);

        assertFalse(result);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenUserDoesNotExist() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        boolean result =
                new UserAuthorization(userRepository)
                        .isCurrentUser(userId, authentication);

        assertFalse(result);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenAuthenticationIsNull() {

        boolean result =
                new UserAuthorization(userRepository)
                        .isCurrentUser(userId, null);

        assertFalse(result);

        verifyNoInteractions(userRepository);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenUserIsNotAuthenticated() {

        when(authentication.isAuthenticated())
                .thenReturn(false);

        boolean result =
                new UserAuthorization(userRepository)
                        .isCurrentUser(userId, authentication);

        assertFalse(result);

        verifyNoInteractions(userRepository);
    }
}
