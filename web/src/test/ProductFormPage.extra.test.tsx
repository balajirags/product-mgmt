/**
 * Extended coverage tests for ProductFormPage — options, variants, images,
 * edit mode pre-population, dimension inputs.
 */
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
          <Route path="/products/:id" element={<div>Detail page</div>} />
          <Route path="/products" element={<div>Product list</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function renderEdit(id: string) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[`/products/${id}/edit`]}>
        <Routes>
          <Route path="/products/:id/edit" element={<ProductFormPage mode="edit" />} />
          <Route path="/products/:id" element={<div>Detail page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const existingProduct = {
  id: '00000000-0000-0000-0000-000000000001',
  title: 'Existing Product', handle: 'existing-product', status: 'PUBLISHED' as const,
  description: 'A desc', subtitle: 'Sub', giftcard: false, discountable: true,
  weight: 1.5, height: 10.0, width: 5.0, length: 20.0,
  metadata: { brand: 'Acme' }, externalId: 'EXT-1',
  thumbnail: 'https://cdn.example.com/thumb.jpg',
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-02T00:00:00Z',
  images: [{ url: 'https://cdn.example.com/img.jpg', rank: 0 }],
  options: [{ id: 'opt1', title: 'Size', values: ['S', 'M'], createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }],
  variants: [{ id: 'v1', title: 'S', sku: 'SKU-S', barcode: null, weight: null, height: null, width: null, length: null, manageInventory: true, allowBackorder: false, optionValues: { Size: 'S' }, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }],
};

describe('ProductFormPage — options and images', () => {
  it('Add option button renders new option row', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /\+ add option/i }));
    expect(screen.getByPlaceholderText(/name.*size/i)).toBeDefined();
  });

  it('Remove option button removes the row', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /\+ add option/i }));
    const removeBtn = screen.getByRole('button', { name: /^remove$/i });
    await user.click(removeBtn);
    expect(screen.queryByPlaceholderText(/name.*size/i)).toBeNull();
  });

  it('Add variant button renders new variant row', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /\+ add variant/i }));
    expect(screen.getByPlaceholderText(/^sku$/i)).toBeDefined();
  });

  it('Remove variant button removes the row', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /\+ add variant/i }));
    // Find the Remove button inside the variants fieldset
    const removeButtons = screen.getAllByRole('button', { name: /^remove$/i });
    await user.click(removeButtons[0]);
    expect(screen.queryByPlaceholderText(/^sku$/i)).toBeNull();
  });

  it('Add image button adds a second image row', async () => {
    const user = userEvent.setup();
    renderCreate();
    const before = screen.getAllByPlaceholderText(/cdn\.example\.com/).length;
    await user.click(screen.getByRole('button', { name: /\+ add image/i }));
    const after = screen.getAllByPlaceholderText(/cdn\.example\.com/).length;
    expect(after).toBe(before + 1);
  });

  it('Remove image button appears and removes a row when 2+ images exist', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.click(screen.getByRole('button', { name: /\+ add image/i }));
    const removeImgBtn = screen.getAllByRole('button', { name: /^remove$/i })[0];
    await user.click(removeImgBtn);
    expect(screen.getAllByPlaceholderText(/cdn\.example\.com/).length).toBe(1);
  });

  it('option with blank title shows validation error on submit', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.type(screen.getAllByRole('textbox')[0], 'My Product');
    await user.click(screen.getByRole('button', { name: /\+ add option/i }));
    // Leave option title blank, fill values
    const valuesInput = screen.getByPlaceholderText(/comma-separated/i);
    await user.type(valuesInput, 'S, M');
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(screen.getByText(/option name is required/i)).toBeDefined();
  });

  it('option with blank values shows validation error on submit', async () => {
    const user = userEvent.setup();
    renderCreate();
    await user.type(screen.getAllByRole('textbox')[0], 'My Product');
    await user.click(screen.getByRole('button', { name: /\+ add option/i }));
    const nameInput = screen.getByPlaceholderText(/name.*e\.g.*size/i);
    await user.type(nameInput, 'Size');
    // Leave values blank
    await user.click(screen.getByRole('button', { name: /^create product$/i }));
    expect(screen.getByText(/at least one value required/i)).toBeDefined();
  });
});

describe('ProductFormPage — edit mode', () => {
  it('shows "Edit product" heading in edit mode', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    expect(screen.getByRole('heading', { name: /edit product/i })).toBeDefined();
  });

  it('pre-populates form with existing product values', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    const titleInput = screen.getAllByRole('textbox')[0] as HTMLInputElement;
    expect(titleInput.value).toBe('Existing Product');
  });

  it('shows loading state in edit mode while fetching', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: true, isError: false, error: null, data: undefined, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    expect(screen.getByText(/loading/i)).toBeDefined();
  });

  it('shows Save changes button in edit mode', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    expect(screen.getByRole('button', { name: /save changes/i })).toBeDefined();
  });

  it('pre-populates existing images', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    const imageInput = screen.getAllByPlaceholderText(/cdn\.example\.com/)[0] as HTMLInputElement;
    expect(imageInput.value).toContain('cdn.example.com/img.jpg');
  });

  it('pre-populates existing options', () => {
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    renderEdit('00000000-0000-0000-0000-000000000001');
    const optionInput = screen.getByPlaceholderText(/name.*e\.g.*size/i) as HTMLInputElement;
    expect(optionInput.value).toBe('Size');
  });

  it('calls updateProduct mutateAsync on submit', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({ ...existingProduct, title: 'Updated' });
    vi.mocked(hooks.useUpdateProduct).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useUpdateProduct>);
    vi.mocked(hooks.useProduct).mockReturnValue({
      isLoading: false, isError: false, error: null, data: existingProduct, refetch: vi.fn(),
    } as unknown as unknown as ReturnType<typeof hooks.useProduct>);

    const user = userEvent.setup();
    renderEdit('00000000-0000-0000-0000-000000000001');
    await user.click(screen.getByRole('button', { name: /save changes/i }));
    expect(mutateAsync).toHaveBeenCalled();
  });
});
