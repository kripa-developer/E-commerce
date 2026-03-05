import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginPageComponent {
  mode: 'signin' | 'register' = 'signin';
  email = ''; password = ''; confirmPassword = '';
  loading = false;
  showPassword = false;

  constructor(
    private auth: AuthService,
    private toast: ToastService,
    private router: Router
  ) {
    if (this.auth.isLoggedIn()) this.router.navigate(['/']);
  }

  submit(): void {
    if (!this.email || !this.password) { this.toast.error('Please fill in all fields'); return; }
    if (this.mode === 'register' && this.password !== this.confirmPassword) { this.toast.error('Passwords do not match'); return; }
    if (this.password.length < 8) { this.toast.error('Password must be at least 8 characters'); return; }

    this.loading = true;
    const req$ = this.mode === 'register'
      ? this.auth.register(this.email, this.password)
      : this.auth.login(this.email, this.password);

    req$.subscribe({
      next: () => { this.toast.success(this.mode === 'register' ? 'Account created!' : 'Welcome back!'); this.router.navigate(['/']); },
      error: err => { this.toast.error(err?.error?.message || 'Authentication failed'); this.loading = false; }
    });
  }
}
