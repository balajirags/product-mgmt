/**
 * Targeted tests to close remaining coverage gaps:
 * - api.ts:31 true branch (Error thrown by fetch)
 * - api.ts:68 (page param branch in listProducts)
 * - ProductDetailPage:163 (deleteError banner)
 * - ProductListPage:112-115 (onMouseEnter/Leave table row handlers)
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductDetailPage } from '@/pages/ProductDetailPage';
import { ProductListPage } from '@/pages/ProductListPage';
import { ApiError } from '@/types/product';
import type { ProblemDetail } from '@/types/product';

// ── API client gap tests ──────────────────────────────────────────────────────

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('api.ts — coverage gaps', () => {
  beforeEach(() => { vi.resetAllMocks(); });

  it('fetch throwing a real Error re-throws it directly (line 31 true branch)', async () => {
    const networkErr = new Error('Connection refused');
    mockFetch.mockRejectedValueOnce(networkErr);
    const { getProduct } = await import('@/lib/api');
    let caught: unknown;
    try { await getProduct('x'); } catch (e) { caught = e; }
    expect(caught).toBe(networkErr); // same reference — not re-wrapped
  });

  it('listProducts with page param — covers page !== undefined branch (line 68)', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true, status: 200,
      json: () => Promise.resolve({ content: [], page: 1, size: 20, totalElements: 0, totalPages: 0 }),
    });
    const { listProducts } = await import('@/lib/api');
    await listProducts({ page: 1 });
    const url = mockFetch.mock.calls[0][0] as string;
    expect(url).toContain('page=1');
  });
});

// ── ProductDetailPage — deleteError banner ────────────────────────────────────

vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

const fullProduct = {
  id: '00000000-0000-0000-0000-000000000099',
  title: 'Deletable Product', handle: 'del', status: 'DRAFT' as const,
  description: null, subtitle: null, giftcard: false, discountable: true,
  weight: null, height: null, width: null, length: null,
  metadata: null, externalId: null, thumbnail: null,
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-02T00:00:00Z',
  images: [], options: [], variants: [],
};

function renderDetail(id: string) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[`/products/${id}`]}>
        <Routes>
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/products" element={<div>list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('ProductDetailPage — delete error path', () => {
  beforeEach(() => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: fullProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
  });

  it('shows delete error when DELETE returns 404 (line 163)', async () => {
    const problem: ProblemDetail = { type: 'about:blank', title: 'Not Found', status: 404, detail: 'Product not found', instance: '/api/v1/products/x' };
    const mutateAsync = vi.fn().mockRejectedValue(new ApiError(problem, 404));
    vi.mocked(hooks.useDeleteProduct).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    const { default: userEvent } = await import('@testing-library/user-event');
    const user = userEvent.setup();
    renderDetail('00000000-0000-0000-0000-000000000099');
    // Open dialog (first Delete button = page action)
    await user.click(screen.getAllByRole('button', { name: /^delete$/i })[0]);
    // Confirm delete (second Delete button = dialog confirm)
    await user.click(screen.getAllByRole('button', { name: /^delete$/i })[1]);
    // Error message should appear
    expect(await screen.findByText(/product not found/i)).toBeDefined();
  });
});

// ── ProductListPage — mouseenter/mouseleave ───────────────────────────────────

const noop = vi.fn();

function wrapList({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

const product = {
  id: '1', title: 'Hover Test Product', status: 'DRAFT' as const,
  thumbnail: null, updatedAt: '2026-01-01T00:00:00Z',
  images: [], options: [], variants: [],
  handle: 'h', giftcard: false, discountable: true,
  createdAt: '2026-01-01T00:00:00Z',
  description: null, subtitle: null, weight: null,
  height: null, width: null, length: null, metadata: null, externalId: null,
};

describe('ProductListPage — row hover handlers (lines 112–115)', () => {
  it('fires onMouseEnter and onMouseLeave on table row without errors', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [product], page: 0, size: 20, totalElements: 1, totalPages: 1 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper: wrapList });
    const row = screen.getByText('Hover Test Product').closest('tr')!;
    fireEvent.mouseEnter(row);
    expect(row.style.background).toBe('rgb(249, 250, 251)');
    fireEvent.mouseLeave(row);
    expect(row.style.background).toBe('');
  });
});
