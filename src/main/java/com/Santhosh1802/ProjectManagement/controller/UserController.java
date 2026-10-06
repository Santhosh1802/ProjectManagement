package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.user.*;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserOwnedProjectsResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserPasswordInvalidException;
import com.Santhosh1802.ProjectManagement.service.UserService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller responsible for user-related operations.
 *
 * <p>
 * Provides endpoints for creating users, retrieving user information,
 * searching and filtering users, updating profiles and passwords,
 * deleting users, and retrieving projects owned by a user.
 * </p>
 *
 * <p>
 * Base endpoint: {@code /api/users}
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * Creates a new user account.
     *
     * <p>
     * Accepts the user's registration information, validates the request,
     * and delegates user creation to the {@link UserService}.
     * </p>
     *
     * <p>
     * The service creates the user with the default {@code USER} role,
     * encodes the password, and persists the user information.
     * </p>
     *
     * @param createUserRequest user creation details including first name,
     *                          last name, email, and password
     * @return {@link ResponseEntity} containing the newly created user
     * with HTTP status {@code 201 CREATED}
     * @throws UserAlreadyExistException       if a user with the supplied email already exists
     * @throws MethodArgumentNotValidException if the request contains invalid or missing fields
     * @apiNote Endpoint: {@code POST /api/users/create}
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GetUserResponse>> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        GetUserResponse createUserResponse = userService.createUser(createUserRequest);
        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.CREATED.value(),
                "User created successfully",
                createUserResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * <p>
     * Accepts a UUID through the request parameter and delegates the
     * lookup operation to the {@link UserService}.
     * </p>
     *
     * @param id unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the requested user
     * with HTTP status {@code 200 OK}
     * @throws UserNotFoundException if no user exists with the supplied identifier
     * @apiNote Endpoint: {@code GET /api/users/id?id={id}}
     */
    @GetMapping("/id")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUserById(@RequestParam UUID id) {
        GetUserByIdRequest getUserByIdRequest = new GetUserByIdRequest(id);
        GetUserResponse getUserResponse = userService.getUserById(getUserByIdRequest);

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User fetched successfully",
                getUserResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Retrieves a user by their email address.
     *
     * <p>
     * Accepts the user's email address as a request parameter and
     * delegates the lookup operation to the {@link UserService}.
     * </p>
     *
     * @param email email address of the user to retrieve
     * @return {@link ResponseEntity} containing the requested user
     * with HTTP status {@code 200 OK}
     * @throws UserNotFoundException if no user exists with the supplied email address
     * @apiNote Endpoint: {@code GET /api/users/email?email={email}}
     */
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUserByEmail(@RequestParam String email) {
        GetUserByEmailRequest getUserByEmailRequest = new GetUserByEmailRequest(email);
        GetUserResponse getUserResponse = userService.getUserByEmail(getUserByEmailRequest);

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User fetched successfully",
                getUserResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Updates a user's profile information.
     *
     * <p>
     * Accepts the user's identifier along with updated profile information
     * such as first name, last name, and email address. The request is
     * validated before being delegated to the {@link UserService}.
     * </p>
     *
     * <p>
     * If the user's email address is changed, the service marks the email
     * as unverified.
     * </p>
     *
     * @param updateUserProfileRequest request containing the user identifier
     *                                 and updated profile information
     * @return {@link ResponseEntity} containing the updated user details
     * with HTTP status {@code 200 OK}
     * @throws UserNotFoundException           if no user exists with the supplied identifier
     * @throws MethodArgumentNotValidException if the request contains invalid or missing fields
     * @apiNote Endpoint: {@code PUT /api/users/profile}
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<GetUserResponse>> updateUserProfile(@Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest) {
        GetUserResponse getUserResponse = userService.updateUserProfile(updateUserProfileRequest);

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User profile updated successfully",
                getUserResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Updates a user's password.
     *
     * <p>
     * Accepts the user's identifier, current password, and new password.
     * The request is validated and delegated to the {@link UserService}.
     * </p>
     *
     * <p>
     * The service verifies the supplied current password before replacing
     * it with the securely encoded new password.
     * </p>
     *
     * @param updateUserPasswordRequest request containing the user identifier,
     *                                  current password, and new password
     * @return {@link ResponseEntity} containing the updated user details
     * with HTTP status {@code 200 OK}
     * @throws UserNotFoundException           if no user exists with the supplied identifier
     * @throws UserPasswordInvalidException    if the supplied current password is incorrect
     * @throws MethodArgumentNotValidException if the request contains invalid or missing fields
     * @apiNote Endpoint: {@code PUT /api/users/password}
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<GetUserResponse>> updateUserPassword(@Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
        GetUserResponse getUserResponse = userService.updateUserPassword(updateUserPasswordRequest);

        ApiResponse<GetUserResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User password updated successfully",
                getUserResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * Deletes a user account by its unique identifier.
     *
     * <p>
     * Accepts the user's identifier in the request body and delegates
     * the deletion operation to the {@link UserService}.
     * </p>
     *
     * @param deleteUserRequest request containing the identifier of the
     *                          user to delete
     * @return {@link ResponseEntity} containing {@code true} when the
     * user is successfully deleted, with HTTP status
     * {@code 200 OK}
     * @throws UserNotFoundException           if no user exists with the supplied identifier
     * @throws MethodArgumentNotValidException if the request contains invalid or missing fields
     * @apiNote Endpoint: {@code DELETE /api/users/delete}
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@Valid @RequestBody DeleteUserRequest deleteUserRequest) {
        boolean deleteResponse = userService.deleteUser(deleteUserRequest);

        ApiResponse<Boolean> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "User deleted successfully",
                deleteResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    /**
     * Searches and retrieves users using pagination, filtering, and sorting.
     *
     * <p>
     * Supports searching users by keyword and filtering by attributes such
     * as user role, account activation status, and email verification status.
     * The results can also be sorted using the supported user fields and
     * returned in configurable pages.
     * </p>
     *
     * <p>
     * The request parameters are mapped to {@link GetUserBySearchRequest}
     * and delegated to the {@link UserService}, which performs the
     * database-level pagination and filtering.
     * </p>
     *
     * @param request search, filter, pagination, and sorting parameters
     * @return {@link ResponseEntity} containing a paginated collection of
     * users with HTTP status {@code 200 OK}
     * @apiNote Endpoint: {@code GET /api/users/all}
     *
     * <p>
     * Example:
     * {@code GET /api/users/all?page=0&size=10&keyword=santhosh&role=USER&isActive=true&sortBy=createdAt&sortOrder=desc}
     * </p>
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<GetUserResponse>>>
    getUsers(
            @Valid @ModelAttribute GetUserBySearchRequest request
    ) {

        PageResponse<GetUserResponse> users =
                userService.getUsersBySearch(request);

        ApiResponse<PageResponse<GetUserResponse>> response =
                new ApiResponse<>(
                        LocalDateTime.now(),
                        HttpStatus.OK.value(),
                        "Users fetched successfully",
                        users
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all projects owned by a specific user.
     *
     * <p>
     * Accepts the user's unique identifier as a request parameter and
     * delegates the project ownership lookup to the {@link UserService}.
     * </p>
     *
     * <p>
     * The returned response contains the user identifier and the projects
     * for which that user is currently the owner.
     * </p>
     *
     * @param id unique identifier of the user whose owned projects
     *           should be retrieved
     * @return {@link ResponseEntity} containing the user's owned projects
     * with HTTP status {@code 200 OK}
     * @throws UserNotFoundException if no user exists with the supplied identifier
     * @apiNote Endpoint: {@code GET /api/users/owned/projects?id={id}}
     */
    @GetMapping("/owned/projects")
    public ResponseEntity<ApiResponse<GetUserOwnedProjectsResponse>> getUserOwnedProjects(@RequestParam UUID id) {
        GetUserByIdRequest getUserByIdRequest = new GetUserByIdRequest(id);
        GetUserOwnedProjectsResponse getUserOwnedProjectsResponse = userService.getOwnedProjects(getUserByIdRequest);
        ApiResponse<GetUserOwnedProjectsResponse> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Owned projects fetched",
                getUserOwnedProjectsResponse
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


}
