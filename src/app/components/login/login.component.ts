import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type AuthMode = 'signin' | 'create';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  authMode: AuthMode = 'signin';
  username = '';
  password = '';
  confirmPassword = '';
  rememberMe = false;
  showPassword = false;

  usernameError = '';
  passwordError = '';
  confirmPasswordError = '';
  statusMessage = '';
  statusType: 'success' | 'error' | '' = '';

  constructor() {
    const previousSession = localStorage.getItem('novacart.session');
    if (!previousSession) return;

    try {
      const { username, rememberMe } = JSON.parse(previousSession) as { username?: string; rememberMe?: boolean };
      if (rememberMe && username) {
        this.username = username;
        this.rememberMe = true;
        this.setStatus(`Welcome back, ${username}. Continue where you left off.`, 'success');
      }
    } catch {
      localStorage.removeItem('novacart.session');
    }
  }

  setMode(mode: AuthMode): void {
    this.authMode = mode;
    this.password = '';
    this.confirmPassword = '';
    this.clearErrors();
    this.setStatus('', '');
  }

  onTogglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.clearErrors();

    let isValid = true;
    if (this.username.trim().length < 3) {
      this.usernameError = 'Username must be at least 3 characters long.';
      isValid = false;
    }

    if (this.password.length < 8) {
      this.passwordError = 'Password must be at least 8 characters long.';
      isValid = false;
    }

    if (this.authMode === 'create' && this.confirmPassword !== this.password) {
      this.confirmPasswordError = 'Passwords do not match.';
      isValid = false;
    }

    if (!isValid) {
      this.setStatus('Please correct the highlighted fields.', 'error');
      return;
    }

    const safeUsername = this.username.trim();
    localStorage.setItem('novacart.session', JSON.stringify({
      username: safeUsername,
      rememberMe: this.rememberMe,
      loginAt: new Date().toISOString()
    }));

    if (this.authMode === 'create') {
      this.setStatus(`Account created! Welcome, ${safeUsername}.`, 'success');
      this.setMode('signin');
      this.username = safeUsername;
      return;
    }

    this.setStatus(`Login successful! Welcome back, ${safeUsername}.`, 'success');
  }

  private clearErrors(): void {
    this.usernameError = '';
    this.passwordError = '';
    this.confirmPasswordError = '';
  }

  private setStatus(message: string, type: 'success' | 'error' | ''): void {
    this.statusMessage = message;
    this.statusType = type;
  }
}
