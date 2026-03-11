import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-logo">🛒 NovaCart</div>

        <!-- Invalid token -->
        <div *ngIf="!token" class="success-state">
          <div class="success-icon">❌</div>
          <h2>Invalid link</h2>
          <p class="auth-sub">This reset link is invalid or has expired.</p>
          <a routerLink="/forgot-password" class="btn btn-primary btn-full">Request New Link</a>
        </div>

        <!-- Reset form -->
        <div *ngIf="token && !success">
          <h1 class="auth-title">Set new password</h1>
          <p class="auth-sub">Must be at least 8 characters.</p>

          <div class="error-banner" *ngIf="error">{{ error }}</div>

          <div class="field">
            <label>New Password</label>
            <div class="input-wrap">
              <input [type]="showPw ? 'text' : 'password'" class="input"
                     placeholder="Min 8 characters" [(ngModel)]="newPassword" [disabled]="loading" />
              <button type="button" class="pw-toggle" (click)="showPw = !showPw">
                {{ showPw ? 'Hide' : 'Show' }}
              </button>
            </div>
          </div>

          <div class="field">
            <label>Confirm Password</label>
            <input type="password" class="input" placeholder="Repeat password"
                   [(ngModel)]="confirmPassword" (keyup.enter)="submit()" [disabled]="loading" />
          </div>

          <button class="btn btn-primary btn-full" (click)="submit()" [disabled]="loading">
            <span class="btn-spinner" *ngIf="loading"></span>
            {{ loading ? 'Saving...' : 'Reset Password' }}
          </button>
        </div>

        <!-- Success -->
        <div *ngIf="success" class="success-state">
          <div class="success-icon">✅</div>
          <h2>Password updated!</h2>
          <p class="auth-sub">Your password has been changed successfully.</p>
          <a routerLink="/login" class="btn btn-primary btn-full">Sign In</a>
        </div>
      </div>
    </div>
  `,
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  newPassword = '';
  confirmPassword = '';
  showPw = false;
  loading = false;
  success = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
  }

  submit(): void {
    this.error = '';
    if (!this.newPassword || this.newPassword.length < 8) {
      this.error = 'Password must be at least 8 characters'; return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Passwords do not match'; return;
    }
    this.loading = true;
    this.http.post(`${environment.apiUrl}/auth/reset-password`, {
      token: this.token,
      newPassword: this.newPassword
    }).subscribe({
      next: () => { this.success = true; this.loading = false; },
      error: err => {
        this.error = err?.error?.message || 'This link is invalid or has expired';
        this.loading = false;
      }
    });
  }
}