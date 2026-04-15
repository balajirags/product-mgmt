import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DeleteDialog } from '@/components/ui/DeleteDialog';

describe('DeleteDialog', () => {
  it('shows product title and warning', () => {
    render(<DeleteDialog productTitle="My Product" onConfirm={() => {}} onCancel={() => {}} isDeleting={false} />);
    expect(screen.getByText(/My Product/)).toBeDefined();
    expect(screen.getByText(/images.*options.*variants|all associated/i)).toBeDefined();
  });

  it('calls onConfirm when Delete clicked', async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    render(<DeleteDialog productTitle="P" onConfirm={onConfirm} onCancel={() => {}} isDeleting={false} />);
    await user.click(screen.getByRole('button', { name: /^Delete$/ }));
    expect(onConfirm).toHaveBeenCalledOnce();
  });

  it('calls onCancel when Cancel clicked', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    render(<DeleteDialog productTitle="P" onConfirm={() => {}} onCancel={onCancel} isDeleting={false} />);
    await user.click(screen.getByRole('button', { name: /cancel/i }));
    expect(onCancel).toHaveBeenCalledOnce();
  });

  it('disables buttons while deleting', () => {
    render(<DeleteDialog productTitle="P" onConfirm={() => {}} onCancel={() => {}} isDeleting={true} />);
    screen.getAllByRole('button').forEach((btn) => {
      expect(btn).toHaveProperty('disabled', true);
    });
  });
});
