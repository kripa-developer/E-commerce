import { Component, computed, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { CartStateService } from '../../core/services/cart-state.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  searchQuery = '';
  mobileMenuOpen = signal(false);
  userMenuOpen = signal(false);
  scrolled = signal(false);

  constructor(
    public auth: AuthService,
    public cartState: CartStateService,
    private router: Router
  ) {}

  @HostListener('window:scroll')
  onScroll() { this.scrolled.set(window.scrollY > 20); }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/products'], { queryParams: { keyword: this.searchQuery.trim() } });
      this.searchQuery = '';
    }
  }

  toggleMobileMenu(): void { this.mobileMenuOpen.update(v => !v); }
  toggleUserMenu(): void   { this.userMenuOpen.update(v => !v); }
  closeUserMenu(): void    { this.userMenuOpen.set(false); }

  logout(): void {
    this.auth.logout();
    this.userMenuOpen.set(false);
  }
}
