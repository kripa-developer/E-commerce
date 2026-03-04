import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  expiresInMs: number;
}

export interface UserMeResponse {
  id: number;
  email: string;
  role: string;
  enabled: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = 'http://localhost:8811/api/v1';

  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { email, password });
  }

  register(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, { email, password });
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/refresh`, { refreshToken });
  }

  logout(refreshToken: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/logout`, { refreshToken });
  }

  me(): Observable<UserMeResponse> {
    return this.http.get<UserMeResponse>(`${this.baseUrl}/users/me`);
  }
}
