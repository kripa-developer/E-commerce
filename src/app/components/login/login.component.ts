import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthApiService, UserMeResponse } from '../../core/auth/auth-api.service';
import { TokenStorageService } from '../../core/auth/token-storage.service';

type AuthMode = 'signin' | 'create' | 'forgot';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  authMode: AuthMode = 'signin';
  username = '';
  password = '';
  confirmPassword = '';
  recoveryEmail = '';
  rememberMe = false;
  showPassword = false;

  usernameError = '';
  passwordError = '';
  confirmPasswordError = '';
  recoveryEmailError = '';
  statusMessage = '';
  statusType: 'success' | 'error' | '' = '';
  isSubmitting = false;

  currentUser: UserMeResponse | null = null;

  constructor(
    private readonly authApi: AuthApiService,
    private readonly tokenStorage: TokenStorageService
  ) {
    this.bootstrapSession();
  }

  setMode(mode: AuthMode): void {
    this.authMode = mode;
    this.password = '';
    this.confirmPassword = '';
    this.recoveryEmail = '';
    this.clearErrors();
    this.setStatus('', '');
  }

  onTogglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.clearErrors();

    let isValid = true;
    const safeEmail = this.username.trim();
    const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(safeEmail);

    if (!validEmail) {
      this.usernameError = 'Enter a valid email address.';
      isValid = false;
    }

    if (this.authMode !== 'forgot' && this.password.length < 8) {
      this.passwordError = 'Password must be at least 8 characters long.';
      isValid = false;
    }

    if (this.authMode === 'create' && this.confirmPassword !== this.password) {
      this.confirmPasswordError = 'Passwords do not match.';
      isValid = false;
    }

    if (this.authMode === 'forgot') {
      const validRecoveryEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.recoveryEmail.trim());
      if (!validRecoveryEmail) {
        this.recoveryEmailError = 'Enter a valid recovery email address.';
        isValid = false;
      }
    }

    if (!isValid) {
      this.setStatus('Please correct the highlighted fields.', 'error');
      return;
    }

    if (this.authMode === 'forgot') {
      this.setStatus(`Reset instructions sent to ${this.recoveryEmail.trim()}.`, 'success');
      return;
    }

    this.isSubmitting = true;
    const request$ = this.authMode === 'create'
      ? this.authApi.register(safeEmail, this.password)
      : this.authApi.login(safeEmail, this.password);

    request$.subscribe({
      next: (response) => {
        this.tokenStorage.setTokens(response.accessToken, response.refreshToken);
        localStorage.setItem('novacart.session', JSON.stringify({
          username: safeEmail,
          rememberMe: this.rememberMe,
          loginAt: new Date().toISOString()
        }));

        const successText = this.authMode === 'create'
          ? `Account created! Welcome, ${safeEmail}.`
          : `Login successful! Welcome back, ${safeEmail}.`;

        this.loadCurrentUser(successText);
      },
      error: (error) => {
        const message = error?.error?.message ?? 'Authentication failed. Please try again.';
        this.setStatus(message, 'error');
        this.isSubmitting = false;
      }
    });
  }

  onLogout(): void {
    const refreshToken = this.tokenStorage.refreshToken;
    if (!refreshToken) {
      this.clearClientSession('Logged out.');
      return;
    }

    this.authApi.logout(refreshToken).subscribe({
      next: () => this.clearClientSession('Logged out successfully.'),
      error: () => this.clearClientSession('Logged out locally.')
    });
  }

  private bootstrapSession(): void {
    const accessToken = this.tokenStorage.accessToken;
    if (!accessToken) {
      return;
    }

    this.loadCurrentUser('Session restored.');
  }

  private loadCurrentUser(successMessage: string): void {
    this.authApi.me().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.username = user.email;
        this.setStatus(successMessage, 'success');
        this.isSubmitting = false;
      },
      error: () => {
        this.tokenStorage.clear();
        this.currentUser = null;
        this.isSubmitting = false;
      }
    });
  }

  private clearClientSession(message: string): void {
    this.tokenStorage.clear();
    localStorage.removeItem('novacart.session');
    this.currentUser = null;
    this.password = '';
    this.confirmPassword = '';
    this.setStatus(message, 'success');
  }

  private clearErrors(): void {
    this.usernameError = '';
    this.passwordError = '';
    this.confirmPasswordError = '';
    this.recoveryEmailError = '';
  }

  private setStatus(message: string, type: 'success' | 'error' | ''): void {
    this.statusMessage = message;
    this.statusType = type;
  }
}
