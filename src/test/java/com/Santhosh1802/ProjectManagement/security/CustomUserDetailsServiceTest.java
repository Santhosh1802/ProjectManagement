package com.Santhosh1802.ProjectManagement.security;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.service.CustomUserDetailsService;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {

        User user = createUser();

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "santhosh@example.com"
                );

        assertNotNull(result);
        assertEquals(
                "santhosh@example.com",
                result.getUsername()
        );

        verify(userRepository)
                .findByEmail("santhosh@example.com");
    }


    @Test
    void loadUserByUsername_shouldReturnCorrectPassword_whenUserExists() {

        User user = createUser();

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "santhosh@example.com"
                );

        assertEquals(
                "encoded-password",
                result.getPassword()
        );
    }


    @Test
    void loadUserByUsername_shouldReturnCorrectRole_whenUserExists() {

        User user = createUser();
        user.setUserRole(UserRole.USER);

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "santhosh@example.com"
                );

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                Objects.equals(authority.getAuthority(), "ROLE_USER")
                        )
        );
    }


    @Test
    void loadUserByUsername_shouldEnableUser_whenUserIsActive() {

        User user = createUser();
        user.setIsActive(true);

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "santhosh@example.com"
                );

        assertTrue(result.isEnabled());
    }


    @Test
    void loadUserByUsername_shouldDisableUser_whenUserIsInactive() {

        User user = createUser();
        user.setIsActive(false);

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "santhosh@example.com"
                );

        assertFalse(result.isEnabled());
    }


    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(
                        "unknown@example.com"
                )
        );

        verify(userRepository)
                .findByEmail("unknown@example.com");
    }

    @Test
    void loadUserByUsername_shouldQueryRepositoryOnlyOnce() {

        User user = createUser();

        when(userRepository.findByEmail("santhosh@example.com"))
                .thenReturn(Optional.of(user));

        customUserDetailsService.loadUserByUsername(
                "santhosh@example.com"
        );

        verify(userRepository, times(1))
                .findByEmail("santhosh@example.com");

        verifyNoMoreInteractions(userRepository);
    }


    private User createUser() {

        User user = new User();

        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhosh@example.com");
        user.setPassword("encoded-password");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);

        return user;
    }
}