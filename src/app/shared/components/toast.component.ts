import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let t of toast.toasts()" class="toast" [class]="t.type">
        <span class="toast-icon">{{ t.type === 'success' ? '✓' : t.type === 'error' ? '✕' : 'ℹ' }}</span>
        {{ t.message }}
      </div>
    </div>
  `,
  styles: [`
    .toast-container { position: fixed; bottom: 28px; right: 28px; z-index: 9999; display: flex; flex-direction: column; gap: 10px; }
    .toast {
      display: flex; align-items: center; gap: 10px;
      padding: 13px 18px; border-radius: 12px; min-width: 260px; max-width: 380px;
      background: var(--bg-3); border: 1px solid var(--card-border);
      box-shadow: 0 8px 24px rgba(0,0,0,0.4);
      font-weight: 500; font-size: 0.9rem;
      animation: slideInRight 0.25s ease;
    }
    .toast.success { border-color: rgba(88,230,169,0.35); color: var(--success); }
    .toast.error   { border-color: rgba(255,123,144,0.35); color: var(--danger); }
    .toast.info    { border-color: rgba(109,141,255,0.35); color: var(--accent); }
    .toast-icon { font-size: 1rem; flex-shrink: 0; }
  `]
})
export class ToastComponent {
  constructor(public toast: ToastService) {}
}
