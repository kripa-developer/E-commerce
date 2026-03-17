import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { AuthResponse, UserMe } from '../models';
import { environment } from '../../../environments/environment';

// const BASE = 'http://localhost:8811/api/v1';

const BASE = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _user = signal<UserMe | null>(null);

  readonly user       = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly isAdmin    = computed(() => this._user()?.role === 'ADMIN');

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
    private router: Router
  ) {
    if (this.tokenStorage.accessToken) this.loadMe();
  }

  login(email: string, password: string): Observable<UserMe> {
    return this.http.post<AuthResponse>(`${BASE}/auth/login`, { email, password }).pipe(
      tap(r => this.tokenStorage.setTokens(r.accessToken, r.refreshToken)),
      switchMap(() => this.http.get<UserMe>(`${BASE}/users/me`)),
      tap(u => this._user.set(u))
    );
  }

  register(email: string, password: string): Observable<UserMe> {
    return this.http.post<AuthResponse>(`${BASE}/auth/register`, { email, password }).pipe(
      tap(r => this.tokenStorage.setTokens(r.accessToken, r.refreshToken)),
      switchMap(() => this.http.get<UserMe>(`${BASE}/users/me`)),
      tap(u => this._user.set(u))
    );
  }

  logout(): void {
    const rt = this.tokenStorage.refreshToken;
    if (rt) this.http.post(`${BASE}/auth/logout`, { refreshToken: rt }).subscribe();
    this.tokenStorage.clear();
    this._user.set(null);
    this.router.navigate(['/']);
  }

  loadMe(): void {
    this.http.get<UserMe>(`${BASE}/users/me`).subscribe({
      next: u => this._user.set(u),
      error: () => { this.tokenStorage.clear(); this._user.set(null); }
    });
  }
}