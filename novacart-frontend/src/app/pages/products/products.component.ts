import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService, CategoryService } from '../../core/services/api.service';
import { ProductSummary, Category } from '../../core/models';
import { ProductCardComponent } from '../../shared/components/product-card.component';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ProductCardComponent],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent implements OnInit {
  products: ProductSummary[] = [];
  categories: Category[] = [];
  brands: string[] = [];
  loading = true;

  // Filters
  keyword = '';
  selectedCategoryId: number | undefined;
  selectedBrand = '';
  minPrice: number | undefined;
  maxPrice: number | undefined;
  sortBy = 'relevance';
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;

  sortOptions = [
    { value: 'relevance', label: 'Relevance' },
    { value: 'popular',   label: 'Most Popular' },
    { value: 'newest',    label: 'Newest First' },
    { value: 'price_asc', label: 'Price: Low to High' },
    { value: 'price_desc',label: 'Price: High to Low' },
    { value: 'rating',    label: 'Top Rated' },
  ];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(c => this.categories = c);
    this.route.queryParams.subscribe(params => {
      this.keyword            = params['keyword'] || '';
      this.selectedCategoryId = params['categoryId'] ? +params['categoryId'] : undefined;
      this.selectedBrand      = params['brand'] || '';
      this.sortBy             = params['sortBy'] || 'relevance';
      this.page               = params['page'] ? +params['page'] : 0;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.search({
      keyword: this.keyword || undefined,
      categoryId: this.selectedCategoryId,
      brand: this.selectedBrand || undefined,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice,
      sortBy: this.sortBy,
      page: this.page,
      size: this.size
    }).subscribe({
      next: p => {
        this.products = p.content;
        this.totalPages = p.totalPages;
        this.totalElements = p.totalElements;
        this.loading = false;
        if (this.selectedCategoryId) {
          this.productService.getBrands(this.selectedCategoryId).subscribe(b => this.brands = b);
        }
      },
      error: () => this.loading = false
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.router.navigate([], { queryParams: this.buildQueryParams(), queryParamsHandling: 'merge' });
  }

  clearFilters(): void {
    this.keyword = ''; this.selectedCategoryId = undefined;
    this.selectedBrand = ''; this.minPrice = undefined; this.maxPrice = undefined;
    this.sortBy = 'relevance'; this.page = 0;
    this.router.navigate(['/products']);
  }

  setPage(p: number): void {
    this.page = p;
    this.router.navigate([], { queryParams: { page: p }, queryParamsHandling: 'merge' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  getPages(): number[] {
    const total = Math.min(this.totalPages, 7);
    const start = Math.max(0, Math.min(this.page - 3, this.totalPages - total));
    return Array.from({ length: total }, (_, i) => start + i);
  }

  private buildQueryParams(): any {
    const params: any = {};
    if (this.keyword)            params['keyword']    = this.keyword;
    if (this.selectedCategoryId) params['categoryId'] = this.selectedCategoryId;
    if (this.selectedBrand)      params['brand']      = this.selectedBrand;
    if (this.minPrice != null)   params['minPrice']   = this.minPrice;
    if (this.maxPrice != null)   params['maxPrice']   = this.maxPrice;
    if (this.sortBy !== 'relevance') params['sortBy'] = this.sortBy;
    return params;
  }
}
