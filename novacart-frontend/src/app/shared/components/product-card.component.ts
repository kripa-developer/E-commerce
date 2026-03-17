import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductSummary } from '../../core/models';
import { CartService } from '../../core/services/api.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="product-card">
      <a [routerLink]="['/products', product.slug]" class="card-image-wrap">
        <img [src]="product.primaryImageUrl || 'assets/placeholder.svg'"
             [alt]="product.name" class="card-image" loading="lazy" />
        <span class="discount-tag" *ngIf="product.discountPercent > 0">
          -{{ product.discountPercent | number:'1.0-0' }}%
        </span>
        <span class="out-of-stock-tag" *ngIf="!product.inStock">Out of Stock</span>
      </a>
      <div class="card-body">
        <div class="card-brand">{{ product.brand }}</div>
        <a [routerLink]="['/products', product.slug]" class="card-name">{{ product.name }}</a>
        <div class="card-rating" *ngIf="product.reviewCount > 0">
          <div class="stars">
            <span *ngFor="let s of [1,2,3,4,5]" class="star" [class.filled]="s <= product.averageRating">★</span>
          </div>
          <span class="rating-count">({{ product.reviewCount }})</span>
        </div>
        <div class="price-group">
          <span class="price">₹{{ product.price | number:'1.0-0' }}</span>
          <span class="price-mrp" *ngIf="product.mrp > product.price">₹{{ product.mrp | number:'1.0-0' }}</span>
        </div>
        <button class="btn btn-primary btn-sm btn-full add-btn"
                [disabled]="!product.inStock || adding"
                (click)="addToCart($event)">
          {{ adding ? 'Adding...' : product.inStock ? 'Add to Cart' : 'Out of Stock' }}
        </button>
      </div>
    </div>
  `,
  styleUrl: './product-card.component.css'
})
export class ProductCardComponent {
  @Input({ required: true }) product!: ProductSummary;
  adding = false;

  constructor(
    private cartApi: CartService,
    private cartState: CartStateService,
    private toast: ToastService,
    private auth: AuthService
  ) {}

  addToCart(e: Event): void {
    e.preventDefault();
    if (!this.auth.isLoggedIn()) { this.toast.info('Please sign in to add to cart'); return; }
    this.adding = true;
    this.cartApi.addItem(this.product.id, 1).subscribe({
      next: cart => { this.cartState.setCart(cart); this.toast.success('Added to cart!'); this.adding = false; },
      error: err  => { this.toast.error(err?.error?.message || 'Failed to add'); this.adding = false; }
    });
  }
}
