package com.novacart.user.web;

import com.novacart.user.repository.UserRepository;
import com.novacart.user.service.UserService;
import com.novacart.user.dto.UserMeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** GET /api/v1/users/me */
    @GetMapping("/me")
    public UserMeResponse getMe(@AuthenticationPrincipal String email) {
        return userService.getCurrentUser(email);
    }

    /** PATCH /api/v1/users/me/password */
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequest req) {

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        userRepository.updatePasswordHash(user.getId(), passwordEncoder.encode(req.newPassword()));
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "New password must be at least 8 characters") String newPassword
    ) {}
}