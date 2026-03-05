import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService, CategoryService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Product, ProductSummary, Category } from '../../core/models';

interface ProductForm {
  name: string;
  shortDescription: string;
  description: string;
  price: number | null;
  mrp: number | null;
  brand: string;
  sku: string;
  stockQuantity: number | null;
  categoryId: number | null;
  weightGrams: number | null;
  status: string;
  images: { imageUrl: string; altText: string; displayOrder: number; primary: boolean }[];
  attributes: { name: string; value: string; displayOrder: number }[];
}

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.css'
})
export class AdminProductsComponent implements OnInit {

  // List state
  products: ProductSummary[] = [];
  categories: Category[] = [];
  loading = true;
  page = 0;
  totalPages = 0;
  totalElements = 0;
  size = 15;
  searchKeyword = '';
  filterCategory: number | null = null;

  // Modal state
  modalMode: 'create' | 'edit' | null = null;
  editingId: number | null = null;
  saving = false;
  deleteConfirmId: number | null = null;
  deleting = false;

  // Form
  form: ProductForm = this.emptyForm();

  statusOptions = ['ACTIVE', 'INACTIVE', 'OUT_OF_STOCK'];

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.categoryService.getAll().subscribe(c => this.categories = c);
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.search({
      keyword: this.searchKeyword || undefined,
      categoryId: this.filterCategory ?? undefined,
      page: this.page,
      size: this.size
    }).subscribe({
      next: p => {
        this.products = p.content;
        this.totalPages = p.totalPages;
        this.totalElements = p.totalElements;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onSearch(): void { this.page = 0; this.loadProducts(); }
  setPage(p: number): void { this.page = p; this.loadProducts(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
  getPages(): number[] {
    const total = Math.min(this.totalPages, 7);
    const start = Math.max(0, Math.min(this.page - 3, this.totalPages - total));
    return Array.from({ length: total }, (_, i) => start + i);
  }

  // ── Modal helpers ────────────────────────────────────
  openCreate(): void {
    this.form = this.emptyForm();
    this.editingId = null;
    this.modalMode = 'create';
  }

  openEdit(summary: ProductSummary): void {
    this.saving = false;
    this.editingId = summary.id;
    this.modalMode = 'edit';
    // fetch full product for all fields
    this.productService.getById(summary.id).subscribe(p => {
      this.form = {
        name: p.name,
        shortDescription: p.shortDescription || '',
        description: p.description || '',
        price: p.price,
        mrp: p.mrp,
        brand: p.brand,
        sku: p.sku || '',
        stockQuantity: p.stockQuantity,
        categoryId: p.categoryId,
        weightGrams: p.weightGrams || null,
        status: p.status,
        images: p.images.length
          ? p.images.map(i => ({ imageUrl: i.imageUrl, altText: i.altText || '', displayOrder: i.displayOrder, primary: i.primary }))
          : [{ imageUrl: '', altText: '', displayOrder: 1, primary: true }],
        attributes: p.attributes.length
          ? p.attributes.map(a => ({ name: a.name, value: a.value, displayOrder: a.displayOrder }))
          : []
      };
    });
  }

  closeModal(): void { this.modalMode = null; this.editingId = null; }

  // ── Image rows ───────────────────────────────────────
  addImage(): void {
    this.form.images.push({ imageUrl: '', altText: '', displayOrder: this.form.images.length + 1, primary: false });
  }
  removeImage(i: number): void { this.form.images.splice(i, 1); }
  setPrimary(i: number): void { this.form.images.forEach((img, idx) => img.primary = idx === i); }

  // ── Attribute rows ───────────────────────────────────
  addAttribute(): void {
    this.form.attributes.push({ name: '', value: '', displayOrder: this.form.attributes.length + 1 });
  }
  removeAttribute(i: number): void { this.form.attributes.splice(i, 1); }

  // ── Save ─────────────────────────────────────────────
  save(): void {
    if (!this.form.name?.trim()) { this.toast.error('Product name is required'); return; }
    if (!this.form.price)        { this.toast.error('Price is required'); return; }
    if (!this.form.categoryId)   { this.toast.error('Category is required'); return; }

    const payload = {
      name:             this.form.name.trim(),
      shortDescription: this.form.shortDescription?.trim() || null,
      description:      this.form.description?.trim() || null,
      price:            this.form.price,
      mrp:              this.form.mrp || this.form.price,   // default mrp = price if blank
      brand:            this.form.brand?.trim() || null,
      sku:              this.form.sku?.trim() || null,
      stockQuantity:    this.form.stockQuantity ?? 0,       // never send null
      categoryId:       this.form.categoryId,
      weightGrams:      this.form.weightGrams || null,
      status:           this.form.status,
      images:           this.form.images.filter(i => i.imageUrl.trim()),
      attributes:       this.form.attributes.filter(a => a.name.trim() && a.value.trim()),
      // slug intentionally omitted — backend auto-generates from name
    };

    this.saving = true;
    const req$ = this.modalMode === 'edit' && this.editingId
      ? this.productService.update(this.editingId, payload)
      : this.productService.create(payload);

    req$.subscribe({
      next: () => {
        this.toast.success(this.modalMode === 'edit' ? 'Product updated!' : 'Product created!');
        this.closeModal();
        this.loadProducts();
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
    this.productService.delete(this.deleteConfirmId).subscribe({
      next: () => {
        this.toast.success('Product deleted');
        this.deleteConfirmId = null;
        this.deleting = false;
        this.loadProducts();
      },
      error: err => { this.toast.error(err?.error?.message || 'Delete failed'); this.deleting = false; }
    });
  }

  // ── Status toggle ────────────────────────────────────
  toggleStatus(p: ProductSummary): void {
    const newStatus = p.inStock ? 'INACTIVE' : 'ACTIVE';
    this.productService.updateStatus(p.id, newStatus).subscribe({
      next: () => { this.toast.success('Status updated'); this.loadProducts(); },
      error: () => this.toast.error('Failed to update status')
    });
  }

  private emptyForm(): ProductForm {
    return {
      name: '', shortDescription: '', description: '',
      price: null, mrp: null, brand: '', sku: '',
      stockQuantity: null, categoryId: null, weightGrams: null,
      status: 'ACTIVE',
      images: [{ imageUrl: '', altText: '', displayOrder: 1, primary: true }],
      attributes: []
    };
  }
}
