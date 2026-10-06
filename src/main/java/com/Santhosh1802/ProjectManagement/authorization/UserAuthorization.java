package com.Santhosh1802.ProjectManagement.authorization;

import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAuthorization")
public class UserAuthorization {

    private final UserRepository userRepository;

    public UserAuthorization(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isCurrentUser(
            UUID userId,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user ->
                        user.getEmail()
                                .equals(authentication.getName())
                )
                .orElse(false);
    }

    private boolean isAuthenticated(
            Authentication authentication
    ) {
        return authentication != null
                && authentication.isAuthenticated();
    }
}
