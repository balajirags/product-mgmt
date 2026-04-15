import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { NotFoundPage } from '@/pages/NotFoundPage';

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  );
}

describe('NotFoundPage', () => {
  it('renders 404 message', () => {
    render(<NotFoundPage />, { wrapper });
    expect(screen.getByText(/404/)).toBeDefined();
    expect(screen.getByRole('link', { name: /back to products/i })).toBeDefined();
  });
});
