import { Injectable } from '@angular/core';

const ACCESS_KEY  = 'novacart.auth.accessToken';
const REFRESH_KEY = 'novacart.auth.refreshToken';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  get accessToken(): string | null  { return localStorage.getItem(ACCESS_KEY); }
  get refreshToken(): string | null { return localStorage.getItem(REFRESH_KEY); }

  setTokens(access: string, refresh: string): void {
    localStorage.setItem(ACCESS_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }
}
