package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.auth.RegisterUserRequest;
import com.Santhosh1802.ProjectManagement.dto.response.auth.RegisterUserResponse;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.user.UserAlreadyExistException;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = {UserAlreadyExistException.class})
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



}
