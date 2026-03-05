import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Category, Page, Product, ProductSummary, Cart, Wishlist,
  UserAddress, Order, Review, ReviewSummary, DashboardStats
} from '../models';

const BASE = 'http://localhost:8811/api/v1';

// ── Category ──────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class CategoryService {
  constructor(private http: HttpClient) {}
  getAll(): Observable<Category[]>          { return this.http.get<Category[]>(`${BASE}/categories`); }
  getTree(): Observable<Category[]>         { return this.http.get<Category[]>(`${BASE}/categories/tree`); }
  getBySlug(slug: string): Observable<Category> { return this.http.get<Category>(`${BASE}/categories/slug/${slug}`); }
  create(data: Partial<Category>): Observable<Category> { return this.http.post<Category>(`${BASE}/categories`, data); }
  update(id: number, data: Partial<Category>): Observable<Category> { return this.http.put<Category>(`${BASE}/categories/${id}`, data); }
  delete(id: number): Observable<void>      { return this.http.delete<void>(`${BASE}/categories/${id}`); }
}

// ── Product ───────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private http: HttpClient) {}

  search(filters: {
    categoryId?: number; brand?: string; minPrice?: number; maxPrice?: number;
    keyword?: string; page?: number; size?: number; sortBy?: string;
  }): Observable<Page<ProductSummary>> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') params = params.set(k, v); });
    return this.http.get<Page<ProductSummary>>(`${BASE}/products`, { params });
  }

  getBestSellers(page = 0, size = 10): Observable<Page<ProductSummary>> {
    return this.http.get<Page<ProductSummary>>(`${BASE}/products/best-sellers`, { params: { page, size } });
  }
  getNewArrivals(page = 0, size = 10): Observable<Page<ProductSummary>> {
    return this.http.get<Page<ProductSummary>>(`${BASE}/products/new-arrivals`, { params: { page, size } });
  }
  getTopDeals(page = 0, size = 10): Observable<Page<ProductSummary>> {
    return this.http.get<Page<ProductSummary>>(`${BASE}/products/top-deals`, { params: { page, size } });
  }
  getById(id: number): Observable<Product>      { return this.http.get<Product>(`${BASE}/products/${id}`); }
  getBySlug(slug: string): Observable<Product>  { return this.http.get<Product>(`${BASE}/products/slug/${slug}`); }
  getBrands(categoryId: number): Observable<string[]> { return this.http.get<string[]>(`${BASE}/products/brands`, { params: { categoryId } }); }
  create(data: any): Observable<Product>         { return this.http.post<Product>(`${BASE}/products`, data); }
  update(id: number, data: any): Observable<Product> { return this.http.put<Product>(`${BASE}/products/${id}`, data); }
  updateStatus(id: number, status: string): Observable<void> { return this.http.patch<void>(`${BASE}/products/${id}/status`, null, { params: { status } }); }
  delete(id: number): Observable<void>           { return this.http.delete<void>(`${BASE}/products/${id}`); }
}

// ── Cart ──────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class CartService {
  constructor(private http: HttpClient) {}
  getCart(): Observable<Cart>                    { return this.http.get<Cart>(`${BASE}/cart`); }
  addItem(productId: number, quantity: number): Observable<Cart> {
    return this.http.post<Cart>(`${BASE}/cart/items`, { productId, quantity });
  }
  updateItem(itemId: number, quantity: number): Observable<Cart> {
    return this.http.patch<Cart>(`${BASE}/cart/items/${itemId}`, null, { params: { quantity } });
  }
  removeItem(itemId: number): Observable<Cart>   { return this.http.delete<Cart>(`${BASE}/cart/items/${itemId}`); }
  clearCart(): Observable<void>                  { return this.http.delete<void>(`${BASE}/cart`); }

  getWishlist(): Observable<Wishlist>            { return this.http.get<Wishlist>(`${BASE}/wishlist`); }
  addToWishlist(productId: number): Observable<void> { return this.http.post<void>(`${BASE}/wishlist/${productId}`, null); }
  removeFromWishlist(productId: number): Observable<void> { return this.http.delete<void>(`${BASE}/wishlist/${productId}`); }
  checkWishlist(productId: number): Observable<{ inWishlist: boolean }> {
    return this.http.get<{ inWishlist: boolean }>(`${BASE}/wishlist/${productId}/check`);
  }
}

// ── Order ─────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}
  placeOrder(data: any): Observable<Order>       { return this.http.post<Order>(`${BASE}/orders`, data); }
  getMyOrders(page = 0, size = 10): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${BASE}/orders`, { params: { page, size } });
  }
  getOrder(id: number): Observable<Order>        { return this.http.get<Order>(`${BASE}/orders/${id}`); }
  cancelOrder(id: number, reason?: string): Observable<Order> {
    return this.http.post<Order>(`${BASE}/orders/${id}/cancel`, { reason });
  }

  getAddresses(): Observable<UserAddress[]>      { return this.http.get<UserAddress[]>(`${BASE}/addresses`); }
  addAddress(data: any): Observable<UserAddress> { return this.http.post<UserAddress>(`${BASE}/addresses`, data); }
  updateAddress(id: number, data: any): Observable<UserAddress> { return this.http.put<UserAddress>(`${BASE}/addresses/${id}`, data); }
  deleteAddress(id: number): Observable<void>    { return this.http.delete<void>(`${BASE}/addresses/${id}`); }
}

// ── Review ────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class ReviewService {
  constructor(private http: HttpClient) {}
  getProductReviews(productId: number, page = 0, size = 10, sortBy = 'recent'): Observable<Page<Review>> {
    return this.http.get<Page<Review>>(`${BASE}/products/${productId}/reviews`, { params: { page, size, sortBy } });
  }
  getSummary(productId: number): Observable<ReviewSummary> {
    return this.http.get<ReviewSummary>(`${BASE}/products/${productId}/reviews/summary`);
  }
  create(productId: number, data: { rating: number; title: string; body: string }): Observable<Review> {
    return this.http.post<Review>(`${BASE}/products/${productId}/reviews`, data);
  }
  update(reviewId: number, data: any): Observable<Review> { return this.http.put<Review>(`${BASE}/reviews/${reviewId}`, data); }
  delete(reviewId: number): Observable<void>     { return this.http.delete<void>(`${BASE}/reviews/${reviewId}`); }
  markHelpful(reviewId: number): Observable<void>{ return this.http.post<void>(`${BASE}/reviews/${reviewId}/helpful`, null); }
  getMyReviews(page = 0, size = 10): Observable<Page<Review>> {
    return this.http.get<Page<Review>>(`${BASE}/reviews/me`, { params: { page, size } });
  }
}

// ── Admin ─────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private http: HttpClient) {}
  getDashboard(): Observable<DashboardStats>     { return this.http.get<DashboardStats>(`${BASE}/admin/dashboard`); }
  getAllOrders(status?: string, page = 0, size = 20): Observable<Page<Order>> {
    let params: any = { page, size };
    if (status) params['status'] = status;
    return this.http.get<Page<Order>>(`${BASE}/admin/orders`, { params });
  }
  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.patch<Order>(`${BASE}/admin/orders/${id}/status`, null, { params: { status } });
  }
  getAllProducts(page = 0, size = 20): Observable<Page<ProductSummary>> {
    return this.http.get<Page<ProductSummary>>(`${BASE}/products`, { params: { page, size } });
  }
}
