import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService, OrderService } from '../../core/services/api.service';
import { CartStateService } from '../../core/services/cart-state.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/auth/auth.service';
import { Cart, UserAddress } from '../../core/models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;
  loading = true;
  updatingItem: number | null = null;

  // Checkout
  checkoutMode = false;
  addresses: UserAddress[] = [];
  selectedAddressId: number | null = null;
  paymentMethod = 'COD';
  notes = '';
  placingOrder = false;
  showAddressForm = false;

  newAddress = { name: '', phone: '', line1: '', line2: '', city: '', state: '', pincode: '', country: 'India', addressType: 'HOME', defaultAddress: false };

  paymentMethods = [
    { value: 'COD',     label: 'Cash on Delivery' },
    { value: 'UPI',     label: 'UPI' },
    { value: 'CARD',    label: 'Credit / Debit Card' },
    { value: 'NETBANK', label: 'Net Banking' },
  ];

  constructor(
    private cartApi: CartService,
    private cartState: CartStateService,
    private orderService: OrderService,
    private toast: ToastService,
    public auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartApi.getCart().subscribe({ next: c => { this.cart = c; this.cartState.setCart(c); this.loading = false; }, error: () => this.loading = false });
  }

  updateQty(itemId: number, qty: number): void {
    this.updatingItem = itemId;
    this.cartApi.updateItem(itemId, qty).subscribe({
      next: c => { this.cart = c; this.cartState.setCart(c); this.updatingItem = null; },
      error: err => { this.toast.error(err?.error?.message || 'Failed'); this.updatingItem = null; }
    });
  }

  removeItem(itemId: number): void {
    this.updatingItem = itemId;
    this.cartApi.removeItem(itemId).subscribe({
      next: c => { this.cart = c; this.cartState.setCart(c); this.updatingItem = null; this.toast.info('Item removed'); },
      error: () => this.updatingItem = null
    });
  }

  get shippingCharge(): number { return (this.cart?.subtotal ?? 0) >= 499 ? 0 : 49; }
  get total(): number { return (this.cart?.subtotal ?? 0) + this.shippingCharge; }

  startCheckout(): void {
    this.checkoutMode = true;
    this.orderService.getAddresses().subscribe(a => {
      this.addresses = a;
      this.selectedAddressId = a.find(x => x.defaultAddress)?.id ?? a[0]?.id ?? null;
    });
  }

  saveAndSelectAddress(): void {
    this.orderService.addAddress(this.newAddress).subscribe({
      next: addr => {
        this.addresses.push(addr);
        this.selectedAddressId = addr.id;
        this.showAddressForm = false;
        this.toast.success('Address saved');
      },
      error: err => this.toast.error(err?.error?.message || 'Failed to save address')
    });
  }

  placeOrder(): void {
    if (!this.selectedAddressId) { this.toast.error('Please select a delivery address'); return; }
    this.placingOrder = true;
    this.orderService.placeOrder({ addressId: this.selectedAddressId, paymentMethod: this.paymentMethod, notes: this.notes }).subscribe({
      next: order => {
        this.cartState.reload();
        this.toast.success('Order placed successfully!');
        this.router.navigate(['/orders', order.id]);
      },
      error: err => { this.toast.error(err?.error?.message || 'Order failed'); this.placingOrder = false; }
    });
  }
}
