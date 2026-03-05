import { Injectable, signal } from '@angular/core';

export interface Toast {
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  id: number;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);
  private nextId = 0;

  show(message: string, type: Toast['type'] = 'info', duration = 3500): void {
    const id = ++this.nextId;
    this.toasts.update(t => [...t, { message, type, id, duration }]);
    setTimeout(() => this.dismiss(id), duration);
  }

  dismiss(id: number): void {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }

  success(msg: string, duration?: number) { this.show(msg, 'success', duration); }
  error(msg: string, duration?: number)   { this.show(msg, 'error', duration ?? 5000); }
  info(msg: string, duration?: number)    { this.show(msg, 'info', duration); }
  warning(msg: string, duration?: number) { this.show(msg, 'warning', duration); }
}
