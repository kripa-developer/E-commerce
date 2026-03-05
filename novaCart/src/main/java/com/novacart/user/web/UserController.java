package com.novacart.user.web;

import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** GET /api/v1/users/me */
    @GetMapping("/me")
    public UserMeResponse getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return UserMeResponse.from(user);
    }

    /** PATCH /api/v1/users/me/password */
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest req) {

        User user = getUser(userDetails);

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        String newHash = passwordEncoder.encode(req.newPassword());
        userRepository.updatePasswordHash(user.getId(), newHash);
    }

    // ── helpers ──
    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // ── DTOs ──
    public record UserMeResponse(Long id, String email, String role, boolean enabled) {
        public static UserMeResponse from(User u) {
            return new UserMeResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.isEnabled()
            );
        }
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "New password must be at least 8 characters") String newPassword
    ) {}
}
