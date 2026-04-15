/**
 * Extended coverage tests for BatchPage — updates/deletes tab content,
 * results display, mixed batch submission.
 */
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

describe('BatchPage — updates tab', () => {
  it('submits update with id and title', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({ created: [], updated: [], deleted: [] });
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^updates$/i }));

    const inputs = screen.getAllByPlaceholderText(/product uuid/i);
    await user.type(inputs[0], '00000000-0000-0000-0000-000000000001');
    const titleInput = screen.getByPlaceholderText(/new title/i);
    await user.type(titleInput, 'Updated Title');

    await user.click(screen.getByRole('button', { name: /run batch/i }));
    expect(mutateAsync).toHaveBeenCalledWith(
      expect.objectContaining({
        update: [{ id: '00000000-0000-0000-0000-000000000001', data: { title: 'Updated Title' } }],
      })
    );
  });

  it('Add row button adds a second update row', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^updates$/i }));
    const before = screen.getAllByPlaceholderText(/product uuid/i).length;
    await user.click(screen.getByRole('button', { name: /\+ add row/i }));
    const after = screen.getAllByPlaceholderText(/product uuid/i).length;
    expect(after).toBe(before + 1);
  });

  it('Remove button removes an update row', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^updates$/i }));
    await user.click(screen.getByRole('button', { name: /\+ add row/i }));
    const removeBtns = screen.getAllByRole('button', { name: /^remove$/i });
    await user.click(removeBtns[0]);
    // Should be back to 1 row
    expect(screen.getAllByPlaceholderText(/product uuid/i).length).toBe(1);
  });
});

describe('BatchPage — deletes tab', () => {
  it('submits delete with id', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({ created: [], updated: [], deleted: [] });
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^deletes$/i }));

    const input = screen.getByPlaceholderText(/product uuid/i);
    await user.type(input, '00000000-0000-0000-0000-000000000002');
    await user.click(screen.getByRole('button', { name: /run batch/i }));

    expect(mutateAsync).toHaveBeenCalledWith(
      expect.objectContaining({ delete: ['00000000-0000-0000-0000-000000000002'] })
    );
  });

  it('Add row and Remove row in deletes tab', async () => {
    const user = userEvent.setup();
    renderBatch();
    await user.click(screen.getByRole('button', { name: /^deletes$/i }));
    await user.click(screen.getByRole('button', { name: /\+ add row/i }));
    expect(screen.getAllByPlaceholderText(/product uuid/i).length).toBe(2);
    const removeBtns = screen.getAllByRole('button', { name: /^remove$/i });
    await user.click(removeBtns[0]);
    expect(screen.getAllByPlaceholderText(/product uuid/i).length).toBe(1);
  });
});

describe('BatchPage — results display', () => {
  it('shows success results summary after batch completes', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({
      created: [{ id: '123', success: true, product: null, errorCode: null, errorMessage: null }],
      updated: [],
      deleted: [],
    });
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    const input = screen.getByPlaceholderText(/product title/i);
    await user.type(input, 'New Product');
    await user.click(screen.getByRole('button', { name: /run batch/i }));

    expect(await screen.findByText(/created/i)).toBeDefined();
    expect(await screen.findByText(/1 succeeded/i)).toBeDefined();
  });

  it('shows ✗ error badge for failed batch item', async () => {
    const mutateAsync = vi.fn().mockResolvedValue({
      created: [{ id: null, success: false, product: null, errorCode: 'DUPLICATE_HANDLE', errorMessage: 'duplicate' }],
      updated: [],
      deleted: [],
    });
    vi.mocked(hooks.useBatchProducts).mockReturnValue({ mutateAsync, isPending: false } as unknown as unknown as ReturnType<typeof hooks.useBatchProducts>);

    const user = userEvent.setup();
    renderBatch();
    const input = screen.getByPlaceholderText(/product title/i);
    await user.type(input, 'Dupe');
    await user.click(screen.getByRole('button', { name: /run batch/i }));

    expect(await screen.findByText(/DUPLICATE_HANDLE/i)).toBeDefined();
  });
});

describe('BatchPage — api.ts extra branches', () => {
  it('listProducts with size and sort params', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ content: [], page: 0, size: 5, total_elements: 0, total_pages: 0 }),
    });
    vi.stubGlobal('fetch', mockFetch);

    const { listProducts } = await import('@/lib/api');
    await listProducts({ size: 5, sort: 'title,asc' });

    const url = mockFetch.mock.calls[0][0] as string;
    expect(url).toContain('size=5');
    expect(url).toContain('sort=title');

    vi.unstubAllGlobals();
  });
});
