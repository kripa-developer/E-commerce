import { Injectable, signal, computed } from '@angular/core';
import { Cart } from '../models';
import { CartService } from './api.service';
import { AuthService } from '../auth/auth.service';
import { effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CartStateService {
  private _cart = signal<Cart | null>(null);

  readonly cart       = this._cart.asReadonly();
  readonly itemCount  = computed(() => this._cart()?.totalItems ?? 0);
  readonly subtotal   = computed(() => this._cart()?.subtotal ?? 0);

  constructor(private cartApi: CartService, private auth: AuthService) {
    effect(() => {
      if (this.auth.isLoggedIn()) this.reload();
      else this._cart.set(null);
    });
  }

  reload(): void {
    this.cartApi.getCart().subscribe({ next: c => this._cart.set(c), error: () => {} });
  }

  setCart(cart: Cart): void { this._cart.set(cart); }
  clear(): void { this._cart.set(null); }
}
