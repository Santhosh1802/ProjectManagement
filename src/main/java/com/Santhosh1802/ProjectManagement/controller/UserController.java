package com.Santhosh1802.ProjectManagement.controller;

import com.Santhosh1802.ProjectManagement.dto.request.user.*;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserOwnedProjectsResponse;
import com.Santhosh1802.ProjectManagement.dto.response.user.GetUserResponse;
import com.Santhosh1802.ProjectManagement.service.UserService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/*
 * REST Controller responsible for user-related operations
 * such as creation, update, deletion, get user details by email, id and other fields.
 * */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;



    /*
     * create new user
     *
     * @param request user creation details CreateUserRequest
     * @return the newly created user
     * @throws UserAlreadyExistException if the email is already registered
     * @throws MethodArgumentNotValidException if fields are not valid
     * */
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

    /*
    * Get User by user id
    *
    * @param parameter as ?id= with the endpoint id is of type UUID
    * @return the user fetched via user id
    * @throws UserNotFoundException if user not found of id
    * @throws MethodArgumentNotValidException if fields are not valid
    * */
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

    /*
     * Get User by user email
     *
     * @param parameter as ?email= with the endpoint email is of type UUID
     * @return the user fetched via user email
     * @throws UserNotFoundException if user not found of email
     * @throws MethodArgumentNotValidException if fields are not valid
     * */
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUserById(@RequestParam String email) {
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

    /*
    * Update user profile updates firstName, LastName, email
    *
    * @param get the id and other fields from UpdateUserProfileRequest
    * @returns updated user details GetUserResponse
    * @throws UserNotFoundException if the user of id not found
    * @throws MethodArgumentNotValidException if fields are not valid
    * */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<GetUserResponse>> updateUserProfile(@Valid @RequestBody UpdateUserProfileRequest updateUserProfileRequest){
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

    /*
     * Update user profile updates firstName, LastName, email
     *
     * @param get the email and other fields from UpdateUserPasswordRequest
     * @returns updated user details GetUserResponse
     * @throws UserNotFoundException if the user of email not found
     * @throws MethodArgumentNotValidException if fields are not valid
     * */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<GetUserResponse>> updateUserPassword(@Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest){
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

    /*
    * Delete user by id
    *
    * @param request user id of type UUID
    * @returns true if user is deleted
    * @throws UserNotFoundException if user is not found of id
    * @throws MethodArgumentNotValidException if fields are not valid
    * */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@Valid @RequestBody DeleteUserRequest deleteUserRequest){
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


    /*
     * Get All users
     *
     * @param doesn't need any params
     * @returns users list of type GetUserResponse
     * @throws no exception is thrown
     * */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetUserResponse>>> getUsersBySearch(@Valid @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer size) {
        GetUserBySearchRequest getUserBySearchRequest = new GetUserBySearchRequest(keyword,page,size);
        List<GetUserResponse> result = userService.getUsersBySearch(getUserBySearchRequest);

        ApiResponse<List<GetUserResponse>> response = new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Users fetched",
                result
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/owned/projects")
    public ResponseEntity<ApiResponse<GetUserOwnedProjectsResponse>> getUserOwnedProjects(@RequestParam UUID id){
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
