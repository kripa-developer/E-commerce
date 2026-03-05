// src/app/core/models/index.ts

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string;
  imageUrl: string;
  parentId: number | null;
  parentName: string | null;
  children: Category[];
  active: boolean;
  displayOrder: number;
}

export interface ProductSummary {
  id: number;
  name: string;
  slug: string;
  shortDescription: string;
  price: number;
  mrp: number;
  discountPercent: number;
  brand: string;
  inStock: boolean;
  primaryImageUrl: string;
  categoryName: string;
  averageRating: number;
  reviewCount: number;
  soldCount: number;
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  altText: string;
  displayOrder: number;
  primary: boolean;
}

export interface ProductAttribute {
  id: number;
  name: string;
  value: string;
  displayOrder: number;
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  description: string;
  shortDescription: string;
  price: number;
  mrp: number;
  discountPercent: number;
  brand: string;
  stockQuantity: number;
  inStock: boolean;
  categoryId: number;
  categoryName: string;
  categorySlug: string;
  images: ProductImage[];
  attributes: ProductAttribute[];
  status: string;
  averageRating: number;
  reviewCount: number;
  soldCount: number;
  sku: string;
  weightGrams: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  productImageUrl: string;
  brand: string;
  unitPrice: number;
  mrp: number;
  quantity: number;
  lineTotal: number;
  inStock: boolean;
  availableStock: number;
}

export interface Cart {
  cartId: number;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  updatedAt: string;
}

export interface WishlistItem {
  id: number;
  product: ProductSummary;
  addedAt: string;
}

export interface Wishlist {
  items: WishlistItem[];
  totalItems: number;
}

export interface ShippingAddress {
  name: string;
  phone: string;
  line1: string;
  line2: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
}

export interface UserAddress extends ShippingAddress {
  id: number;
  addressType: string;
  defaultAddress: boolean;
  createdAt: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  productImageUrl: string;
  unitPrice: number;
  mrp: number;
  quantity: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  paymentId: string;
  shippingAddress: ShippingAddress;
  items: OrderItem[];
  subtotal: number;
  shippingCharge: number;
  discountAmount: number;
  totalAmount: number;
  notes: string;
  cancelledReason: string;
  expectedDeliveryDate: string;
  deliveredAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface Review {
  id: number;
  userId: number;
  userEmail: string;
  productId: number;
  rating: number;
  title: string;
  body: string;
  verifiedPurchase: boolean;
  helpfulCount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<number, number>;
}

export interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  activeOrders: number;
  revenueToday: number;
  revenueThisMonth: number;
  recentOrders: RecentOrder[];
}

export interface RecentOrder {
  id: number;
  orderNumber: string;
  userEmail: string;
  status: string;
  paymentStatus: string;
  totalAmount: number;
  itemCount: number;
  createdAt: string;
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  expiresInMs: number;
}

export interface UserMe {
  id: number;
  email: string;
  role: string;
  enabled: boolean;
}
