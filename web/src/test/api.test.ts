import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ApiError } from '@/types/product';

// Mock global fetch
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function makeResponse(body: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: 'OK',
    json: () => Promise.resolve(body),
  } as unknown as Response;
}

describe('API client', () => {
  beforeEach(() => { vi.clearAllMocks(); });
  afterEach(() => { vi.restoreAllMocks(); });

  it('listProducts — returns PagedProductResponse', async () => {
    const payload = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
    mockFetch.mockResolvedValueOnce(makeResponse(payload));
    const { listProducts } = await import('@/lib/api');
    const result = await listProducts();
    expect(result.totalElements).toBe(0);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/products'),
      expect.objectContaining({ headers: expect.objectContaining({ 'Content-Type': 'application/json' }) }),
    );
  });

  it('listProducts — passes status filter', async () => {
    mockFetch.mockResolvedValueOnce(makeResponse({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }));
    const { listProducts } = await import('@/lib/api');
    await listProducts({ status: 'PUBLISHED' });
    const url = mockFetch.mock.calls[0][0] as string;
    expect(url).toContain('status=PUBLISHED');
  });

  it('getProduct — returns ProductResponse', async () => {
    const product = { id: 'abc', title: 'Test', status: 'DRAFT', images: [], options: [], variants: [] };
    mockFetch.mockResolvedValueOnce(makeResponse(product));
    const { getProduct } = await import('@/lib/api');
    const result = await getProduct('abc');
    expect(result.id).toBe('abc');
  });

  it('createProduct — sends POST with body', async () => {
    const product = { id: '123', title: 'New', status: 'DRAFT', images: [], options: [], variants: [] };
    mockFetch.mockResolvedValueOnce({ ok: true, status: 201, json: () => Promise.resolve(product) } as unknown as Response);
    const { createProduct } = await import('@/lib/api');
    const result = await createProduct({ title: 'New' });
    expect(result.title).toBe('New');
    const call = mockFetch.mock.calls[0];
    expect(call[1].method).toBe('POST');
    expect(JSON.parse(call[1].body as string).title).toBe('New');
  });

  it('deleteProduct — sends DELETE, resolves on 204', async () => {
    mockFetch.mockResolvedValueOnce({ ok: true, status: 204, json: () => Promise.resolve(null) } as unknown as Response);
    const { deleteProduct } = await import('@/lib/api');
    await expect(deleteProduct('abc')).resolves.toBeUndefined();
  });

  it('API 404 throws ApiError with status 404', async () => {
    const problem = { type: 'about:blank', title: 'Not Found', status: 404, detail: 'Product not found', instance: '/api/v1/products/x', error_code: 'PRODUCT_NOT_FOUND' };
    mockFetch.mockResolvedValueOnce(makeResponse(problem, 404));
    const { getProduct } = await import('@/lib/api');
    let caught: unknown;
    try {
      await getProduct('x');
    } catch (err) {
      caught = err;
    }
    expect(caught).toBeInstanceOf(ApiError);
    expect((caught as ApiError).status).toBe(404);
    expect((caught as ApiError).problem.error_code).toBe('PRODUCT_NOT_FOUND');
  });

  it('API 400 with field_errors throws ApiError', async () => {
    mockFetch.mockResolvedValueOnce(makeResponse({
      type: 'about:blank', title: 'Validation Failed', status: 400,
      detail: 'Validation failed', instance: '/api/v1/products',
      field_errors: { title: 'title is required' },
    }, 400));
    const { createProduct } = await import('@/lib/api');
    await expect(createProduct({ title: '' })).rejects.toBeInstanceOf(ApiError);
  });

  it('API 409 duplicate handle throws ApiError', async () => {
    mockFetch.mockResolvedValueOnce(makeResponse({
      type: 'about:blank', title: 'Business Rule Violation', status: 409,
      detail: "A product with handle 'test' already exists", instance: '/api/v1/products',
      error_code: 'DUPLICATE_HANDLE',
    }, 409));
    const { createProduct } = await import('@/lib/api');
    await expect(createProduct({ title: 'Test' })).rejects.toBeInstanceOf(ApiError);
  });
});
