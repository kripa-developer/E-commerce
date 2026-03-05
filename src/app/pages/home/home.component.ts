import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ProductService, CategoryService } from '../../core/services/api.service';
import { ProductSummary, Category } from '../../core/models';
import { ProductCardComponent } from '../../shared/components/product-card.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductCardComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  categories: Category[] = [];
  bestSellers: ProductSummary[] = [];
  newArrivals: ProductSummary[] = [];
  topDeals: ProductSummary[] = [];
  loading = true;

  features = [
    { icon: '🚚', title: 'Free Delivery',   desc: 'On orders above ₹499' },
    { icon: '🔒', title: 'Secure Payments', desc: 'SSL encrypted checkout' },
    { icon: '↩️', title: 'Easy Returns',    desc: '10-day hassle-free returns' },
    { icon: '💬', title: '24/7 Support',    desc: "We're here to help anytime" },
  ];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.getTree().subscribe(c => this.categories = c.slice(0, 8));
    this.productService.getBestSellers(0, 8).subscribe(p => { this.bestSellers = p.content; this.loading = false; });
    this.productService.getNewArrivals(0, 8).subscribe(p => this.newArrivals = p.content);
    this.productService.getTopDeals(0, 8).subscribe(p => this.topDeals = p.content);
  }

  browseCategory(cat: Category): void {
    this.router.navigate(['/products'], { queryParams: { categoryId: cat.id } });
  }

  browseDeals(): void {
    this.router.navigate(['/products'], { queryParams: { sortBy: 'price_asc' } });
  }
}
