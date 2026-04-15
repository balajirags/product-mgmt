import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductFormPage } from '@/pages/ProductFormPage';

vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

const noopMutation = { mutateAsync: vi.fn(), isPending: false };

beforeEach(() => {
  vi.mocked(hooks.useCreateProduct).mockReturnValue(noopMutation as unknown as ReturnType<typeof hooks.useCreateProduct>);
  vi.mocked(hooks.useUpdateProduct).mockReturnValue(noopMutation as unknown as ReturnType<typeof hooks.useUpdateProduct>);
  vi.mocked(hooks.useProduct).mockReturnValue({
    isLoading: false, isError: false, error: null, data: undefined, refetch: vi.fn(),
  } as unknown as unknown as ReturnType<typeof hooks.useProduct>);
});

function renderCreate() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/products/new']}>
        <Routes>
          <Route path="/products/new" element={<ProductFormPage mode="create" />} />
          <Route path="/products" element={<div>Product list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('ProductFormPage — create mode', () => {
  it('renders create form heading', () => {
    renderCreate();
    expect(screen.getByRole('heading', { name: /create product/i })).toBeDefined();
  });

  it('shows inline error when title is empty on submit', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(screen.getByText(/title is required/i)).toBeDefined();
  });

  it('does not call mutateAsync when title is empty', async () => {
    const mutateAsync = vi.fn();
    vi.mocked(hooks.useCreateProduct).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useCreateProduct>);

    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(mutateAsync).not.toHaveBeenCalled();
  });

  it('calls mutateAsync with title when form is valid', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({ id: '123', title: 'New Shoe' });
    vi.mocked(hooks.useCreateProduct).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useCreateProduct>);

    const user = userEvent.setup();
    renderCreate();
    // First textbox in the form is the Title field
    const titleInput = screen.getAllByRole('textbox')[0];
    await user.type(titleInput, 'New Shoe');
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(mutateAsync).toHaveBeenCalledWith(expect.objectContaining({ title: 'New Shoe' }));
  });

  it('cancel button is present', () => {
    renderCreate();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeDefined();
  });

  it('shows handle hint text', () => {
    renderCreate();
    expect(screen.getByText(/auto-generated from title/i)).toBeDefined();
  });

  it('shows server error when API returns 409', async () => {
    const { ApiError } = await import('@/types/product');
    const mutateAsync = vi.fn().mockRejectedValue(
      new ApiError({
        type: 'about:blank', title: 'Conflict', status: 409,
        detail: "A product with handle 'test' already exists",
        instance: '/api/v1/products', error_code: 'DUPLICATE_HANDLE',
      }, 409)
    );
    vi.mocked(hooks.useCreateProduct).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useCreateProduct>);

    const user = userEvent.setup();
    renderCreate();
    await user.type(screen.getAllByRole('textbox')[0], 'Test');
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(await screen.findByText(/already exists/i)).toBeDefined();
  });

  it('status select defaults to DRAFT', () => {
    renderCreate();
    const selects = screen.getAllByRole('combobox');
    const statusSelect = selects[0] as HTMLSelectElement;
    expect(statusSelect.value).toBe('DRAFT');
  });

  it('invalid JSON in metadata shows validation error', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.type(screen.getAllByRole('textbox')[0], 'Test Product');
    const metadataArea = screen.getByPlaceholderText(/{"brand"/i);
    await user.type(metadataArea, '{{not valid json');
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(screen.getByText(/invalid json/i)).toBeDefined();
  });
});
