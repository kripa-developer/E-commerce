import { Component, computed, HostListener, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { SearchService, SearchSuggestion } from '../../core/services/search.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnDestroy {
  searchQuery = '';
  mobileMenuOpen = signal(false);
  userMenuOpen = signal(false);
  scrolled = signal(false);

  // Search autocomplete
  suggestions: SearchSuggestion[] = [];
  showSuggestions = false;
  loadingSuggestions = false;
  selectedIndex = -1;
  private searchSubject = new Subject<string>();
  private searchSub: Subscription;

  constructor(
    public auth: AuthService,
    public cartState: CartStateService,
    private router: Router,
    private searchService: SearchService
  ) {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(280),
      distinctUntilChanged(),
      switchMap(q => {
        if (!q || q.trim().length < 2) { this.suggestions = []; this.loadingSuggestions = false; return []; }
        this.loadingSuggestions = true;
        return this.searchService.getSuggestions(q);
      })
    ).subscribe(results => {
      this.suggestions = results;
      this.loadingSuggestions = false;
      this.showSuggestions = results.length > 0;
      this.selectedIndex = -1;
    });
  }

  ngOnDestroy(): void { this.searchSub.unsubscribe(); }

  @HostListener('window:scroll')
  onScroll() { this.scrolled.set(window.scrollY > 20); }

  @HostListener('document:click', ['$event'])
  onDocClick(e: Event) {
    const target = e.target as HTMLElement;
    if (!target.closest('.nav-search')) {
      this.showSuggestions = false;
      this.selectedIndex = -1;
    }
  }

  onSearchInput(): void {
    this.showSuggestions = false;
    this.searchSubject.next(this.searchQuery);
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (!this.showSuggestions) {
      if (event.key === 'Enter') this.onSearch();
      return;
    }
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedIndex = Math.min(this.selectedIndex + 1, this.suggestions.length - 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedIndex = Math.max(this.selectedIndex - 1, -1);
    } else if (event.key === 'Enter') {
      event.preventDefault();
      if (this.selectedIndex >= 0 && this.suggestions[this.selectedIndex]) {
        this.selectSuggestion(this.suggestions[this.selectedIndex]);
      } else {
        this.onSearch();
      }
    } else if (event.key === 'Escape') {
      this.showSuggestions = false;
      this.selectedIndex = -1;
    }
  }

  selectSuggestion(s: SearchSuggestion): void {
    this.showSuggestions = false;
    this.searchQuery = '';
    if (s.type === 'product' && s.slug) {
      this.router.navigate(['/products', s.slug]);
    }
  }

  onSearch(): void {
    this.showSuggestions = false;
    if (this.searchQuery.trim()) {
      this.router.navigate(['/products'], { queryParams: { keyword: this.searchQuery.trim() } });
      this.searchQuery = '';
    }
  }

  onSearchFocus(): void {
    if (this.suggestions.length > 0) this.showSuggestions = true;
  }

  toggleMobileMenu(): void { this.mobileMenuOpen.update(v => !v); }
  toggleUserMenu(): void   { this.userMenuOpen.update(v => !v); }
  closeUserMenu(): void    { this.userMenuOpen.set(false); }
  logout(): void { this.auth.logout(); this.userMenuOpen.set(false); }
}
