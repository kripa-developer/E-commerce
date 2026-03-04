import { Injectable } from '@angular/core';

const ACCESS_TOKEN_KEY = 'novacart.auth.accessToken';
const REFRESH_TOKEN_KEY = 'novacart.auth.refreshToken';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
