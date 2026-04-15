import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductDetailPage } from '@/pages/ProductDetailPage';
import { ApiError } from '@/types/product';
import type { ProblemDetail } from '@/types/product';

vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

function renderAtPath(path: string) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/products/:id" element={<ProductDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const baseMutationResult = { mutateAsync: vi.fn(), isPending: false, isError: false };
const noop = vi.fn();

const fullProduct = {
  id: '00000000-0000-0000-0000-000000000001',
  title: 'Classic Sneaker',
  handle: 'classic-sneaker',
  status: 'PUBLISHED' as const,
  description: 'A great shoe',
  subtitle: 'Comfy',
  giftcard: false, discountable: true,
  weight: 0.35, height: 12.0, width: 8.0, length: 30.0,
  metadata: { brand: 'Acme' },
  external_id: 'EXT-1',
  thumbnail: null,
  created_at: '2026-01-01T00:00:00Z',
  updated_at: '2026-01-02T00:00:00Z',
  images: [{ url: 'https://cdn.example.com/img.jpg', rank: 0 }],
  options: [{ id: 'opt1', title: 'Size', values: ['S', 'M'], created_at: '2026-01-01T00:00:00Z', updated_at: '2026-01-01T00:00:00Z' }],
  variants: [{ id: 'v1', title: 'S', sku: 'SNK-S', barcode: null, weight: null, height: null, width: null, length: null, manage_inventory: true, allow_backorder: false, option_values: { Size: 'S' }, created_at: '2026-01-01T00:00:00Z', updated_at: '2026-01-01T00:00:00Z' }],
};

describe('ProductDetailPage', () => {
  it('shows "Invalid product ID" for non-UUID path', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: false, error: null, data: undefined, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/not-a-uuid');
    expect(screen.getByText(/invalid product id/i)).toBeDefined();
  });

  it('shows loading state for valid UUID', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: true, isError: false, error: null, data: undefined, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    const { container } = renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(container.querySelector('.animate-pulse')).not.toBeNull();
  });

  it('shows "Product not found" on 404', () => {
    const problem: ProblemDetail = { type: 'about:blank', title: 'Not Found', status: 404, detail: 'Product not found', instance: '/api/v1/products/x', error_code: 'PRODUCT_NOT_FOUND' };
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: true, error: new ApiError(problem, 404), data: undefined, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(screen.getByText(/product not found/i)).toBeDefined();
    expect(screen.getByRole('link', { name: /back to products/i })).toBeDefined();
  });

  it('shows generic error with retry for non-404', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: true, error: new Error('Server error'), data: undefined, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(screen.getByRole('button', { name: /retry/i })).toBeDefined();
  });

  it('renders full product detail', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: false, error: null, data: fullProduct, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(screen.getByRole('heading', { level: 1, name: 'Classic Sneaker' })).toBeDefined();
    expect(screen.getByText('PUBLISHED')).toBeDefined();
    expect(screen.getByText(/classic-sneaker/i)).toBeDefined();
    expect(screen.getByText('Size')).toBeDefined();
    expect(screen.getByText('SNK-S')).toBeDefined();
  });

  it('shows Edit and Delete buttons', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: false, error: null, data: fullProduct, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(screen.getByRole('link', { name: /edit/i })).toBeDefined();
    expect(screen.getByRole('button', { name: /delete/i })).toBeDefined();
  });

  it('opens delete confirmation dialog on Delete click', async () => {
    const user = userEvent.setup();
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: false, error: null, data: fullProduct, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    await user.click(screen.getByRole('button', { name: /^delete$/i }));
    expect(screen.getByRole('dialog')).toBeDefined();
    expect(screen.getByText(/delete product/i)).toBeDefined();
  });

  it('shows "No images" when product has no images', () => {
    const noImages = { ...fullProduct, images: [] };
    vi.mocked(hooks.useProduct).mockReturnValue({ isLoading: false, isError: false, error: null, data: noImages, refetch: noop } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
    vi.mocked(hooks.useDeleteProduct).mockReturnValue(baseMutationResult as unknown as ReturnType<typeof hooks.useDeleteProduct>);

    renderAtPath('/products/00000000-0000-0000-0000-000000000001');
    expect(screen.getByText(/no images/i)).toBeDefined();
  });
});
