package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.user.CreateUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.DeleteUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.GetUserByEmailRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.GetUserByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.GetUserBySearchRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.UpdateUserPasswordRequest;
import com.Santhosh1802.ProjectManagement.dto.request.user.UpdateUserProfileRequest;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserOwnedProjectsResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserPasswordInvalidException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    // ============================================================
    // CREATE USER
    // ============================================================

    @Test
    void createUser_shouldCreateSuccessfully() {

        CreateUserRequest request = new CreateUserRequest(
                "Santhosh",
                "K",
                "santhosh@example.com",
                "Password@123",
                UserRole.USER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        User savedUser = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        GetUserResponse response =
                userService.createUser(request);

        assertNotNull(response);
        assertEquals(savedUser.getId(), response.getId());
        assertEquals("Santhosh", response.getFirstName());
        assertEquals("K", response.getLastName());
        assertEquals("santhosh@example.com", response.getEmail());
        assertEquals(UserRole.USER, response.getUserRole());
        assertTrue(response.getIsActive());
        assertFalse(response.getEmailVerified());

        verify(userRepository)
                .findByEmail("santhosh@example.com");

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void createUser_shouldEncodePasswordBeforeSaving() {

        CreateUserRequest request = new CreateUserRequest(
                "Santhosh",
                "K",
                "santhosh@example.com",
                "Password@123",
                UserRole.USER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");

        User savedUser = createUser();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        userService.createUser(request);

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(argThat(user ->
                        "encoded-password".equals(user.getPassword())
                ));
    }


    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {

        CreateUserRequest request = new CreateUserRequest(
                "Santhosh",
                "K",
                "santhosh@example.com",
                "Password@123",
                UserRole.USER
        );

        User existingUser = createUser();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UserAlreadyExistException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository)
                .findByEmail("santhosh@example.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    // ============================================================
    // GET USER BY ID
    // ============================================================

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {

        User user = createUser();

        GetUserByIdRequest request =
                new GetUserByIdRequest(user.getId());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        GetUserResponse response =
                userService.getUserById(request);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getUserRole(), response.getUserRole());

        verify(userRepository)
                .findById(user.getId());
    }


    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        GetUserByIdRequest request =
                new GetUserByIdRequest(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(request)
        );

        verify(userRepository)
                .findById(id);
    }


    // ============================================================
    // GET USER BY EMAIL
    // ============================================================

    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {

        User user = createUser();

        GetUserByEmailRequest request =
                new GetUserByEmailRequest(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        GetUserResponse response =
                userService.getUserByEmail(request);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());

        verify(userRepository)
                .findByEmail(user.getEmail());
    }


    @Test
    void getUserByEmail_shouldThrowException_whenUserDoesNotExist() {

        String email = "unknown@example.com";

        GetUserByEmailRequest request =
                new GetUserByEmailRequest(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail(request)
        );

        verify(userRepository)
                .findByEmail(email);
    }


    // ============================================================
    // SEARCH USERS
    // ============================================================

    @Test
    void getUsersBySearch_shouldReturnUsers() {

        User user1 = createUser();

        User user2 = createUser();
        user2.setId(UUID.randomUUID());
        user2.setEmail("second@example.com");

        GetUserBySearchRequest request =
                new GetUserBySearchRequest(
                        0,
                        10,
                        "san",
                        null,
                        null,
                        null,
                        "createdAt",
                        "desc"
                );

        Page<User> page =
                new PageImpl<>(
                        List.of(user1, user2),
                        PageRequest.of(0, 10),
                        2
                );

        when(userRepository.searchUsers(
                eq("san"),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        PageResponse<GetUserResponse> response =
                userService.getUsersBySearch(request);

        assertNotNull(response);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        assertEquals(
                user1.getEmail(),
                response.getContent().get(0).getEmail()
        );

        assertEquals(
                user2.getEmail(),
                response.getContent().get(1).getEmail()
        );

        verify(userRepository)
                .searchUsers(
                        eq("san"),
                        isNull(),
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    void getUsersBySearch_shouldReturnEmptyPage_whenNoUsersFound() {

        GetUserBySearchRequest request =
                new GetUserBySearchRequest(
                        0,
                        10,
                        "unknown",
                        null,
                        null,
                        null,
                        "createdAt",
                        "desc"
                );

        Page<User> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(userRepository.searchUsers(
                eq("unknown"),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        PageResponse<GetUserResponse> response =
                userService.getUsersBySearch(request);

        assertNotNull(response);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        verify(userRepository)
                .searchUsers(
                        eq("unknown"),
                        isNull(),
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    @Test
    void updateUserProfile_shouldUpdateSuccessfully() {

        User user = createUser();

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        user.getId(),
                        "Updated",
                        "Name",
                        "updated@example.com"
                );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        GetUserResponse response =
                userService.updateUserProfile(request);

        assertNotNull(response);

        assertEquals(
                "Updated",
                user.getFirstName()
        );

        assertEquals(
                "Name",
                user.getLastName()
        );

        assertEquals(
                "updated@example.com",
                user.getEmail()
        );

        assertFalse(user.getEmailVerified());

        verify(userRepository)
                .save(user);
    }


    @Test
    void updateUserProfile_shouldNotResetEmailVerification_whenEmailDoesNotChange() {

        User user = createUser();

        user.setEmailVerified(true);

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        user.getId(),
                        "Updated",
                        "Name",
                        user.getEmail()
                );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        userService.updateUserProfile(request);

        assertTrue(user.getEmailVerified());

        verify(userRepository)
                .save(user);
    }


    @Test
    void updateUserProfile_shouldResetEmailVerification_whenEmailChanges() {

        User user = createUser();

        user.setEmailVerified(true);

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        user.getId(),
                        "Santhosh",
                        "K",
                        "new@example.com"
                );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        userService.updateUserProfile(request);

        assertEquals(
                "new@example.com",
                user.getEmail()
        );

        assertFalse(user.getEmailVerified());
    }


    @Test
    void updateUserProfile_shouldThrowException_whenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        id,
                        "Santhosh",
                        "K",
                        "new@example.com"
                );

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserProfile(request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // ============================================================
    // UPDATE PASSWORD
    // ============================================================

    @Test
    void updateUserPassword_shouldUpdateSuccessfully() {

        User user = createUser();

        UpdateUserPasswordRequest request =
                new UpdateUserPasswordRequest(
                        user.getId(),
                        "OldPassword@123",
                        "NewPassword@123"
                );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OldPassword@123",
                "encoded-password"
        )).thenReturn(true);

        when(passwordEncoder.encode("NewPassword@123"))
                .thenReturn("new-encoded-password");

        when(userRepository.save(user))
                .thenReturn(user);

        GetUserResponse response =
                userService.updateUserPassword(request);

        assertNotNull(response);

        assertEquals(
                "new-encoded-password",
                user.getPassword()
        );

        verify(passwordEncoder)
                .matches(
                        "OldPassword@123",
                        "encoded-password"
                );

        verify(passwordEncoder)
                .encode("NewPassword@123");

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateUserPassword_shouldThrowException_whenOldPasswordIsInvalid() {

        User user = createUser();

        UpdateUserPasswordRequest request =
                new UpdateUserPasswordRequest(
                        user.getId(),
                        "WrongPassword@123",
                        "NewPassword@123"
                );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword@123",
                user.getPassword()
        )).thenReturn(false);

        assertThrows(
                UserPasswordInvalidException.class,
                () -> userService.updateUserPassword(request)
        );

        verify(passwordEncoder, never())
                .encode("NewPassword@123");

        verify(userRepository, never())
                .save(any(User.class));
    }


    @Test
    void updateUserPassword_shouldThrowException_whenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        UpdateUserPasswordRequest request =
                new UpdateUserPasswordRequest(
                        id,
                        "OldPassword@123",
                        "NewPassword@123"
                );

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserPassword(request)
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }


    // ============================================================
    // DELETE USER
    // ============================================================

    @Test
    void deleteUser_shouldDeleteSuccessfully() {

        UUID id = UUID.randomUUID();

        DeleteUserRequest request =
                new DeleteUserRequest(id);

        User user = createUser();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        Boolean result =
                userService.deleteUser(request);

        assertTrue(result);

        verify(userRepository)
                .findById(id);

        verify(userRepository)
                .deleteById(id);
    }


    @Test
    void deleteUser_shouldThrowException_whenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        DeleteUserRequest request =
                new DeleteUserRequest(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(request)
        );

        verify(userRepository, never())
                .deleteById(id);
    }


    // ============================================================
    // OWNED PROJECTS
    // ============================================================

    @Test
    void getOwnedProjects_shouldReturnProjects_whenUserExists() {

        User user = createUser();

        GetUserByIdRequest request =
                new GetUserByIdRequest(user.getId());

        Project project = mock(Project.class);

        UUID projectId = UUID.randomUUID();

        when(project.getId())
                .thenReturn(projectId);

        when(project.getName())
                .thenReturn("Project One");

        when(project.getDescription())
                .thenReturn("Project description");

        when(project.getOwner())
                .thenReturn(user);

        when(project.getCreatedAt())
                .thenReturn(Instant.now());

        when(project.getUpdatedAt())
                .thenReturn(Instant.now());

        when(projectRepository.findProjectsByOwner(user))
                .thenReturn(List.of(project));

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        GetUserOwnedProjectsResponse response =
                userService.getOwnedProjects(request);

        assertNotNull(response);

        assertEquals(
                user.getId(),
                response.getId()
        );

        assertNotNull(response.getOwnedProjects());

        assertEquals(
                1,
                response.getOwnedProjects().size()
        );

        assertEquals(
                projectId,
                response.getOwnedProjects().getFirst().getId()
        );

        verify(projectRepository)
                .findProjectsByOwner(user);
    }


    @Test
    void getOwnedProjects_shouldReturnEmptyList_whenUserHasNoProjects() {

        User user = createUser();

        GetUserByIdRequest request =
                new GetUserByIdRequest(user.getId());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(projectRepository.findProjectsByOwner(user))
                .thenReturn(List.of());

        GetUserOwnedProjectsResponse response =
                userService.getOwnedProjects(request);

        assertNotNull(response);
        assertTrue(response.getOwnedProjects().isEmpty());
    }


    @Test
    void getOwnedProjects_shouldThrowException_whenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        GetUserByIdRequest request =
                new GetUserByIdRequest(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getOwnedProjects(request)
        );

        verify(projectRepository, never())
                .findProjectsByOwner(any(User.class));
    }


    // ============================================================
    // TEST DATA
    // ============================================================

    private User createUser() {

        Instant now = Instant.now();

        User user = new User();

        user.setId(UUID.randomUUID());
        user.setFirstName("Santhosh");
        user.setLastName("K");
        user.setEmail("santhosh@example.com");
        user.setPassword("encoded-password");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(null);

        return user;
    }
}