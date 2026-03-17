import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-logo">🛒 NovaCart</div>

        <div *ngIf="!submitted">
          <h1 class="auth-title">Forgot password?</h1>
          <p class="auth-sub">Enter your email and we'll send you a reset link.</p>

          <div class="error-banner" *ngIf="error">{{ error }}</div>

          <div class="field">
            <label>Email address</label>
            <input type="email" class="input" placeholder="you@example.com"
                   [(ngModel)]="email" (keyup.enter)="submit()" [disabled]="loading" />
          </div>

          <button class="btn btn-primary btn-full" (click)="submit()" [disabled]="loading">
            <span class="btn-spinner" *ngIf="loading"></span>
            {{ loading ? 'Sending...' : 'Send Reset Link' }}
          </button>

          <div class="auth-footer">
            <a routerLink="/login">← Back to Sign In</a>
          </div>
        </div>

        <!-- Success state -->
        <div *ngIf="submitted" class="success-state">
          <div class="success-icon">📧</div>
          <h2>Check your inbox</h2>
          <p class="auth-sub">
            If <strong>{{ email }}</strong> is registered, you'll receive a reset link shortly.
            Check your spam folder if you don't see it.
          </p>
          <a routerLink="/login" class="btn btn-primary btn-full">Back to Sign In</a>
        </div>
      </div>
    </div>
  `,
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  submitted = false;
  error = '';

  constructor(private http: HttpClient) {}

  submit(): void {
    this.error = '';
    if (!this.email) { this.error = 'Please enter your email'; return; }
    this.loading = true;
    this.http.post(`${environment.apiUrl}/auth/forgot-password`, { email: this.email }).subscribe({
      next: () => { this.submitted = true; this.loading = false; },
      error: () => { this.submitted = true; this.loading = false; } // still show success (security)
    });
  }
}