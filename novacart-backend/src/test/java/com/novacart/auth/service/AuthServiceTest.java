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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    // ── Shared test data ──
    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User("user@novacart.com", "$2a$10$hashedpassword", UserRole.CUSTOMER, true);
    }

    // ════════════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return AuthResponse on valid credentials")
        void shouldReturnAuthResponseOnValidCredentials() {
            LoginRequest request = new LoginRequest("user@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(request.password(), activeUser.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");
            when(jwtService.getAccessExpirationMs()).thenReturn(3600000L);

            AuthResponse response = authService.login(request);

            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.expiresInMs()).isEqualTo(3600000L);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should throw 401 when user not found")
        void shouldThrow401WhenUserNotFound() {
            LoginRequest request = new LoginRequest("unknown@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
            assertThat(ex.getReason()).isEqualTo("Invalid credentials");
        }

        @Test
        @DisplayName("should throw 401 when password does not match")
        void shouldThrow401WhenPasswordDoesNotMatch() {
            LoginRequest request = new LoginRequest("user@novacart.com", "wrong-password");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(request.password(), activeUser.getPasswordHash())).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
            assertThat(ex.getReason()).isEqualTo("Invalid credentials");
        }

        @Test
        @DisplayName("should throw 401 when user is disabled")
        void shouldThrow401WhenUserIsDisabled() {
            User disabledUser = new User("user@novacart.com", "$2a$10$hash", UserRole.CUSTOMER, false);
            LoginRequest request = new LoginRequest("user@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(disabledUser));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
        }

        @Test
        @DisplayName("should persist refresh token on successful login")
        void shouldPersistRefreshTokenOnSuccessfulLogin() {
            LoginRequest request = new LoginRequest("user@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");
            when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);

            authService.login(request);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getToken()).isEqualTo("refresh-token");
            assertThat(captor.getValue().isRevoked()).isFalse();
        }
    }

    // ════════════════════════════════════════════════
    // REGISTER
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register new user and return AuthResponse")
        void shouldRegisterNewUserAndReturnAuthResponse() {
            RegisterRequest request = new RegisterRequest("new@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(request.password())).thenReturn("$2a$10$encoded");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
            when(jwtService.getAccessExpirationMs()).thenReturn(3600000L);

            AuthResponse response = authService.register(request);

            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.accessToken()).isEqualTo("access-token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should save user with CUSTOMER role")
        void shouldSaveUserWithCustomerRole() {
            RegisterRequest request = new RegisterRequest("new@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

            authService.register(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.CUSTOMER);
            assertThat(captor.getValue().isEnabled()).isTrue();
            assertThat(captor.getValue().getEmail()).isEqualTo("new@novacart.com");
        }

        @Test
        @DisplayName("should throw 409 when email already exists")
        void shouldThrow409WhenEmailAlreadyExists() {
            RegisterRequest request = new RegisterRequest("existing@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.register(request));

            assertThat(ex.getStatusCode().value()).isEqualTo(409);
            assertThat(ex.getReason()).isEqualTo("Email already exists");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePasswordBeforeSaving() {
            RegisterRequest request = new RegisterRequest("new@novacart.com", "Password@123");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encoded");
            when(jwtService.generateAccessToken(any())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

            authService.register(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$encoded");
            assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("Password@123");
        }
    }

    // ════════════════════════════════════════════════
    // REFRESH TOKEN
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("should return new tokens on valid refresh token")
        void shouldReturnNewTokensOnValidRefreshToken() {
            RefreshToken storedToken = new RefreshToken("valid-token", activeUser,
                    Instant.now().plusSeconds(3600), false);
            Claims claims = mock(Claims.class);

            when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(storedToken));
            when(jwtService.isValidToken("valid-token")).thenReturn(true);
            when(jwtService.parseClaims("valid-token")).thenReturn(claims);
            when(claims.getSubject()).thenReturn("user@novacart.com");
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(activeUser));
            when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(activeUser)).thenReturn("new-refresh-token");
            when(jwtService.getAccessExpirationMs()).thenReturn(3600000L);

            AuthResponse response = authService.refresh("valid-token");

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(storedToken.isRevoked()).isTrue(); // old token revoked
        }

        @Test
        @DisplayName("should throw 401 when token not found")
        void shouldThrow401WhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh("missing-token"));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
            assertThat(ex.getReason()).isEqualTo("Invalid refresh token");
        }

        @Test
        @DisplayName("should throw 401 when token is revoked")
        void shouldThrow401WhenTokenIsRevoked() {
            RefreshToken revokedToken = new RefreshToken("revoked-token", activeUser,
                    Instant.now().plusSeconds(3600), true);
            when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh("revoked-token"));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
        }

        @Test
        @DisplayName("should throw 401 when token is expired")
        void shouldThrow401WhenTokenIsExpired() {
            RefreshToken expiredToken = new RefreshToken("expired-token", activeUser,
                    Instant.now().minusSeconds(3600), false);
            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh("expired-token"));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
        }

        @Test
        @DisplayName("should throw 401 when JWT signature is invalid")
        void shouldThrow401WhenJwtSignatureIsInvalid() {
            RefreshToken storedToken = new RefreshToken("bad-token", activeUser,
                    Instant.now().plusSeconds(3600), false);
            when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.of(storedToken));
            when(jwtService.isValidToken("bad-token")).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.refresh("bad-token"));

            assertThat(ex.getStatusCode().value()).isEqualTo(401);
        }
    }

    // ════════════════════════════════════════════════
    // LOGOUT
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("should revoke refresh token on logout")
        void shouldRevokeRefreshTokenOnLogout() {
            RefreshToken storedToken = new RefreshToken("valid-token", activeUser,
                    Instant.now().plusSeconds(3600), false);
            when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(storedToken));

            authService.logout("valid-token");

            assertThat(storedToken.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("should do nothing when token not found")
        void shouldDoNothingWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> authService.logout("unknown-token"));
        }
    }

    // ════════════════════════════════════════════════
    // FORGOT PASSWORD
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPassword {

        @Test
        @DisplayName("should send reset email when user exists")
        void shouldSendResetEmailWhenUserExists() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(activeUser));

            authService.forgotPassword("user@novacart.com");

            verify(passwordResetTokenRepository).deleteAllByUserId(activeUser.getId());
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq("user@novacart.com"), anyString());
        }

        @Test
        @DisplayName("should do nothing silently when email not found")
        void shouldDoNothingSilentlyWhenEmailNotFound() {
            when(userRepository.findByEmail("unknown@novacart.com")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> authService.forgotPassword("unknown@novacart.com"));

            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
            verify(passwordResetTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should delete existing tokens before creating new one")
        void shouldDeleteExistingTokensBeforeCreatingNew() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(activeUser));

            authService.forgotPassword("user@novacart.com");

            // deleteAllByUserId must be called before save
            var inOrder = inOrder(passwordResetTokenRepository);
            inOrder.verify(passwordResetTokenRepository).deleteAllByUserId(activeUser.getId());
            inOrder.verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("should save token expiring in 15 minutes")
        void shouldSaveTokenExpiringIn15Minutes() {
            when(userRepository.findByEmail("user@novacart.com")).thenReturn(Optional.of(activeUser));
            Instant before = Instant.now().plusSeconds(14 * 60);
            Instant after = Instant.now().plusSeconds(16 * 60);

            authService.forgotPassword("user@novacart.com");

            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(passwordResetTokenRepository).save(captor.capture());
            // token not yet expired
            assertThat(captor.getValue().isExpired()).isFalse();
        }
    }

    // ════════════════════════════════════════════════
    // RESET PASSWORD
    // ════════════════════════════════════════════════
    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("should update password on valid token")
        void shouldUpdatePasswordOnValidToken() {
            PasswordResetToken resetToken = new PasswordResetToken(
                    "valid-token", activeUser, Instant.now().plusSeconds(900));
            when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode("NewPassword@123")).thenReturn("$2a$10$newHash");

            authService.resetPassword("valid-token", "NewPassword@123");

            assertThat(activeUser.getPasswordHash()).isEqualTo("$2a$10$newHash");
            assertThat(resetToken.isUsed()).isTrue();
            verify(userRepository).save(activeUser);
            verify(passwordResetTokenRepository).save(resetToken);
        }

        @Test
        @DisplayName("should throw 400 when token not found")
        void shouldThrow400WhenTokenNotFound() {
            when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.resetPassword("bad-token", "NewPassword@123"));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).isEqualTo("Invalid or expired reset link");
        }

        @Test
        @DisplayName("should throw 400 when token already used")
        void shouldThrow400WhenTokenAlreadyUsed() {
            PasswordResetToken usedToken = new PasswordResetToken(
                    "used-token", activeUser, Instant.now().plusSeconds(900));
            usedToken.markUsed();
            when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.resetPassword("used-token", "NewPassword@123"));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).isEqualTo("This reset link has already been used");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw 400 when token is expired")
        void shouldThrow400WhenTokenIsExpired() {
            PasswordResetToken expiredToken = new PasswordResetToken(
                    "expired-token", activeUser, Instant.now().minusSeconds(1));
            when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.resetPassword("expired-token", "NewPassword@123"));

            assertThat(ex.getStatusCode().value()).isEqualTo(400);
            assertThat(ex.getReason()).isEqualTo("This reset link has expired. Please request a new one");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should mark token as used after successful reset")
        void shouldMarkTokenAsUsedAfterSuccessfulReset() {
            PasswordResetToken resetToken = new PasswordResetToken(
                    "valid-token", activeUser, Instant.now().plusSeconds(900));
            when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newHash");

            authService.resetPassword("valid-token", "NewPassword@123");

            assertThat(resetToken.isUsed()).isTrue();
        }
    }
}