package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.user.*;
import com.Santhosh1802.ProjectManagement.dto.response.project.GetProjectResponse;
import com.Santhosh1802.ProjectManagement.dto.response.project.ProjectOwnerResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserOwnedProjectsResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserPasswordInvalidException;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for user management operations.
 *
 * <p>
 * Provides functionality for creating users, retrieving user information,
 * searching and filtering users, updating user profiles and passwords,
 * deleting users, and retrieving projects owned by users.
 * </p>
 *
 * <p>
 * Read operations use a read-only transaction by default, while methods
 * that modify persistent data explicitly use {@link Transactional}.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Converts a {@link User} entity into a {@link GetUserResponse} DTO.
     *
     * <p>
     * This method maps the user entity fields required by the API response
     * and prevents the persistence entity from being directly exposed to
     * the controller layer.
     * </p>
     *
     * @param user user entity to convert
     * @return {@link GetUserResponse} containing the user's response data
     */
    private GetUserResponse returnGetUserResponse(User user) {
        return new GetUserResponse(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserRole(),
                user.getIsActive(),
                user.getEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * Creates the pagination and sorting configuration for user searches.
     *
     * <p>
     * Only explicitly supported user fields are allowed for sorting.
     * If an unsupported sort field is supplied, {@code createdAt} is used
     * as the default sorting field.
     * </p>
     *
     * <p>
     * The requested sort order is converted into a Spring Data
     * {@link Sort.Direction}. Any value other than {@code asc} results
     * in descending order.
     * </p>
     *
     * @param request user search request containing pagination and sorting
     *                parameters
     * @return configured {@link Pageable} instance
     */
    private Pageable createUserPageable(
            GetUserBySearchRequest request
    ) {

        String sortField = switch (request.getSortBy()) {

            case "firstName" -> "firstName";

            case "lastName" -> "lastName";

            case "email" -> "email";

            case "updatedAt" -> "updatedAt";

            case "lastLoginAt" -> "lastLoginAt";

            default -> "createdAt";
        };

        Sort.Direction direction =
                "asc".equalsIgnoreCase(request.getSortOrder())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, sortField)
        );
    }

    /**
     * Normalizes a user search keyword before it is passed to the repository.
     *
     * <p>
     * Leading and trailing whitespace is removed from the supplied keyword.
     * If the keyword is {@code null} or becomes empty after trimming,
     * {@code null} is returned so that the repository can treat the search
     * as having no keyword filter.
     * </p>
     *
     * @param keyword raw search keyword
     * @return normalized keyword, or {@code null} when no keyword is supplied
     */
    private String normalizeKeyword(String keyword) {

        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    /**
     * Creates a new user account.
     *
     * <p>
     * The method first checks whether a user already exists with the
     * supplied email address. If the email is already registered,
     * a {@link UserAlreadyExistException} is thrown.
     * </p>
     *
     * <p>
     * For a new user, the password is encoded using the configured
     * {@link PasswordEncoder}. and initially
     * marked as having an unverified email address.
     * </p>
     *
     * @param createUserRequest request containing the user's creation details
     * @return {@link GetUserResponse} containing the newly created user's data
     * @throws UserAlreadyExistException if a user with the supplied email address already exists
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "(hasRole('MANAGER') and #createUserRequest.role == T(com.Santhosh1802.ProjectManagement.util.UserRole).USER)"
    )
    @Transactional
    public GetUserResponse createUser(@P("createUserRequest") CreateUserRequest createUserRequest) {
        boolean userExist = userRepository.findByEmail(createUserRequest.getEmail()).isPresent();
        if (!userExist) {
            User user = new User();
            user.setFirstName(createUserRequest.getFirstName());
            user.setLastName(createUserRequest.getLastName());
            user.setEmail(createUserRequest.getEmail());
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));

            user.setUserRole(createUserRequest.getRole());
            user.setIsActive(true);
            user.setEmailVerified(false);

            User createdUser = userRepository.save(user);

            return returnGetUserResponse(createdUser);
        } else {
            throw new UserAlreadyExistException("Email already exists");
        }
    }


    /**
     * Retrieves all projects owned by a specific user.
     *
     * <p>
     * The method first verifies that the requested user exists and then
     * retrieves the projects for which that user is the owner.
     * Each project is converted into a {@link GetProjectResponse}.
     * </p>
     *
     * @param getUserByIdRequest request containing the user's unique identifier
     * @return {@link GetUserOwnedProjectsResponse} containing the user ID
     * and their owned projects
     * @throws UserNotFoundException if no user exists with the supplied identifier
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@userAuthorization.isCurrentUser(#getUserByIdRequest.id, authentication)"
    )
    public GetUserOwnedProjectsResponse getOwnedProjects(@P("getUserByIdRequest") GetUserByIdRequest getUserByIdRequest) {
        User user = userRepository.findById(getUserByIdRequest.getId()).orElseThrow(
                () -> new UserNotFoundException("User not found with id " + getUserByIdRequest.getId())
        );
        List<GetProjectResponse> projects = new ArrayList<>();
        projectRepository.findProjectsByOwner(user).forEach(project -> {
            GetProjectResponse projectResponse = new GetProjectResponse(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getProjectStatus(),
                    project.getStartDate(),
                    project.getDueDate(),
                    new ProjectOwnerResponse(project.getOwner().getId(),
                            project.getOwner().getFirstName(),
                            project.getOwner().getLastName(),
                            project.getOwner().getEmail()),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
            projects.add(projectResponse);
        });
        return new GetUserOwnedProjectsResponse(
                getUserByIdRequest.getId(), projects
        );

    }


    /**
     * Retrieves a user by their unique identifier.
     *
     * <p>
     * The method searches the user repository using the supplied UUID
     * and converts the resulting entity into a {@link GetUserResponse}.
     * </p>
     *
     * @param getUserByIdRequest request containing the user's unique identifier
     * @return {@link GetUserResponse} containing the requested user's data
     * @throws UserNotFoundException if no user exists with the supplied identifier
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@userAuthorization.isCurrentUser(#getUserByIdRequest.id, authentication)"
    )
    public GetUserResponse getUserById(@P("getUserByIdRequest") GetUserByIdRequest getUserByIdRequest) {

        User user = userRepository.findById(getUserByIdRequest.getId()).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + getUserByIdRequest.getId())
        );

        return returnGetUserResponse(user);

    }

    /**
     * Retrieves a user by their email address.
     *
     * <p>
     * The method searches the user repository using the supplied email
     * address and converts the resulting entity into a
     * {@link GetUserResponse}.
     * </p>
     *
     * @param getUserByEmailRequest request containing the user's email address
     * @return {@link GetUserResponse} containing the requested user's data
     * @throws UserNotFoundException if no user exists with the supplied email address
     */
    @PreAuthorize("hasRole('ADMIN')")
    public GetUserResponse getUserByEmail(GetUserByEmailRequest getUserByEmailRequest) {
        User user = userRepository.findByEmail(getUserByEmailRequest.getEmail()).orElseThrow(() ->
                new UserNotFoundException("User not found with email" + getUserByEmailRequest.getEmail())
        );
        return returnGetUserResponse(user);
    }

    /**
     * Searches and retrieves users using pagination, filtering, and sorting.
     *
     * <p>
     * Supports searching users by first name, last name, or email address.
     * Results can be filtered by user role, account activation status,
     * and email verification status.
     * </p>
     *
     * <p>
     * Results are sorted using the supported sorting fields and retrieved
     * using database-level pagination through Spring Data {@link Pageable}.
     * This prevents the application from loading the complete user dataset
     * into memory before applying pagination.
     * </p>
     *
     * <p>
     * Access to this operation is restricted to users with the
     * {@code ADMIN} role.
     * </p>
     *
     * @param request search, filtering, pagination, and sorting parameters
     * @return {@link PageResponse} containing the paginated user results
     * @throws org.springframework.security.access.AccessDeniedException if the authenticated user does not have the {@code ADMIN} role
     */
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<GetUserResponse> getUsersBySearch(
            GetUserBySearchRequest request
    ) {

        Pageable pageable = createUserPageable(request);

        String keyword = normalizeKeyword(request.getKeyword());

        Page<User> userPage =
                userRepository.searchUsers(
                        keyword,
                        request.getRole(),
                        request.getIsActive(),
                        request.getEmailVerified(),
                        pageable
                );

        List<GetUserResponse> users =
                userPage.getContent()
                        .stream()
                        .map(this::returnGetUserResponse)
                        .toList();

        return new PageResponse<>(
                users,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    /**
     * Updates a user's profile information.
     *
     * <p>
     * Retrieves the user using the supplied identifier and updates the
     * user's first name, last name, and email address.
     * </p>
     *
     * <p>
     * If the email address is changed, the user's email verification status
     * is reset to {@code false}, requiring the new email address to be
     * verified again.
     * </p>
     *
     * @param updateUserProfileRequest request containing the user identifier
     *                                 and updated profile information
     * @return {@link GetUserResponse} containing the updated user data
     * @throws UserNotFoundException if no user exists with the supplied identifier
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@userAuthorization.isCurrentUser(#updateUserProfileRequest.id, authentication)"
    )
    @Transactional
    public GetUserResponse updateUserProfile(@P("updateUserProfileRequest") UpdateUserProfileRequest updateUserProfileRequest) {
        User user = userRepository.findById(updateUserProfileRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User not found with id" + updateUserProfileRequest.getId())
        );
        user.setFirstName(updateUserProfileRequest.getFirstName());
        user.setLastName(updateUserProfileRequest.getLastName());
        if (!user.getEmail().equals(updateUserProfileRequest.getEmail())) {
            user.setEmailVerified(false);
        }
        user.setEmail(updateUserProfileRequest.getEmail());
        User savedUser = userRepository.save(user);
        return returnGetUserResponse(savedUser);
    }

    /**
     * Updates a user's password.
     *
     * <p>
     * The method retrieves the user using the supplied identifier and
     * verifies the supplied old password against the stored encoded
     * password using {@link PasswordEncoder}.
     * </p>
     *
     * <p>
     * If the old password is valid, the new password is encoded and stored.
     * The updated user information is then returned.
     * </p>
     *
     * @param updateUserPasswordRequest request containing the user identifier,
     *                                  old password, and new password
     * @return {@link GetUserResponse} containing the updated user data
     * @throws UserNotFoundException        if no user exists with the supplied identifier
     * @throws UserPasswordInvalidException if the supplied old password does not match the stored password
     */
    @PreAuthorize(
            "@userAuthorization.isCurrentUser(#updateUserPasswordRequest.id, authentication)"
    )
    @Transactional
    public GetUserResponse updateUserPassword(@P("updateUserPasswordRequest") UpdateUserPasswordRequest updateUserPasswordRequest) {
        User user = userRepository.findById(updateUserPasswordRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + updateUserPasswordRequest.getId())
        );
        boolean passwordMatches = passwordEncoder.matches(updateUserPasswordRequest.getOldPassword(), user.getPassword());
        if (!passwordMatches) {
            throw new UserPasswordInvalidException("Invalid old password");
        } else {
            user.setPassword(passwordEncoder.encode(updateUserPasswordRequest.getNewPassword()));
            User savedUser = userRepository.save(user);
            return returnGetUserResponse(savedUser);
        }
    }

    /**
     * Deletes a user account by its unique identifier.
     *
     * <p>
     * The method first verifies that the requested user exists and then
     * permanently removes the user from the repository.
     * </p>
     *
     * @param deleteUserRequest request containing the identifier of the user
     *                          to delete
     * @return {@code true} when the user is successfully deleted
     * @throws UserNotFoundException if no user exists with the supplied identifier
     */
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@userAuthorization.isCurrentUser(#deleteUserRequest.id, authentication)"
    )
    @Transactional
    public Boolean deleteUser(@P("deleteUserRequest") DeleteUserRequest deleteUserRequest) {
        userRepository.findById(deleteUserRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User not found with id" + deleteUserRequest.getId())
        );
        userRepository.deleteById(deleteUserRequest.getId());
        return true;
    }
}
