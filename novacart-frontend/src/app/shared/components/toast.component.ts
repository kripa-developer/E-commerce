import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div
        *ngFor="let toast of toastService.toasts(); trackBy: trackById"
        class="toast toast-{{ toast.type }}"
        (click)="toastService.dismiss(toast.id)"
      >
        <div class="toast-icon">
          <svg *ngIf="toast.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <circle cx="12" cy="12" r="10" opacity=".2" fill="currentColor" stroke="none"/>
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <svg *ngIf="toast.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <circle cx="12" cy="12" r="10" opacity=".2" fill="currentColor" stroke="none"/>
            <line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          <svg *ngIf="toast.type === 'info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <circle cx="12" cy="12" r="10" opacity=".2" fill="currentColor" stroke="none"/>
            <line x1="12" y1="8" x2="12" y2="8" stroke-width="3"/><line x1="12" y1="12" x2="12" y2="16"/>
          </svg>
          <svg *ngIf="toast.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" opacity=".2" fill="currentColor" stroke="none"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <span class="toast-msg">{{ toast.message }}</span>
        <button class="toast-close" (click).stop="toastService.dismiss(toast.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        <div class="toast-progress" [style.--duration]="toast.duration + 'ms'"></div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 24px; right: 24px;
      display: flex; flex-direction: column; gap: 10px;
      z-index: 9999;
      pointer-events: none;
    }

    .toast {
      display: flex; align-items: center; gap: 12px;
      padding: 13px 16px 13px 14px;
      border-radius: 12px;
      min-width: 280px; max-width: 380px;
      border: 1px solid;
      backdrop-filter: blur(16px);
      box-shadow: 0 8px 32px rgba(0,0,0,.4);
      cursor: pointer;
      pointer-events: all;
      position: relative; overflow: hidden;
      animation: toastIn .3s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    @keyframes toastIn {
      from { opacity: 0; transform: translateX(60px) scale(.9); }
      to   { opacity: 1; transform: none; }
    }

    .toast-success {
      background: rgba(88, 230, 169, 0.12);
      border-color: rgba(88, 230, 169, 0.3);
      color: #58e6a9;
    }
    .toast-error {
      background: rgba(255, 123, 144, 0.12);
      border-color: rgba(255, 123, 144, 0.3);
      color: #ff7b90;
    }
    .toast-info {
      background: rgba(109, 141, 255, 0.12);
      border-color: rgba(109, 141, 255, 0.3);
      color: #6d8dff;
    }
    .toast-warning {
      background: rgba(255, 200, 87, 0.12);
      border-color: rgba(255, 200, 87, 0.3);
      color: #ffc857;
    }

    .toast-icon {
      width: 20px; height: 20px; flex-shrink: 0;
    }
    .toast-icon svg { width: 100%; height: 100%; }

    .toast-msg {
      flex: 1; font-size: 0.88rem; font-weight: 500;
      line-height: 1.4; color: #eef3ff;
    }

    .toast-close {
      width: 20px; height: 20px; flex-shrink: 0;
      background: none; border: none; padding: 0;
      opacity: .5; cursor: pointer; transition: opacity .15s;
      color: inherit;
    }
    .toast-close:hover { opacity: 1; }
    .toast-close svg { width: 100%; height: 100%; }

    .toast-progress {
      position: absolute; bottom: 0; left: 0;
      height: 2px;
      background: currentColor;
      opacity: .4;
      animation: progress var(--duration, 3500ms) linear forwards;
    }
    @keyframes progress { from { width: 100%; } to { width: 0%; } }

    @media (max-width: 480px) {
      .toast-container { bottom: 16px; right: 16px; left: 16px; }
      .toast { min-width: unset; max-width: unset; width: 100%; }
    }
  `]
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
  trackById(_: number, t: { id: number }) { return t.id; }
}
