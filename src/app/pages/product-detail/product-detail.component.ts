import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService, CartService, ReviewService } from '../../core/services/api.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';
import { Product, Review, ReviewSummary } from '../../core/models';

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
  inWishlist = false;
  activeTab: 'description' | 'specs' | 'reviews' = 'description';
  stickyInfo = true;

  // Image zoom
  zoomActive = false;
  zoomX = 0;
  zoomY = 0;
  @ViewChild('mainImg') mainImgRef!: ElementRef<HTMLImageElement>;

  // Reviews
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
      this.loading = true;
      this.product = null;
      this.reviews = [];
      this.selectedImage = 0;
      this.reviewPage = 0;

      this.productService.getBySlug(p['slug']).subscribe({
        next: prod => {
          this.product = prod;
          this.loading = false;
          this.loadReviews();
          if (this.auth.isLoggedIn()) {
            this.cartApi.checkWishlist(prod.id).subscribe(r => this.inWishlist = r.inWishlist);
          }
        },
        error: () => { this.loading = false; }
      });
    });
  }

  // ── Image ──
  selectImage(i: number): void { this.selectedImage = i; }

  prevImage(): void {
    if (!this.product) return;
    this.selectedImage = this.selectedImage > 0 ? this.selectedImage - 1 : this.product.images.length - 1;
  }

  nextImage(): void {
    if (!this.product) return;
    this.selectedImage = this.selectedImage < this.product.images.length - 1 ? this.selectedImage + 1 : 0;
  }

  onMouseMove(event: MouseEvent): void {
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width - 0.5) * -60;
    const y = ((event.clientY - rect.top)  / rect.height - 0.5) * -60;
    this.zoomX = x;
    this.zoomY = y;
  }

  resetZoom(): void { this.zoomX = 0; this.zoomY = 0; }

  // ── Quantity ──
  incQty(): void { if (this.product && this.quantity < this.product.stockQuantity) this.quantity++; }
  decQty(): void { if (this.quantity > 1) this.quantity--; }

  // ── Cart ──
  addToCart(): void {
    if (!this.auth.isLoggedIn()) { this.toast.info('Please sign in to add to cart'); this.router.navigate(['/login']); return; }
    this.addingToCart = true;
    this.cartApi.addItem(this.product!.id, this.quantity).subscribe({
      next: cart => {
        this.cartState.setCart(cart);
        this.toast.success(`${this.product!.name} added to cart!`);
        this.addingToCart = false;
      },
      error: err => { this.toast.error(err?.error?.message || 'Failed to add to cart'); this.addingToCart = false; }
    });
  }

  buyNow(): void {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/login']); return; }
    this.addingToCart = true;
    this.cartApi.addItem(this.product!.id, this.quantity).subscribe({
      next: cart => {
        this.cartState.setCart(cart);
        this.addingToCart = false;
        this.router.navigate(['/cart']);
      },
      error: err => { this.toast.error(err?.error?.message || 'Failed'); this.addingToCart = false; }
    });
  }

  // ── Wishlist ──
  toggleWishlist(): void {
    if (!this.auth.isLoggedIn()) { this.toast.info('Please sign in'); return; }
    if (this.inWishlist) {
      this.cartApi.removeFromWishlist(this.product!.id).subscribe(() => {
        this.inWishlist = false;
        this.toast.info('Removed from wishlist');
      });
    } else {
      this.cartApi.addToWishlist(this.product!.id).subscribe(() => {
        this.inWishlist = true;
        this.toast.success('Added to wishlist!');
      });
    }
  }

  // ── Reviews ──
  loadReviews(): void {
    if (!this.product) return;
    this.reviewService.getSummary(this.product.id).subscribe(s => this.reviewSummary = s);
    this.reviewService.getProductReviews(this.product.id, 0, 5).subscribe(r => {
      this.reviews = r.content;
      this.reviewTotalPages = r.totalPages;
      this.reviewPage = 0;
    });
  }

  loadMoreReviews(): void {
    if (!this.product) return;
    this.reviewService.getProductReviews(this.product.id, this.reviewPage + 1, 5).subscribe(r => {
      this.reviews = [...this.reviews, ...r.content];
      this.reviewPage++;
    });
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
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to submit review');
        this.submittingReview = false;
      }
    });
  }

  markHelpful(reviewId: number): void {
    this.reviewService.markHelpful(reviewId).subscribe(() => {
      const r = this.reviews.find(x => x.id === reviewId);
      if (r) r.helpfulCount++;
    });
  }

  setRating(r: number): void { this.newReview.rating = r; }

  getRatingPercent(r: number): number {
    if (!this.reviewSummary?.totalReviews) return 0;
    return ((this.reviewSummary.ratingDistribution[r] ?? 0) / this.reviewSummary.totalReviews) * 100;
  }
}
