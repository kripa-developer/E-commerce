package com.novacart.auth.service;

import com.novacart.auth.dto.AuthResponse;
import com.novacart.auth.dto.LoginRequest;
import com.novacart.auth.dto.RegisterRequest;
import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse("Bearer", accessToken, refreshToken, jwtService.getAccessExpirationMs());
    }


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                UserRole.CUSTOMER,
                true
        );
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse("Bearer", accessToken, refreshToken, jwtService.getAccessExpirationMs());
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValidToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token"));

        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse("Bearer", accessToken, refreshToken, jwtService.getAccessExpirationMs());
    }
}
