import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BatchPage } from '@/pages/BatchPage';

vi.mock('@/hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useProduct: vi.fn(),
  useCreateProduct: vi.fn(),
  useUpdateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
  useBatchProducts: vi.fn(),
}));

import * as hooks from '@/hooks/useProducts';

function renderBatch() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter>
        <BatchPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  vi.mocked(hooks.useBatchProducts).mockReturnValue({
    mutateAsync: vi.fn(),
    isPending: false,
  } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);
});

describe('BatchPage', () => {
  it('renders batch operations heading', () => {
    renderBatch();
    expect(screen.getByRole('heading', { name: /batch operations/i })).toBeDefined();
  });

  it('shows three tabs: Creates, Updates, Deletes', () => {
    renderBatch();
    expect(screen.getByRole('button', { name: /^creates$/i })).toBeDefined();
    expect(screen.getByRole('button', { name: /^updates$/i })).toBeDefined();
    expect(screen.getByRole('button', { name: /^deletes$/i })).toBeDefined();
  });

  it('shows empty batch validation error on submit with no data', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /run batch/i }));
    expect(screen.getByText(/add at least one operation/i)).toBeDefined();
  });

  it('calls mutateAsync when creates has a title', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({ created: [], updated: [], deleted: [] });
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    const input = screen.getByPlaceholderText(/product title/i) as HTMLInputElement;
    await user.type(input, 'New Batch Product');
    await user.click(screen.getByRole('button', { name: /run batch/i }));
    expect(mutateAsync).toHaveBeenCalledWith(
      expect.objectContaining({ create: [{ title: 'New Batch Product' }] })
    );
  });

  it('Add row button adds a new input row', async () => {
    const user = userEvent.setup();
    renderBatch();
    const before = screen.getAllByPlaceholderText(/product title/i).length;
    await user.click(screen.getByRole('button', { name: /\+ add row/i }));
    const after = screen.getAllByPlaceholderText(/product title/i).length;
    expect(after).toBe(before + 1);
  });

  it('Remove button removes a row when multiple rows exist', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /\+ add row/i }));
    const removeButtons = screen.getAllByRole('button', { name: /^remove$/i });
    expect(removeButtons.length).toBeGreaterThan(0);
    await user.click(removeButtons[0]);
    expect(screen.getAllByPlaceholderText(/product title/i).length).toBe(1);
  });

  it('shows server error when batch API fails', async () => {
    const { ApiError } = await import('@/types/product');
    const mutateAsync = vi.fn().mockRejectedValue(
      new ApiError({ type: 'about:blank', title: 'Error', status: 400, detail: 'Batch failed', instance: '/api/v1/products/batch' }, 400)
    );
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    const input = screen.getByPlaceholderText(/product title/i) as HTMLInputElement;
    await user.type(input, 'Fail Product');
    await user.click(screen.getByRole('button', { name: /run batch/i }));
    expect(await screen.findByText(/batch failed/i)).toBeDefined();
  });

  it('switches tab to Updates on click', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^updates$/i }));
    expect(screen.getByPlaceholderText(/product uuid/i)).toBeDefined();
  });

  it('switches tab to Deletes on click', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^deletes$/i }));
    expect(screen.getAllByPlaceholderText(/product uuid/i).length).toBeGreaterThan(0);
  });
});
