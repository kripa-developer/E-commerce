package com.novacart.auth.service;

import com.novacart.auth.dto.LoginRequest;
import com.novacart.auth.dto.RegisterRequest;
import com.novacart.auth.repository.RefreshTokenRepository;
import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldReturnConflictWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@novacart.com", "Password@123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User("existing@novacart.com", "$2a$10$hash", UserRole.CUSTOMER, true)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Email already exists", ex.getReason());
    }

    @Test
    void refreshShouldReturnUnauthorizedWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.refresh("missing-token"));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Invalid refresh token", ex.getReason());
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("user@novacart.com", "wrong-password");
        User user = new User("user@novacart.com", "$2a$10$hash", UserRole.CUSTOMER, true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Invalid credentials", ex.getReason());
    }
}
