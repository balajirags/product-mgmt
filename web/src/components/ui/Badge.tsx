import type { ProductStatus } from '@/types/product';

const styles: Record<ProductStatus, string> = {
  DRAFT:     'bg-slate-100 text-slate-600',
  PUBLISHED: 'bg-emerald-100 text-emerald-700',
  PROPOSED:  'bg-blue-100 text-blue-700',
  REJECTED:  'bg-red-100 text-red-700',
};

export function StatusBadge({ status }: { status: ProductStatus }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${styles[status] ?? styles.DRAFT}`}>
      {status}
    </span>
  );
}
