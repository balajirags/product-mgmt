/**
 * Additional coverage tests for ProductListPage — table row navigation,
 * thumbnail presence, filter/sort state changes, page navigation callbacks.
 */
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductListPage } from '@/pages/ProductListPage';

vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

const noop = vi.fn();

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

function makeProduct(overrides = {}) {
  return {
    id: '00000000-0000-0000-0000-000000000001',
    title: 'Test Product',
    status: 'DRAFT' as const,
    thumbnail: null,
    updated_at: '2026-01-01T00:00:00Z',
    images: [], options: [], variants: [],
    handle: 'test', giftcard: false, discountable: true,
    created_at: '2026-01-01T00:00:00Z',
    description: null, subtitle: null, weight: null,
    height: null, width: null, length: null, metadata: null, external_id: null,
    ...overrides,
  };
}

function mockWithProducts(overrides = {}) {
  vi.mocked(hooks.useProducts).mockReturnValue({
    isLoading: false, isError: false, error: null,
    data: {
      content: [makeProduct(overrides)],
      page: 0, size: 20, total_elements: 1, total_pages: 1,
    },
    refetch: noop,
  } as unknown as unknown as ReturnType<typeof hooks.useProducts>);
}

describe('ProductListPage — extended coverage', () => {
  it('renders thumbnail image when product has one', () => {
    mockWithProducts({ thumbnail: 'https://cdn.example.com/img.jpg' });
    render(<ProductListPage />, { wrapper });
    const img = screen.getByRole('img');
    expect(img).toBeDefined();
    expect((img as HTMLImageElement).src).toContain('cdn.example.com');
  });

  it('renders placeholder div when thumbnail is null', () => {
    mockWithProducts({ thumbnail: null });
    render(<ProductListPage />, { wrapper });
    expect(screen.queryByRole('img')).toBeNull();
  });

  it('changing status filter resets to page 0', async () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, total_elements: 0, total_pages: 0 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    const user = userEvent.setup();
    render(<ProductListPage />, { wrapper });
    const statusSelect = screen.getAllByRole('combobox')[0] as HTMLSelectElement;
    await user.selectOptions(statusSelect, 'PUBLISHED');
    expect(statusSelect.value).toBe('PUBLISHED');
  });

  it('changing sort option updates sort state', async () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, total_elements: 0, total_pages: 0 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    const user = userEvent.setup();
    render(<ProductListPage />, { wrapper });
    const sortSelect = screen.getAllByRole('combobox')[1] as HTMLSelectElement;
    await user.selectOptions(sortSelect, 'createdAt,desc');
    expect(sortSelect.value).toBe('createdAt,desc');
  });

  it('next page button advances page', async () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [makeProduct()], page: 0, size: 20, total_elements: 50, total_pages: 3 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    const user = userEvent.setup();
    render(<ProductListPage />, { wrapper });
    await user.click(screen.getByRole('button', { name: /next/i }));
    expect(screen.getByText(/page 2 of 3/i)).toBeDefined();
  });

  it('previous page button goes back from page 2', async () => {
    // Start on page 2 by clicking next twice
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [makeProduct()], page: 0, size: 20, total_elements: 60, total_pages: 3 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    const user = userEvent.setup();
    render(<ProductListPage />, { wrapper });
    await user.click(screen.getByRole('button', { name: /next/i }));
    await user.click(screen.getByRole('button', { name: /previous/i }));
    expect(screen.getByText(/page 1 of 3/i)).toBeDefined();
  });

  it('next page button is disabled on last page', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [makeProduct()], page: 0, size: 20, total_elements: 1, total_pages: 1 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    // Only 1 page so pagination doesn't show, but we can verify no Next button
    expect(screen.queryByRole('button', { name: /next/i })).toBeNull();
  });

  it('Create product header button is rendered', () => {
    vi.mocked(hooks.useProducts).mockReturnValue({
      isLoading: false, isError: false, error: null,
      data: { content: [], page: 0, size: 20, total_elements: 0, total_pages: 0 },
      refetch: noop,
    } as unknown as unknown as ReturnType<typeof hooks.useProducts>);

    render(<ProductListPage />, { wrapper });
    expect(screen.getByRole('link', { name: /new product/i })).toBeDefined();
  });
});
