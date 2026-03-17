package com.novacart.user.service;

import com.novacart.user.domain.User;
import com.novacart.user.dto.UserMeResponse;
import com.novacart.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserMeResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return new UserMeResponse(user.getId(), user.getEmail(), user.getRole().name(), user.isEnabled());
    }
}
