package com.novacart.auth.service;

import com.novacart.auth.domain.PasswordResetToken;
import com.novacart.auth.domain.RefreshToken;
import com.novacart.auth.dto.AuthResponse;
import com.novacart.auth.dto.LoginRequest;
import com.novacart.auth.dto.RegisterRequest;
import com.novacart.auth.repository.PasswordResetTokenRepository;
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
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
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

    // ── Forgot Password ──
    @Transactional
    public void forgotPassword(String email) {
        // Always return success even if email not found (security best practice)
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete any existing tokens for this user
            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token, user, Instant.now().plusSeconds(15 * 60) // 15 minutes
            );
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(email, token);
        });
    }

    // ── Reset Password ──
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(BAD_REQUEST, "This reset link has already been used");
        }
        if (resetToken.isExpired()) {
            throw new ResponseStatusException(BAD_REQUEST, "This reset link has expired. Please request a new one");
        }

        User user = resetToken.getUser();
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
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