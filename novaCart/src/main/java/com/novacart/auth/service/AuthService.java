package com.novacart.auth.service;

import com.novacart.auth.domain.RefreshToken;
import com.novacart.auth.dto.AuthResponse;
import com.novacart.auth.dto.LoginRequest;
import com.novacart.auth.dto.RegisterRequest;
import com.novacart.auth.repository.RefreshTokenRepository;
import com.novacart.user.domain.User;
import com.novacart.user.domain.UserRole;
import com.novacart.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, refreshTokenValue);

        return new AuthResponse("Bearer", accessToken, refreshTokenValue, jwtService.getAccessExpirationMs());
    }

    @Transactional
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
        String refreshTokenValue = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, refreshTokenValue);
        return new AuthResponse("Bearer", accessToken, refreshTokenValue, jwtService.getAccessExpirationMs());
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now()) || !jwtService.isValidToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token"));

        storedToken.revoke();

        String accessToken = jwtService.generateAccessToken(user);
        String rotatedRefreshToken = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, rotatedRefreshToken);

        return new AuthResponse("Bearer", accessToken, rotatedRefreshToken, jwtService.getAccessExpirationMs());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(RefreshToken::revoke);
    }

    private void persistRefreshToken(User user, String tokenValue) {
        RefreshToken refreshToken = new RefreshToken(
                tokenValue,
                user,
                Instant.now().plusMillis(jwtService.getRefreshExpirationMs()),
                false
        );
        refreshTokenRepository.save(refreshToken);
    }
}
