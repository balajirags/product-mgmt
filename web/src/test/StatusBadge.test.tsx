import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge } from '@/components/ui/StatusBadge';
import type { ProductStatus } from '@/types/product';

const statuses: ProductStatus[] = ['DRAFT', 'PUBLISHED', 'PROPOSED', 'REJECTED'];

describe('StatusBadge', () => {
  statuses.forEach((status) => {
    it(`renders ${status}`, () => {
      render(<StatusBadge status={status} />);
      expect(screen.getByText(status)).toBeDefined();
    });
  });
});
