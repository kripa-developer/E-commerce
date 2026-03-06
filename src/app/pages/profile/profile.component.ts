import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { OrderService, ReviewService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { UserMe, UserAddress, Review } from '../../core/models';
import { environment } from '../../../environments/environment';

// const BASE = 'http://localhost:8811/api/v1';

const BASE = environment.apiUrl;

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  activeTab: 'overview' | 'addresses' | 'reviews' | 'security' = 'overview';

  user: UserMe | null = null;
  totalOrders = 0;
  totalReviews = 0;

  // Addresses
  addresses: UserAddress[] = [];
  loadingAddresses = false;
  showAddressForm = false;
  editingAddress: UserAddress | null = null;
  savingAddress = false;
  addressForm = this.emptyAddress();

  // Reviews
  reviews: Review[] = [];
  loadingReviews = false;

  // Security (change password)
  pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  showCurrentPw = false;
  showNewPw = false;
  savingPw = false;
  pwError = '';
  pwSuccess = false;

  constructor(
    public auth: AuthService,
    private orderService: OrderService,
    private reviewService: ReviewService,
    private toast: ToastService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.user = this.auth.user();
    // Load order count
    this.orderService.getMyOrders(0, 1).subscribe(p => this.totalOrders = p.totalElements);
    // Load review count
    this.reviewService.getMyReviews(0, 1).subscribe(p => this.totalReviews = p.totalElements);
  }

  switchTab(tab: typeof this.activeTab): void {
    this.activeTab = tab;
    if (tab === 'addresses' && this.addresses.length === 0) this.loadAddresses();
    if (tab === 'reviews' && this.reviews.length === 0) this.loadReviews();
  }

  // ── Addresses ──
  loadAddresses(): void {
    this.loadingAddresses = true;
    this.http.get<UserAddress[]>(`${BASE}/addresses`).subscribe({
      next: a => { this.addresses = a; this.loadingAddresses = false; },
      error: () => this.loadingAddresses = false
    });
  }

  openAddAddress(): void {
    this.editingAddress = null;
    this.addressForm = this.emptyAddress();
    this.showAddressForm = true;
  }

  openEditAddress(addr: UserAddress): void {
    this.editingAddress = addr;
    this.addressForm = { ...addr };
    this.showAddressForm = true;
  }

  saveAddress(): void {
    this.savingAddress = true;
    const req$ = this.editingAddress
      ? this.http.put<UserAddress>(`${BASE}/addresses/${this.editingAddress.id}`, this.addressForm)
      : this.http.post<UserAddress>(`${BASE}/addresses`, this.addressForm);

    req$.subscribe({
      next: addr => {
        if (this.editingAddress) {
          const idx = this.addresses.findIndex(a => a.id === addr.id);
          if (idx >= 0) this.addresses[idx] = addr;
        } else {
          this.addresses.push(addr);
        }
        this.showAddressForm = false;
        this.savingAddress = false;
        this.toast.success(this.editingAddress ? 'Address updated' : 'Address added');
        this.editingAddress = null;
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to save address');
        this.savingAddress = false;
      }
    });
  }

  deleteAddress(addr: UserAddress): void {
    if (!confirm('Delete this address?')) return;
    this.http.delete(`${BASE}/addresses/${addr.id}`).subscribe({
      next: () => {
        this.addresses = this.addresses.filter(a => a.id !== addr.id);
        this.toast.info('Address deleted');
      },
      error: () => this.toast.error('Failed to delete address')
    });
  }

  // ── Reviews ──
  loadReviews(): void {
    this.loadingReviews = true;
    this.reviewService.getMyReviews(0, 20).subscribe({
      next: p => { this.reviews = p.content; this.loadingReviews = false; },
      error: () => this.loadingReviews = false
    });
  }

  deleteReview(review: Review): void {
    if (!confirm('Delete this review?')) return;
    this.reviewService.delete(review.id).subscribe({
      next: () => {
        this.reviews = this.reviews.filter(r => r.id !== review.id);
        this.totalReviews--;
        this.toast.info('Review deleted');
      },
      error: () => this.toast.error('Failed to delete review')
    });
  }

  // ── Security ──
  changePassword(): void {
    this.pwError = '';
    this.pwSuccess = false;
    if (!this.pwForm.currentPassword || !this.pwForm.newPassword) {
      this.pwError = 'Please fill in all fields'; return;
    }
    if (this.pwForm.newPassword.length < 8) {
      this.pwError = 'New password must be at least 8 characters'; return;
    }
    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) {
      this.pwError = 'Passwords do not match'; return;
    }
    this.savingPw = true;
    this.http.patch(`${BASE}/users/me/password`, {
      currentPassword: this.pwForm.currentPassword,
      newPassword: this.pwForm.newPassword
    }).subscribe({
      next: () => {
        this.pwSuccess = true;
        this.pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        this.savingPw = false;
        this.toast.success('Password changed successfully!');
      },
      error: err => {
        this.pwError = err?.error?.message || 'Failed to change password';
        this.savingPw = false;
      }
    });
  }

  // ── Helpers ──
  get userInitial(): string {
    return this.user?.email?.[0]?.toUpperCase() ?? '?';
  }

  get memberSince(): string {
    return 'NovaCart Member';
  }

  emptyAddress() {
    return { name: '', phone: '', line1: '', line2: '', city: '', state: '', pincode: '', country: 'India', addressType: 'HOME', defaultAddress: false };
  }

  getStars(rating: number): boolean[] {
    return [1,2,3,4,5].map(s => s <= Math.round(rating));
  }
}
