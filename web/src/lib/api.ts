import { config } from './config';
import type {
  CreateProductRequest,
  UpdateProductRequest,
  BatchProductRequest,
  ProductResponse,
  PagedProductResponse,
  BatchProductResponse,
  ProblemDetail,
  ProductStatus,
} from '@/types/product';
import { ApiError } from '@/types/product';

const BASE = `${config.apiBaseUrl}/api/v1`;

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 10_000);

  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      ...init,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...init?.headers,
      },
    });
  } catch (err) {
    throw err instanceof Error ? err : new Error('Network error');
  } finally {
    clearTimeout(timeoutId);
  }

  if (!res.ok) {
    let problem: ProblemDetail;
    try {
      problem = (await res.json()) as ProblemDetail;
    } catch {
      problem = {
        type: 'about:blank',
        title: res.statusText,
        status: res.status,
        detail: res.statusText,
        instance: path,
      };
    }
    throw new ApiError(problem, res.status);
  }

  if (res.status === 204) return undefined as unknown as T;
  return res.json() as Promise<T>;
}

// ── Product API ───────────────────────────────────────────────────────────────

export interface ListProductsParams {
  status?: ProductStatus;
  page?: number;
  size?: number;
  sort?: string;
}

export function listProducts(params: ListProductsParams = {}): Promise<PagedProductResponse> {
  const qs = new URLSearchParams();
  if (params.status) qs.set('status', params.status);
  if (params.page !== undefined) qs.set('page', String(params.page));
  if (params.size !== undefined) qs.set('size', String(params.size));
  if (params.sort) qs.set('sort', params.sort);
  const query = qs.toString() ? `?${qs.toString()}` : '';
  return request<PagedProductResponse>(`/products${query}`);
}

export function getProduct(id: string): Promise<ProductResponse> {
  return request<ProductResponse>(`/products/${id}`);
}

export function createProduct(body: CreateProductRequest): Promise<ProductResponse> {
  return request<ProductResponse>('/products', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function updateProduct(id: string, body: UpdateProductRequest): Promise<ProductResponse> {
  return request<ProductResponse>(`/products/${id}`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function deleteProduct(id: string): Promise<void> {
  return request<void>(`/products/${id}`, { method: 'DELETE' });
}

export function batchProducts(body: BatchProductRequest): Promise<BatchProductResponse> {
  return request<BatchProductResponse>('/products/batch', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}
