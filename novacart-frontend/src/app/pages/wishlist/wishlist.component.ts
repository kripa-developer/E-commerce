import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../core/services/api.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { ToastService } from '../../core/services/toast.service';
import { WishlistItem } from '../../core/models';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {
  items: WishlistItem[] = [];
  loading = true;
  removingId: number | null = null;
  addingToCartId: number | null = null;

  constructor(
    private cartApi: CartService,
    private cartState: CartStateService,
    private toast: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist(): void {
    this.loading = true;
    this.cartApi.getWishlist().subscribe({
      next: w => { this.items = w.items; this.loading = false; },
      error: () => this.loading = false
    });
  }

  removeFromWishlist(productId: number): void {
    this.removingId = productId;
    this.cartApi.removeFromWishlist(productId).subscribe({
      next: () => {
        this.items = this.items.filter(i => i.product.id !== productId);
        this.toast.info('Removed from wishlist');
        this.removingId = null;
      },
      error: () => this.removingId = null
    });
  }

  addToCart(item: WishlistItem): void {
    this.addingToCartId = item.product.id;
    this.cartApi.addItem(item.product.id, 1).subscribe({
      next: cart => {
        this.cartState.setCart(cart);
        this.toast.success(`${item.product.name} added to cart!`);
        this.addingToCartId = null;
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to add to cart');
        this.addingToCartId = null;
      }
    });
  }

  moveAllToCart(): void {
    const inStock = this.items.filter(i => i.product.inStock);
    if (inStock.length === 0) { this.toast.info('No in-stock items to add'); return; }
    let done = 0;
    inStock.forEach(item => {
      this.cartApi.addItem(item.product.id, 1).subscribe({
        next: cart => {
          this.cartState.setCart(cart);
          done++;
          if (done === inStock.length) {
            this.toast.success(`${done} item${done > 1 ? 's' : ''} added to cart!`);
          }
        }
      });
    });
  }
}
