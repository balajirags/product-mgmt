// ── Enums ─────────────────────────────────────────────────────────────────────

export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'PROPOSED' | 'REJECTED';

export const PRODUCT_STATUSES: ProductStatus[] = ['DRAFT', 'PUBLISHED', 'PROPOSED', 'REJECTED'];

// ── Request DTOs ──────────────────────────────────────────────────────────────

export interface ProductImageRequest {
  url: string;
}

export interface ProductOptionRequest {
  title: string;
  values: string[];
}

export interface ProductVariantRequest {
  title?: string | null;
  sku?: string | null;
  barcode?: string | null;
  weight?: number | null;
  height?: number | null;
  width?: number | null;
  length?: number | null;
  manage_inventory?: boolean;
  allow_backorder?: boolean;
  option_values?: Record<string, string> | null;
}

export interface CreateProductRequest {
  title: string;
  handle?: string | null;
  status?: ProductStatus;
  description?: string | null;
  subtitle?: string | null;
  weight?: number | null;
  height?: number | null;
  width?: number | null;
  length?: number | null;
  metadata?: Record<string, unknown> | null;
  external_id?: string | null;
  thumbnail?: string | null;
  images?: ProductImageRequest[] | null;
  options?: ProductOptionRequest[] | null;
  variants?: ProductVariantRequest[] | null;
}

export interface UpdateProductRequest {
  title?: string | null;
  handle?: string | null;
  status?: ProductStatus;
  description?: string | null;
  subtitle?: string | null;
  weight?: number | null;
  height?: number | null;
  width?: number | null;
  length?: number | null;
  metadata?: Record<string, unknown> | null;
  external_id?: string | null;
  thumbnail?: string | null;
  images?: ProductImageRequest[] | null;
  options?: ProductOptionRequest[] | null;
  variants?: ProductVariantRequest[] | null;
}

export interface BatchUpdateItem {
  id: string;
  data: UpdateProductRequest;
}

export interface BatchProductRequest {
  create?: CreateProductRequest[];
  update?: BatchUpdateItem[];
  delete?: string[];
}

// ── Response DTOs ─────────────────────────────────────────────────────────────

export interface ProductImageResponse {
  url: string;
  rank: number;
}

export interface ProductOptionResponse {
  id: string;
  title: string;
  values: string[];
  created_at: string;
  updated_at: string;
}

export interface ProductVariantResponse {
  id: string;
  title: string;
  sku: string | null;
  barcode: string | null;
  weight: number | null;
  height: number | null;
  width: number | null;
  length: number | null;
  manage_inventory: boolean;
  allow_backorder: boolean;
  option_values: Record<string, string>;
  created_at: string;
  updated_at: string;
}

export interface ProductResponse {
  id: string;
  title: string;
  handle: string;
  status: ProductStatus;
  description: string | null;
  subtitle: string | null;
  is_giftcard: boolean;
  discountable: boolean;
  weight: number | null;
  height: number | null;
  width: number | null;
  length: number | null;
  metadata: Record<string, unknown> | null;
  external_id: string | null;
  thumbnail: string | null;
  created_at: string;
  updated_at: string;
  images: ProductImageResponse[];
  options: ProductOptionResponse[];
  variants: ProductVariantResponse[];
}

export interface PagedProductResponse {
  content: ProductResponse[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}

export interface BatchItemResult {
  id: string | null;
  success: boolean;
  product: ProductResponse | null;
  errorCode: string | null;
  errorMessage: string | null;
}

export interface BatchProductResponse {
  created: BatchItemResult[];
  updated: BatchItemResult[];
  deleted: BatchItemResult[];
}

// ── Error ─────────────────────────────────────────────────────────────────────

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  error_code?: string | null;
  field_errors?: Record<string, string> | null;
}

export class ApiError extends Error {
  readonly problem: ProblemDetail;
  readonly status: number;

  constructor(problem: ProblemDetail, status: number) {
    super(problem.detail ?? problem.title);
    this.name = 'ApiError';
    this.problem = problem;
    this.status = status;
  }
}
