import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useProducts } from '@/hooks/useProducts';
import { StatusBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { TableSkeleton } from '@/components/ui/Skeleton';
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

  const errorMessage = error instanceof ApiError ? error.message : 'Failed to load products.';
  const isEmpty = !isLoading && !isError && data?.content.length === 0;
  const hasData = !isLoading && !isError && (data?.content.length ?? 0) > 0;

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Products</h1>
          {data && !isLoading && (
            <p className="mt-1 text-sm text-slate-500">
              {data.total_elements} {data.total_elements === 1 ? 'product' : 'products'}
            </p>
          )}
        </div>
        <div className="flex items-center gap-2">
          <Link to="/products/batch">
            <Button variant="secondary" size="sm">Batch operations</Button>
          </Link>
          <Link to="/products/new">
            <Button variant="primary" size="sm">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              New product
            </Button>
          </Link>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value as ProductStatus | ''); setPage(0); }}
          aria-label="Filter by status"
          className="px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-violet-500 cursor-pointer"
        >
          <option value="">All statuses</option>
          {PRODUCT_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>

        <select
          value={sort}
          onChange={(e) => { setSort(e.target.value); setPage(0); }}
          aria-label="Sort order"
          className="px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-violet-500 cursor-pointer"
        >
          <option value="">Default order</option>
          <option value="createdAt,desc">Newest first</option>
          <option value="createdAt,asc">Oldest first</option>
          <option value="title,asc">Title A–Z</option>
          <option value="title,desc">Title Z–A</option>
        </select>
      </div>

      {/* Error */}
      {isError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3 text-sm text-red-700">
            <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            {errorMessage}
          </div>
          <Button variant="ghost" size="sm" onClick={() => refetch()}>Retry</Button>
        </div>
      )}

      {/* Table card */}
      <Card>
        {/* Loading skeleton */}
        {isLoading && <TableSkeleton rows={5} />}

        {/* Empty state */}
        {isEmpty && (
          <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
            <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mb-4">
              <svg className="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10" />
              </svg>
            </div>
            <h3 className="text-base font-medium text-slate-900 mb-1">No products yet</h3>
            <p className="text-sm text-slate-500 mb-6 max-w-xs">
              {statusFilter ? `No products with status "${statusFilter}".` : 'Get started by creating your first product.'}
            </p>
            {!statusFilter && (
              <Link to="/products/new">
                <Button variant="primary" size="sm">Create your first product</Button>
              </Link>
            )}
          </div>
        )}

        {/* Data table */}
        {hasData && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-100">
                    <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">Product</th>
                    <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">Status</th>
                    <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide hidden sm:table-cell">Updated</th>
                    <th className="w-10 px-6 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {data?.content.map((p) => (
                    <tr
                      key={p.id}
                      onClick={() => navigate(`/products/${p.id}`)}
                      className="group cursor-pointer hover:bg-violet-50/50 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          {p.thumbnail ? (
                            <img
                              src={p.thumbnail}
                              alt={p.title}
                              loading="lazy"
                              className="w-10 h-10 rounded-lg object-cover border border-slate-100 flex-shrink-0"
                            />
                          ) : (
                            <div className="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center flex-shrink-0">
                              <svg className="w-5 h-5 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909" />
                              </svg>
                            </div>
                          )}
                          <div>
                            <p className="font-medium text-slate-900 group-hover:text-violet-700 transition-colors">{p.title}</p>
                            <p className="text-xs text-slate-400 mt-0.5">{p.handle}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <StatusBadge status={p.status} />
                      </td>
                      <td className="px-6 py-4 text-slate-500 hidden sm:table-cell">
                        {new Date(p.updated_at).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                      </td>
                      <td className="px-6 py-4">
                        <svg className="w-4 h-4 text-slate-300 group-hover:text-violet-400 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                        </svg>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {(data?.total_pages ?? 0) > 1 && (
              <div className="flex items-center justify-between px-6 py-4 border-t border-slate-100">
                <p className="text-sm text-slate-500">
                  Page {page + 1} of {data?.total_pages}
                </p>
                <div className="flex items-center gap-2">
                  <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button variant="secondary" size="sm" disabled={page >= (data?.total_pages ?? 1) - 1} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </Card>
    </div>
  );
}
