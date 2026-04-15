import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useProducts } from '@/hooks/useProducts';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PRODUCT_STATUSES } from '@/types/product';
import type { ProductStatus } from '@/types/product';
import { ApiError } from '@/types/product';

const PAGE_SIZE = 20;

export function ProductListPage() {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<ProductStatus | ''>('');
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState('');

  const { data, isLoading, isError, error, refetch } = useProducts({
    status: statusFilter || undefined,
    page,
    size: PAGE_SIZE,
    sort: sort || undefined,
  });

  const errorMessage =
    error instanceof ApiError ? error.message : 'Failed to load products.';

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '2rem 1rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ margin: 0 }}>Products</h1>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/products/batch">
            <button>Batch operations</button>
          </Link>
          <Link to="/products/new">
            <button style={{ fontWeight: 600 }}>+ Create product</button>
          </Link>
        </div>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
        <label>
          Status:{' '}
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as ProductStatus | '');
              setPage(0);
            }}
          >
            <option value="">All</option>
            {PRODUCT_STATUSES.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </label>

        <label>
          Sort:{' '}
          <select value={sort} onChange={(e) => { setSort(e.target.value); setPage(0); }}>
            <option value="">Default</option>
            <option value="createdAt,desc">Newest first</option>
            <option value="createdAt,asc">Oldest first</option>
            <option value="title,asc">Title A–Z</option>
            <option value="title,desc">Title Z–A</option>
          </select>
        </label>
      </div>

      {/* Loading */}
      {isLoading && (
        <div style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
          Loading products…
        </div>
      )}

      {/* Error */}
      {isError && (
        <div style={{ padding: '1rem', background: '#fee2e2', borderRadius: 6, marginBottom: '1rem' }}>
          {errorMessage}{' '}
          <button onClick={() => refetch()}>Retry</button>
        </div>
      )}

      {/* Empty state */}
      {!isLoading && !isError && data?.content.length === 0 && (
        <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
          <p>No products yet.</p>
          <Link to="/products/new">
            <button>Create your first product</button>
          </Link>
        </div>
      )}

      {/* Table */}
      {!isLoading && !isError && (data?.content.length ?? 0) > 0 && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #e5e7eb', textAlign: 'left' }}>
              <th style={{ padding: '0.5rem' }}>Thumbnail</th>
              <th style={{ padding: '0.5rem' }}>Title</th>
              <th style={{ padding: '0.5rem' }}>Status</th>
              <th style={{ padding: '0.5rem' }}>Updated</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((p) => (
              <tr
                key={p.id}
                onClick={() => navigate(`/products/${p.id}`)}
                style={{ borderBottom: '1px solid #f3f4f6', cursor: 'pointer' }}
                onMouseEnter={(e) => (e.currentTarget.style.background = '#f9fafb')}
                onMouseLeave={(e) => (e.currentTarget.style.background = '')}
              >
                <td style={{ padding: '0.5rem' }}>
                  {p.thumbnail ? (
                    <img src={p.thumbnail} alt={p.title} width={40} height={40} style={{ objectFit: 'cover', borderRadius: 4 }} />
                  ) : (
                    <div style={{ width: 40, height: 40, background: '#e5e7eb', borderRadius: 4 }} />
                  )}
                </td>
                <td style={{ padding: '0.5rem', fontWeight: 500 }}>{p.title}</td>
                <td style={{ padding: '0.5rem' }}>
                  <StatusBadge status={p.status} />
                </td>
                <td style={{ padding: '0.5rem', color: '#6b7280', fontSize: '0.875rem' }}>
                  {new Date(p.updatedAt).toLocaleDateString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem', alignItems: 'center' }}>
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
            Page {page + 1} of {data.totalPages} ({data.totalElements} products)
          </span>
          <button disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      )}
    </div>
  );
}
