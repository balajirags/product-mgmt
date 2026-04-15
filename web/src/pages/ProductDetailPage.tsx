import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useDeleteProduct } from '@/hooks/useProducts';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { DeleteDialog } from '@/components/ui/DeleteDialog';
import { ApiError } from '@/types/product';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

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
      <div style={{ maxWidth: 800, margin: '2rem auto', padding: '0 1rem' }}>
        <p style={{ color: '#dc2626' }}>Invalid product ID.</p>
        <Link to="/products">Back to products</Link>
      </div>
    );
  }

  if (isLoading) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading…</div>;
  }

  if (isError) {
    const is404 = error instanceof ApiError && error.status === 404;
    return (
      <div style={{ maxWidth: 800, margin: '2rem auto', padding: '0 1rem' }}>
        {is404 ? (
          <>
            <p>Product not found.</p>
            <Link to="/products">Back to products</Link>
          </>
        ) : (
          <>
            <p style={{ color: '#dc2626' }}>
              {error instanceof ApiError ? error.message : 'Failed to load product.'}
            </p>
            <button onClick={() => refetch()}>Retry</button>
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
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '2rem 1rem' }}>
      {/* Breadcrumb */}
      <div style={{ marginBottom: '1rem', fontSize: '0.875rem', color: '#6b7280' }}>
        <Link to="/products">Products</Link> / {product.title}
      </div>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ margin: '0 0 0.5rem 0' }}>{product.title}</h1>
          <StatusBadge status={product.status} />
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to={`/products/${id}/edit`}>
            <button>Edit</button>
          </Link>
          <button
            onClick={() => setShowDelete(true)}
            style={{ background: '#dc2626', color: '#fff' }}
          >
            Delete
          </button>
        </div>
      </div>

      {deleteError && (
        <div style={{ padding: '0.75rem', background: '#fee2e2', borderRadius: 4, marginBottom: '1rem' }}>
          {deleteError}
        </div>
      )}

      {/* Core fields */}
      <section style={{ marginBottom: '1.5rem' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <tbody>
            {[
              ['Handle', product.handle],
              ['Subtitle', product.subtitle],
              ['Description', product.description],
              ['External ID', product.external_id],
              ['Thumbnail', product.thumbnail],
              ['Giftcard', String(product.is_giftcard)],
              ['Discountable', String(product.discountable)],
              ['Created', new Date(product.created_at).toLocaleString()],
              ['Updated', new Date(product.updated_at).toLocaleString()],
            ].map(([label, value]) =>
              value ? (
                <tr key={label} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={{ padding: '0.4rem 0.75rem 0.4rem 0', color: '#6b7280', width: 140, fontWeight: 500, fontSize: '0.875rem' }}>{label}</td>
                  <td style={{ padding: '0.4rem 0', fontSize: '0.875rem', wordBreak: 'break-word' }}>{value}</td>
                </tr>
              ) : null,
            )}
          </tbody>
        </table>
      </section>

      {/* Dimensions */}
      {(product.weight ?? product.height ?? product.width ?? product.length) !== null && (
        <section style={{ marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Dimensions</h2>
          <div style={{ display: 'flex', gap: '1.5rem', fontSize: '0.875rem' }}>
            {product.weight !== null && <span>Weight: {product.weight}</span>}
            {product.height !== null && <span>H: {product.height}</span>}
            {product.width !== null && <span>W: {product.width}</span>}
            {product.length !== null && <span>L: {product.length}</span>}
          </div>
        </section>
      )}

      {/* Metadata */}
      {product.metadata && Object.keys(product.metadata).length > 0 && (
        <section style={{ marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Metadata</h2>
          <table style={{ borderCollapse: 'collapse' }}>
            <tbody>
              {Object.entries(product.metadata).map(([k, v]) => (
                <tr key={k} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={{ padding: '0.25rem 1rem 0.25rem 0', color: '#6b7280', fontSize: '0.875rem' }}>{k}</td>
                  <td style={{ padding: '0.25rem 0', fontSize: '0.875rem' }}>{String(v)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {/* Images */}
      <section style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Images</h2>
        {product.images.length === 0 ? (
          <p style={{ color: '#9ca3af', fontSize: '0.875rem' }}>No images.</p>
        ) : (
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {product.images
              .slice()
              .sort((a, b) => a.rank - b.rank)
              .map((img) => (
                <img
                  key={img.rank}
                  src={img.url}
                  alt={`Product image ${img.rank}`}
                  width={80}
                  height={80}
                  style={{ objectFit: 'cover', borderRadius: 4, border: '1px solid #e5e7eb' }}
                />
              ))}
          </div>
        )}
      </section>

      {/* Options */}
      <section style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Options</h2>
        {product.options.length === 0 ? (
          <p style={{ color: '#9ca3af', fontSize: '0.875rem' }}>None.</p>
        ) : (
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            {product.options.map((opt) => (
              <div key={opt.id} style={{ background: '#f9fafb', padding: '0.5rem 0.75rem', borderRadius: 6, fontSize: '0.875rem' }}>
                <strong>{opt.title}</strong>: {opt.values.join(', ')}
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Variants */}
      <section>
        <h2 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Variants</h2>
        {product.variants.length === 0 ? (
          <p style={{ color: '#9ca3af', fontSize: '0.875rem' }}>None.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #e5e7eb', textAlign: 'left' }}>
                <th style={{ padding: '0.4rem' }}>Title</th>
                <th style={{ padding: '0.4rem' }}>SKU</th>
                <th style={{ padding: '0.4rem' }}>Barcode</th>
                <th style={{ padding: '0.4rem' }}>Track inv.</th>
                <th style={{ padding: '0.4rem' }}>Backorder</th>
              </tr>
            </thead>
            <tbody>
              {product.variants.map((v) => (
                <tr key={v.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={{ padding: '0.4rem' }}>{v.title}</td>
                  <td style={{ padding: '0.4rem', color: '#6b7280' }}>{v.sku ?? '—'}</td>
                  <td style={{ padding: '0.4rem', color: '#6b7280' }}>{v.barcode ?? '—'}</td>
                  <td style={{ padding: '0.4rem' }}>{v.manage_inventory ? 'Yes' : 'No'}</td>
                  <td style={{ padding: '0.4rem' }}>{v.allow_backorder ? 'Yes' : 'No'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* Delete dialog */}
      {showDelete && (
        <DeleteDialog
          productTitle={product.title}
          onConfirm={handleDelete}
          onCancel={() => setShowDelete(false)}
          isDeleting={deleteMutation.isPending}
        />
      )}
    </div>
  );
}
