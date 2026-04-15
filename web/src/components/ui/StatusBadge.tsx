import type { ProductStatus } from '@/types/product';

const COLORS: Record<ProductStatus, { background: string; color: string }> = {
  DRAFT:     { background: '#e5e7eb', color: '#374151' },
  PUBLISHED: { background: '#d1fae5', color: '#065f46' },
  PROPOSED:  { background: '#dbeafe', color: '#1e40af' },
  REJECTED:  { background: '#fee2e2', color: '#991b1b' },
};

interface StatusBadgeProps {
  status: ProductStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const { background, color } = COLORS[status] ?? COLORS.DRAFT;
  return (
    <span
      style={{
        background,
        color,
        padding: '2px 8px',
        borderRadius: '12px',
        fontSize: '0.75rem',
        fontWeight: 600,
      }}
    >
      {status}
    </span>
  );
}
