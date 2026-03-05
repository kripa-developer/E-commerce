import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService, CartService, ReviewService } from '../../core/services/api.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';
import { Product, Review, ReviewSummary, Page } from '../../core/models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  selectedImage = 0;
  quantity = 1;
  loading = true;
  addingToCart = false;
  addingToWishlist = false;
  inWishlist = false;
  activeTab: 'description' | 'specs' | 'reviews' = 'description';

  reviews: Review[] = [];
  reviewSummary: ReviewSummary | null = null;
  reviewPage = 0;
  reviewTotalPages = 0;
  showReviewForm = false;
  newReview = { rating: 5, title: '', body: '' };
  submittingReview = false;
  ratingLabels = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartApi: CartService,
    private cartState: CartStateService,
    private reviewService: ReviewService,
    private toast: ToastService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(p => {
      this.productService.getBySlug(p['slug']).subscribe({
        next: prod => {
          this.product = prod;
          this.loading = false;
          this.loadReviews();
          if (this.auth.isLoggedIn()) {
            this.cartApi.checkWishlist(prod.id).subscribe(r => this.inWishlist = r.inWishlist);
          }
        },
        error: () => this.router.navigate(['/products'])
      });
    });
  }

  loadReviews(): void {
    if (!this.product) return;
    this.reviewService.getSummary(this.product.id).subscribe(s => this.reviewSummary = s);
    this.reviewService.getProductReviews(this.product.id, this.reviewPage, 5).subscribe(r => {
      this.reviews = r.content;
      this.reviewTotalPages = r.totalPages;
    });
  }

  addToCart(): void {
    if (!this.auth.isLoggedIn()) { this.toast.info('Please sign in'); return; }
    this.addingToCart = true;
    this.cartApi.addItem(this.product!.id, this.quantity).subscribe({
      next: cart => { this.cartState.setCart(cart); this.toast.success('Added to cart!'); this.addingToCart = false; },
      error: err  => { this.toast.error(err?.error?.message || 'Failed'); this.addingToCart = false; }
    });
  }

  buyNow(): void {
    this.addToCart();
    this.router.navigate(['/cart']);
  }

  toggleWishlist(): void {
    if (!this.auth.isLoggedIn()) { this.toast.info('Please sign in'); return; }
    if (this.inWishlist) {
      this.cartApi.removeFromWishlist(this.product!.id).subscribe(() => { this.inWishlist = false; this.toast.info('Removed from wishlist'); });
    } else {
      this.cartApi.addToWishlist(this.product!.id).subscribe(() => { this.inWishlist = true; this.toast.success('Added to wishlist!'); });
    }
  }

  submitReview(): void {
    if (!this.product) return;
    this.submittingReview = true;
    this.reviewService.create(this.product.id, this.newReview).subscribe({
      next: () => {
        this.toast.success('Review submitted!');
        this.showReviewForm = false;
        this.newReview = { rating: 5, title: '', body: '' };
        this.loadReviews();
        this.submittingReview = false;
      },
      error: err => { this.toast.error(err?.error?.message || 'Failed'); this.submittingReview = false; }
    });
  }

  setRating(r: number): void { this.newReview.rating = r; }
  getRatingPercent(r: number): number {
    if (!this.reviewSummary?.totalReviews) return 0;
    return ((this.reviewSummary.ratingDistribution[r] ?? 0) / this.reviewSummary.totalReviews) * 100;
  }

  getStars(rating: number): boolean[] {
    return [1,2,3,4,5].map(s => s <= Math.round(rating));
  }
}
