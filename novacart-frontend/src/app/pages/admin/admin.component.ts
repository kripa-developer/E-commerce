import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminService, ProductService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { DashboardStats, Order, ProductSummary } from '../../core/models';

type AdminTab = 'overview' | 'orders' | 'products';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  activeTab: AdminTab = 'overview';
  stats: DashboardStats | null = null;
  orders: Order[] = [];
  products: ProductSummary[] = [];
  loading = true;
  orderStatusFilter = '';
  orderPage = 0; orderTotalPages = 0;
  productPage = 0; productTotalPages = 0;

  orderStatuses = ['', 'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];
  statusBadge: Record<string, string> = {
    PENDING: 'warning', CONFIRMED: 'accent', PROCESSING: 'accent',
    SHIPPED: 'teal', OUT_FOR_DELIVERY: 'teal', DELIVERED: 'success',
    CANCELLED: 'danger'
  };

  constructor(
    private adminService: AdminService,
    private productService: ProductService,
    private toast: ToastService
  ) {}

  ngOnInit(): void { this.loadDashboard(); }

  loadDashboard(): void {
    this.adminService.getDashboard().subscribe({ next: s => { this.stats = s; this.loading = false; }, error: () => this.loading = false });
  }

  setTab(tab: AdminTab): void {
    this.activeTab = tab;
    if (tab === 'orders' && this.orders.length === 0) this.loadOrders();
    if (tab === 'products' && this.products.length === 0) this.loadProducts();
  }

  loadOrders(): void {
    this.adminService.getAllOrders(this.orderStatusFilter || undefined, this.orderPage, 20).subscribe(p => {
      this.orders = p.content; this.orderTotalPages = p.totalPages;
    });
  }

  loadProducts(): void {
    this.productService.search({ page: this.productPage, size: 20 }).subscribe(p => {
      this.products = p.content; this.productTotalPages = p.totalPages;
    });
  }

  updateOrderStatus(orderId: number, status: string): void {
    this.adminService.updateOrderStatus(orderId, status).subscribe({
      next: updated => {
        const idx = this.orders.findIndex(o => o.id === orderId);
        if (idx >= 0) this.orders[idx] = updated;
        this.toast.success('Order status updated');
      },
      error: () => this.toast.error('Failed to update status')
    });
  }

  updateProductStatus(productId: number, status: string): void {
    this.productService.updateStatus(productId, status).subscribe({
      next: () => { this.toast.success('Product status updated'); this.loadProducts(); },
      error: () => this.toast.error('Failed')
    });
  }

  getBadge(status: string): string { return `badge badge-${this.statusBadge[status] || 'accent'}`; }

  formatCurrency(n: number): string { return '₹' + n.toLocaleString('en-IN'); }
}
