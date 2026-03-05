import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Category } from '../../core/models';

interface CategoryForm {
  name: string;
  description: string;
  imageUrl: string;
  parentId: number | null;
  displayOrder: number;
  active: boolean;
}

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-categories.component.html',
  styleUrl: './admin-categories.component.css'
})
export class AdminCategoriesComponent implements OnInit {

  categories: Category[] = [];
  flatCategories: Category[] = [];   // for parent dropdown (no children)
  loading = true;

  modalMode: 'create' | 'edit' | null = null;
  editingId: number | null = null;
  saving = false;
  deleteConfirmId: number | null = null;
  deleting = false;

  form: CategoryForm = this.emptyForm();

  constructor(
    private categoryService: CategoryService,
    private toast: ToastService
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    // load tree for display
    this.categoryService.getTree().subscribe({
      next: tree => {
        this.categories = tree;
        this.loading = false;
      },
      error: () => this.loading = false
    });
    // load flat list for parent dropdown
    this.categoryService.getAll().subscribe(all => this.flatCategories = all);
  }

  // ── Modal ────────────────────────────────────────────
  openCreate(parentId: number | null = null): void {
    this.form = this.emptyForm();
    this.form.parentId = parentId;
    this.editingId = null;
    this.modalMode = 'create';
  }

  openEdit(cat: Category): void {
    this.form = {
      name:         cat.name,
      description:  cat.description || '',
      imageUrl:     cat.imageUrl || '',
      parentId:     cat.parentId,
      displayOrder: cat.displayOrder,
      active:       cat.active
    };
    this.editingId = cat.id;
    this.modalMode = 'edit';
  }

  closeModal(): void { this.modalMode = null; this.editingId = null; }

  save(): void {
    if (!this.form.name.trim()) { this.toast.error('Category name is required'); return; }
    this.saving = true;
    const req$ = this.modalMode === 'edit' && this.editingId
      ? this.categoryService.update(this.editingId, this.form)
      : this.categoryService.create(this.form);

    req$.subscribe({
      next: () => {
        this.toast.success(this.modalMode === 'edit' ? 'Category updated!' : 'Category created!');
        this.closeModal();
        this.load();
        this.saving = false;
      },
      error: err => { this.toast.error(err?.error?.message || 'Save failed'); this.saving = false; }
    });
  }

  // ── Delete ───────────────────────────────────────────
  confirmDelete(id: number): void { this.deleteConfirmId = id; }
  cancelDelete(): void { this.deleteConfirmId = null; }

  doDelete(): void {
    if (!this.deleteConfirmId) return;
    this.deleting = true;
    this.categoryService.delete(this.deleteConfirmId).subscribe({
      next: () => {
        this.toast.success('Category deleted');
        this.deleteConfirmId = null;
        this.deleting = false;
        this.load();
      },
      error: err => { this.toast.error(err?.error?.message || 'Delete failed — may have products assigned'); this.deleting = false; }
    });
  }

  // ── Helpers ──────────────────────────────────────────
  totalCount(): number {
    return this.categories.reduce((sum, c) => sum + 1 + (c.children?.length ?? 0), 0);
  }

  parentName(parentId: number | null): string {
    if (!parentId) return '—';
    return this.flatCategories.find(c => c.id === parentId)?.name ?? '—';
  }

  private emptyForm(): CategoryForm {
    return { name: '', description: '', imageUrl: '', parentId: null, displayOrder: 0, active: true };
  }
}
