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
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


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
//                user.getOwnedProjects(),
//                user.getCreatedTasks(),
//                user.getAssignedTasks(),
//                user.getProjectMemberships()
        );
    }

    @Transactional(rollbackFor = {UserAlreadyExistException.class})
    public GetUserResponse createUser(CreateUserRequest createUserRequest) {
        boolean userExist = userRepository.findByEmail(createUserRequest.getEmail()).isPresent();
        if (!userExist) {
            User user = new User();
            user.setFirstName(createUserRequest.getFirstName());
            user.setLastName(createUserRequest.getLastName());
            user.setEmail(createUserRequest.getEmail());
            user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));

            user.setUserRole(UserRole.USER);
            user.setIsActive(true);
            user.setEmailVerified(false);

            User createdUser = userRepository.save(user);

            return returnGetUserResponse(createdUser);
        } else {
            throw new UserAlreadyExistException("Email already exists");
        }
    }


    public GetUserOwnedProjectsResponse getOwnedProjects(GetUserByIdRequest getUserByIdRequest) {
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
                getUserByIdRequest.getId(),projects
        );

    }


    public GetUserResponse getUserById(GetUserByIdRequest getUserByIdRequest) {

        User user = userRepository.findById(getUserByIdRequest.getId()).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + getUserByIdRequest.getId())
        );

        return returnGetUserResponse(user);

    }

    public GetUserResponse getUserByEmail(GetUserByEmailRequest getUserByEmailRequest) {
        User user = userRepository.findByEmail(getUserByEmailRequest.getEmail()).orElseThrow(() ->
                new UserNotFoundException("User not found with email" + getUserByEmailRequest.getEmail())
        );
        return returnGetUserResponse(user);
    }

    public List<GetUserResponse> getUsersBySearch(GetUserBySearchRequest getUserBySearchRequest) {

        Pageable pageable = PageRequest.of(getUserBySearchRequest.getPage(), getUserBySearchRequest.getSize(), Sort.by("createdAt").descending());
        Page<User> users = userRepository.searchUsers(getUserBySearchRequest.getKeyword(), pageable);
        List<GetUserResponse> userResponseList = new ArrayList<>();
        for (User user : users) {
            userResponseList.add(returnGetUserResponse(user));
        }
        return userResponseList;
    }

    @Transactional(rollbackFor = {UserNotFoundException.class})
    public GetUserResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest) {
        User user = userRepository.findById(updateUserProfileRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User not found with id" + updateUserProfileRequest.getId())
        );
        user.setFirstName(updateUserProfileRequest.getFirstName());
        user.setLastName(updateUserProfileRequest.getLastName());
        user.setEmail(updateUserProfileRequest.getEmail());
        User savedUser = userRepository.save(user);
        return returnGetUserResponse(savedUser);
    }

    @Transactional(rollbackFor = {UserNotFoundException.class, UserPasswordInvalidException.class})
    public GetUserResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
        User user = userRepository.findByEmail(updateUserPasswordRequest.getEmail()).orElseThrow(() ->
                new UserNotFoundException("User not found with email: " + updateUserPasswordRequest.getEmail())
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

    @Transactional(rollbackFor = {UserNotFoundException.class})
    public Boolean deleteUser(DeleteUserRequest deleteUserRequest) {
        userRepository.findById(deleteUserRequest.getId()).orElseThrow(() ->
                new UserNotFoundException("User not found with id" + deleteUserRequest.getId())
        );
        userRepository.deleteById(deleteUserRequest.getId());
        return true;
    }
}
