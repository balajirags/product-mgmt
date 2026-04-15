import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '@/pages/ProductListPage';

// Mock the hooks module
vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

const noop = vi.fn();

describe('ProductListPage', () => {
  it('shows loading state', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: true, isError: false, error: null, data: undefined, refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    expect(screen.getByText(/loading products/i)).toBeDefined();
  });

  it('shows error banner with retry on API failure', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: true,
      error: new Error('Network error'),
      data: undefined, refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    expect(screen.getByText(/failed to load products/i)).toBeDefined();
    expect(screen.getByRole('button', { name: /retry/i })).toBeDefined();
  });

  it('shows empty state with CTA when no products', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    expect(screen.getByText(/no products yet/i)).toBeDefined();
    expect(screen.getByRole('button', { name: /create your first product/i })).toBeDefined();
  });

  it('renders product rows in table', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: {
        content: [
          { id: '1', title: 'Unique Sneaker XYZ', status: 'PUBLISHED', thumbnail: null, updatedAt: '2026-01-01T00:00:00Z', images: [], options: [], variants: [], handle: 'sneaker', giftcard: false, discountable: true, createdAt: '2026-01-01T00:00:00Z', description: null, subtitle: null, weight: null, height: null, width: null, length: null, metadata: null, externalId: null },
        ],
        page: 0, size: 20, totalElements: 1, totalPages: 1,
      },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    // Title is unique — safe to use getByText
    expect(screen.getByText('Unique Sneaker XYZ')).toBeDefined();
    // Status badge appears inside table row; there are multiple "PUBLISHED" elements (badge + dropdown option)
    expect(screen.getAllByText('PUBLISHED').length).toBeGreaterThan(0);
  });

  it('shows pagination when totalPages > 1', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: {
        content: [{ id: '1', title: 'A', status: 'DRAFT', thumbnail: null, updatedAt: '2026-01-01T00:00:00Z', images: [], options: [], variants: [], handle: 'a', giftcard: false, discountable: true, createdAt: '2026-01-01T00:00:00Z', description: null, subtitle: null, weight: null, height: null, width: null, length: null, metadata: null, externalId: null }],
        page: 0, size: 20, totalElements: 50, totalPages: 3,
      },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    expect(screen.getByText(/page 1 of 3/i)).toBeDefined();
    expect(screen.getByRole('button', { name: /next/i })).toBeDefined();
  });

  it('previous page button is disabled on page 0', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, totalElements: 50, totalPages: 3 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    const prev = screen.getByRole('button', { name: /previous/i });
    expect((prev as HTMLButtonElement).disabled).toBe(true);
  });

  it('retry button calls refetch', async () => {
    const refetch = vi.fn();
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: true, error: new Error('fail'), data: undefined, refetch,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    const user = userEvent.setup();
    render(<ProductListPage />, { wrapper });
    await user.click(screen.getByRole('button', { name: /retry/i }));
    expect(refetch).toHaveBeenCalledOnce();
  });

  it('status filter select is rendered with all statuses', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    const select = screen.getByRole('combobox', { name: /status/i }) as HTMLSelectElement;
    const options = Array.from(select.options).map((o) => o.value);
    expect(options).toContain('PUBLISHED');
    expect(options).toContain('DRAFT');
  });
});
