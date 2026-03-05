import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Order } from '../../core/models';

interface OrderStep {
  label: string;
  done: boolean;
  active: boolean;
  time?: string;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  selectedOrder: Order | null = null;
  loading = true;
  page = 0;
  totalPages = 0;
  cancellingId: number | null = null;

  statusColors: Record<string, string> = {
    PENDING: 'warning', CONFIRMED: 'accent', PROCESSING: 'accent',
    SHIPPED: 'teal', OUT_FOR_DELIVERY: 'teal', DELIVERED: 'success',
    CANCELLED: 'danger', RETURN_REQUESTED: 'warning', RETURNED: 'danger',
    PAID: 'success', UNPAID: 'warning', REFUNDED: 'teal'
  };

  constructor(
    private orderService: OrderService,
    private toast: ToastService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.route.params.subscribe(p => {
      if (p['id']) {
        this.orderService.getOrder(+p['id']).subscribe(o => this.selectedOrder = o);
      }
    });
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getMyOrders(this.page, 10).subscribe({
      next: p => {
        this.orders = p.content;
        this.totalPages = p.totalPages;
        this.loading = false;
        // Auto-select first order on desktop
        if (this.orders.length > 0 && !this.selectedOrder && window.innerWidth > 900) {
          this.selectedOrder = this.orders[0];
        }
      },
      error: () => this.loading = false
    });
  }

  selectOrder(order: Order): void {
    this.selectedOrder = order;
    // Scroll to top of detail on mobile
    if (window.innerWidth <= 900) {
      setTimeout(() => document.querySelector('.order-detail')?.scrollIntoView({ behavior: 'smooth' }), 50);
    }
  }

  cancelOrder(order: Order): void {
    if (!confirm('Are you sure you want to cancel this order?')) return;
    this.cancellingId = order.id;
    this.orderService.cancelOrder(order.id, 'Cancelled by customer').subscribe({
      next: updated => {
        const idx = this.orders.findIndex(o => o.id === updated.id);
        if (idx >= 0) this.orders[idx] = updated;
        if (this.selectedOrder?.id === updated.id) this.selectedOrder = updated;
        this.toast.success('Order cancelled successfully');
        this.cancellingId = null;
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Cannot cancel this order');
        this.cancellingId = null;
      }
    });
  }

  canCancel(order: Order): boolean {
    return ['PENDING', 'CONFIRMED', 'PROCESSING'].includes(order.status);
  }

  getBadgeClass(status: string): string {
    return `badge badge-${this.statusColors[status] || 'accent'}`;
  }

  getOrderSteps(order: Order): OrderStep[] {
    const flow = [
      'PENDING',
      'CONFIRMED',
      'PROCESSING',
      'SHIPPED',
      'OUT_FOR_DELIVERY',
      'DELIVERED'
    ];
    const currentIdx = flow.indexOf(order.status);

    return flow.map((s, i) => ({
      label: s.replace(/_/g, ' ').toLowerCase(),
      done: i < currentIdx,
      active: i === currentIdx,
      time: i < currentIdx ? order.updatedAt : (i === currentIdx ? order.updatedAt : undefined)
    }));
  }

  pageArray(): number[] {
    return Array(this.totalPages).fill(0);
  }

  setPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.loadOrders();
  }
}
