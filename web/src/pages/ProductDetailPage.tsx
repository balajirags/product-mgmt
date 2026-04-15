import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useDeleteProduct } from '@/hooks/useProducts';
import { StatusBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Dialog } from '@/components/ui/Dialog';
import { Skeleton } from '@/components/ui/Skeleton';
import { ApiError } from '@/types/product';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  if (!value && value !== 0) return null;
  return (
    <div className="flex items-start py-3 border-b border-slate-50 last:border-0">
      <dt className="w-36 flex-shrink-0 text-sm text-slate-500">{label}</dt>
      <dd className="text-sm text-slate-900 break-words min-w-0">{value}</dd>
    </div>
  );
}

export function ProductDetailPage() {
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [showDelete, setShowDelete] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const isValidUuid = UUID_REGEX.test(id);
  const { data: product, isLoading, isError, error, refetch } = useProduct(isValidUuid ? id : '');
  const deleteMutation = useDeleteProduct();

  if (!isValidUuid) {
    return (
      <div className="text-center py-16">
        <p className="text-slate-600 mb-4">Invalid product ID.</p>
        <Link to="/products"><Button variant="secondary" size="sm">Back to products</Button></Link>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-5 w-20 rounded-full" />
        </div>
        <Card className="p-6 space-y-3">
          {[80, 64, 72, 56].map((w) => <Skeleton key={w} className={`h-4 w-${w}`} />)}
        </Card>
      </div>
    );
  }

  if (isError) {
    const is404 = error instanceof ApiError && error.status === 404;
    return (
      <div className="text-center py-16 space-y-4">
        {is404 ? (
          <>
            <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto">
              <svg className="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10" />
              </svg>
            </div>
            <p className="text-base font-medium text-slate-900">Product not found</p>
            <Link to="/products"><Button variant="secondary" size="sm">Back to products</Button></Link>
          </>
        ) : (
          <>
            <p className="text-sm text-red-600">{error instanceof ApiError ? error.message : 'Failed to load product.'}</p>
            <Button variant="secondary" size="sm" onClick={() => refetch()}>Retry</Button>
          </>
        )}
      </div>
    );
  }

  if (!product) return null;

  async function handleDelete() {
    setDeleteError('');
    try {
      await deleteMutation.mutateAsync(id);
      navigate('/products');
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : 'Delete failed.');
    }
  }

  return (
    <div className="space-y-6">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-slate-500">
        <Link to="/products" className="hover:text-violet-600 transition-colors">Products</Link>
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
        </svg>
        <span className="text-slate-900 truncate max-w-xs">{product.title}</span>
      </nav>

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div className="flex items-center gap-3 min-w-0">
          {product.thumbnail && (
            <img src={product.thumbnail} alt={product.title} className="w-12 h-12 rounded-xl object-cover border border-slate-200 flex-shrink-0" />
          )}
          <div className="min-w-0">
            <h1 className="text-2xl font-semibold text-slate-900 truncate">{product.title}</h1>
            <p className="text-sm text-slate-500 mt-0.5">{product.handle}</p>
          </div>
          <StatusBadge status={product.status} />
        </div>
        <div className="flex items-center gap-2 flex-shrink-0">
          <Link to={`/products/${id}/edit`}>
            <Button variant="secondary" size="sm">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931z" />
              </svg>
              Edit
            </Button>
          </Link>
          <Button variant="danger" size="sm" onClick={() => setShowDelete(true)}>
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916" />
            </svg>
            Delete
          </Button>
        </div>
      </div>

      {deleteError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{deleteError}</div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Core fields */}
          <Card className="p-6">
            <h2 className="text-sm font-semibold text-slate-900 mb-4">Details</h2>
            <dl>
              <DetailRow label="Description" value={product.description} />
              <DetailRow label="Subtitle" value={product.subtitle} />
              <DetailRow label="External ID" value={product.external_id} />
              <DetailRow label="Created" value={new Date(product.created_at).toLocaleString()} />
              <DetailRow label="Updated" value={new Date(product.updated_at).toLocaleString()} />
            </dl>
          </Card>

          {/* Images */}
          <Card className="p-6">
            <h2 className="text-sm font-semibold text-slate-900 mb-4">Images</h2>
            {product.images.length === 0 ? (
              <p className="text-sm text-slate-400">No images.</p>
            ) : (
              <div className="flex flex-wrap gap-3">
                {[...product.images].sort((a, b) => a.rank - b.rank).map((img) => (
                  <img
                    key={img.rank}
                    src={img.url}
                    alt={`Product image ${img.rank + 1}`}
                    loading="lazy"
                    className="w-20 h-20 rounded-xl object-cover border border-slate-100"
                  />
                ))}
              </div>
            )}
          </Card>

          {/* Variants */}
          {product.variants.length > 0 && (
            <Card>
              <div className="px-6 py-4 border-b border-slate-100">
                <h2 className="text-sm font-semibold text-slate-900">Variants <span className="ml-1.5 text-xs font-medium text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded-full">{product.variants.length}</span></h2>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-slate-100">
                      <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">Title</th>
                      <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">SKU</th>
                      <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">Barcode</th>
                      <th className="text-left px-6 py-3 font-medium text-slate-500 text-xs uppercase tracking-wide">Inventory</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-50">
                    {product.variants.map((v) => (
                      <tr key={v.id} className="hover:bg-slate-50/50">
                        <td className="px-6 py-3 font-medium text-slate-900">{v.title}</td>
                        <td className="px-6 py-3 font-mono text-xs text-slate-500">{v.sku ?? <span className="text-slate-300">—</span>}</td>
                        <td className="px-6 py-3 font-mono text-xs text-slate-500">{v.barcode ?? <span className="text-slate-300">—</span>}</td>
                        <td className="px-6 py-3">
                          <span className={`inline-flex items-center gap-1 text-xs font-medium ${v.manage_inventory ? 'text-emerald-600' : 'text-slate-400'}`}>
                            <span className={`w-1.5 h-1.5 rounded-full ${v.manage_inventory ? 'bg-emerald-500' : 'bg-slate-300'}`} />
                            {v.manage_inventory ? 'Tracked' : 'Untracked'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Options */}
          <Card className="p-6">
            <h2 className="text-sm font-semibold text-slate-900 mb-4">Options</h2>
            {product.options.length === 0 ? (
              <p className="text-sm text-slate-400">None.</p>
            ) : (
              <div className="space-y-3">
                {product.options.map((opt) => (
                  <div key={opt.id}>
                    <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1.5">{opt.title}</p>
                    <div className="flex flex-wrap gap-1.5">
                      {opt.values.map((v) => (
                        <span key={v} className="px-2 py-1 text-xs bg-slate-100 text-slate-700 rounded-md">{v}</span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>

          {/* Dimensions */}
          {(product.weight ?? product.height ?? product.width ?? product.length) !== null && (
            <Card className="p-6">
              <h2 className="text-sm font-semibold text-slate-900 mb-4">Dimensions</h2>
              <dl className="space-y-2">
                {[['Weight', product.weight], ['Height', product.height], ['Width', product.width], ['Length', product.length]].map(([label, val]) =>
                  val !== null ? (
                    <div key={label as string} className="flex justify-between text-sm">
                      <dt className="text-slate-500">{label}</dt>
                      <dd className="font-medium text-slate-900">{val}</dd>
                    </div>
                  ) : null
                )}
              </dl>
            </Card>
          )}

          {/* Metadata */}
          {product.metadata && Object.keys(product.metadata).length > 0 && (
            <Card className="p-6">
              <h2 className="text-sm font-semibold text-slate-900 mb-4">Metadata</h2>
              <dl className="space-y-2">
                {Object.entries(product.metadata).map(([k, v]) => (
                  <div key={k} className="flex justify-between text-sm gap-2">
                    <dt className="text-slate-500 truncate">{k}</dt>
                    <dd className="font-medium text-slate-900 text-right">{String(v)}</dd>
                  </div>
                ))}
              </dl>
            </Card>
          )}
        </div>
      </div>

      {/* Delete dialog */}
      {showDelete && (
        <Dialog
          title="Delete product?"
          description={
            <>
              <strong>{product.title}</strong> and all associated images, options, and variants will be permanently removed. This cannot be undone.
            </>
          }
          confirmLabel="Delete"
          onConfirm={handleDelete}
          onCancel={() => setShowDelete(false)}
          loading={deleteMutation.isPending}
        />
      )}
    </div>
  );
}
