package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.auth.LoginUserRequest;
import com.Santhosh1802.ProjectManagement.dto.request.auth.RegisterUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.auth.RegisterUserResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service responsible for user authentication-related operations.
 *
 * <p>
 * This service handles user registration and post-authentication
 * login information updates. Credential authentication itself is
 * delegated to Spring Security's authentication infrastructure.
 * </p>
 *
 * <p>
 * User passwords are encoded using the configured
 * {@link PasswordEncoder} before being persisted.
 * </p>
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Converts a User entity into a GetUserResponse DTO.
     *
     * <p>
     * This method is used internally by the authentication service
     * to prevent exposing the User entity directly to the controller
     * or API response.
     * </p>
     *
     * @param user the User entity to convert
     * @return the corresponding GetUserResponse DTO
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
     * Registers a new user in the application.
     *
     * <p>
     * The method checks whether the supplied email address is already
     * registered. If the email does not exist, a new user is created
     * with a Bcrypt encoded password and the default {@link UserRole#USER}
     * role.
     * </p>
     *
     * <p>
     * Newly registered users are active by default and their email
     * address is initially marked as unverified.
     * </p>
     *
     * @param registerUserRequest registration details supplied by the client
     * @return details of the newly registered user
     * @throws UserAlreadyExistException if a user with the supplied email already exists
     */
    @Transactional
    public RegisterUserResponse registerUser(RegisterUserRequest registerUserRequest) {
        boolean userExist = userRepository.findByEmail(registerUserRequest.getEmail()).isPresent();
        if (!userExist) {
            User user = new User();
            user.setFirstName(registerUserRequest.getFirstName());
            user.setLastName(registerUserRequest.getLastName());
            user.setEmail(registerUserRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));

            user.setUserRole(UserRole.USER);
            user.setIsActive(true);
            user.setEmailVerified(false);

            User createdUser = userRepository.save(user);

            return new RegisterUserResponse(
                    createdUser.getId(),
                    createdUser.getFirstName(),
                    createdUser.getLastName(),
                    createdUser.getEmail(),
                    createdUser.getUserRole(),
                    createdUser.getIsActive(),
                    createdUser.getEmailVerified(),
                    createdUser.getCreatedAt(),
                    createdUser.getUpdatedAt(),
                    createdUser.getLastLoginAt());
        } else {
            throw new UserAlreadyExistException("Email already exists");
        }
    }

    /**
     * Updates the last login timestamp of an authenticated user.
     *
     * <p>
     * Password verification is intentionally not performed here.
     * AuthenticationManager and Spring Security are responsible for
     * validating the user's credentials before this method is called.
     * </p>
     *
     * <p>
     * After successful authentication, this method retrieves the user,
     * updates the {@code lastLoginAt} timestamp, persists the change,
     * and returns the updated user information.
     * </p>
     *
     * @param loginUserRequest login request containing the user's email
     * @return updated user information
     * @throws UserNotFoundException if no user exists with the supplied email
     */
    @Transactional
    public GetUserResponse updateLoginDetails(LoginUserRequest loginUserRequest) {
        User user = userRepository.findByEmail(loginUserRequest.getEmail()).orElseThrow(() -> new UserNotFoundException("User with email: " + loginUserRequest.getEmail() + " not found"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return returnGetUserResponse(user);
    }


}
