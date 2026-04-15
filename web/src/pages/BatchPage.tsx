import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useBatchProducts } from '@/hooks/useProducts';
import type { BatchItemResult } from '@/types/product';
import { ApiError } from '@/types/product';

const MAX_ITEMS = 100;

type Tab = 'creates' | 'updates' | 'deletes';

interface CreateRow { title: string }
interface UpdateRow { id: string; title: string }
interface DeleteRow { id: string }

interface Results {
  created: BatchItemResult[];
  updated: BatchItemResult[];
  deleted: BatchItemResult[];
}

export function BatchPage() {
  const [tab, setTab] = useState<Tab>('creates');
  const [creates, setCreates] = useState<CreateRow[]>([{ title: '' }]);
  const [updates, setUpdates] = useState<UpdateRow[]>([{ id: '', title: '' }]);
  const [deletes, setDeletes] = useState<DeleteRow[]>([{ id: '' }]);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState('');
  const [results, setResults] = useState<Results | null>(null);

  const batchMutation = useBatchProducts();

  function validate(): boolean {
    const errs: Record<string, string> = {};
    const hasAny =
      creates.some((r) => r.title.trim()) ||
      updates.some((r) => r.id.trim() || r.title.trim()) ||
      deletes.some((r) => r.id.trim());
    if (!hasAny) {
      errs.general = 'Add at least one operation before submitting.';
    }
    creates.forEach((r, i) => {
      if (r.title === '' && creates.some((c) => c.title.trim())) {
        // skip intentionally blank rows only if they're not in use
      }
      if (creates.filter((c) => c.title.trim()).length > 0 && !r.title.trim() && creates.length > 1) {
        // blank rows among non-blank rows
      }
      if (r.title.trim() === '' && creates.length === 1) {
        // allow single blank row in creates (user hasn't filled anything)
      } else if (r.title !== '' && r.title.trim() === '') {
        errs[`create_title_${i}`] = 'Title is required';
      }
    });
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit() {
    setServerError('');
    setResults(null);
    if (!validate()) return;

    const createItems = creates.filter((r) => r.title.trim()).map((r) => ({ title: r.title.trim() }));
    const updateItems = updates.filter((r) => r.id.trim() && r.title.trim()).map((r) => ({ id: r.id.trim(), data: { title: r.title.trim() } }));
    const deleteIds = deletes.filter((r) => r.id.trim()).map((r) => r.id.trim());

    try {
      const res = await batchMutation.mutateAsync({
        create: createItems.length > 0 ? createItems : undefined,
        update: updateItems.length > 0 ? updateItems : undefined,
        delete: deleteIds.length > 0 ? deleteIds : undefined,
      });
      setResults(res);
    } catch (err) {
      setServerError(err instanceof ApiError ? err.message : 'Batch operation failed.');
    }
  }

  const isSubmitting = batchMutation.isPending;

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '2rem 1rem' }}>
      <div style={{ marginBottom: '1rem', fontSize: '0.875rem', color: '#6b7280' }}>
        <Link to="/products">Products</Link> / Batch operations
      </div>
      <h1 style={{ marginBottom: '1.5rem' }}>Batch operations</h1>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '2px solid #e5e7eb', marginBottom: '1.5rem' }}>
        {(['creates', 'updates', 'deletes'] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: '0.5rem 1.25rem', background: 'none', border: 'none', cursor: 'pointer',
              borderBottom: tab === t ? '2px solid #2563eb' : '2px solid transparent',
              marginBottom: -2, fontWeight: tab === t ? 600 : 400, color: tab === t ? '#2563eb' : '#374151',
            }}
          >
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {errors.general && (
        <div style={{ padding: '0.75rem', background: '#fee2e2', borderRadius: 4, marginBottom: '1rem' }}>
          {errors.general}
        </div>
      )}
      {serverError && (
        <div style={{ padding: '0.75rem', background: '#fee2e2', borderRadius: 4, marginBottom: '1rem' }}>
          {serverError}
        </div>
      )}

      {/* Creates tab */}
      {tab === 'creates' && (
        <div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
            Max {MAX_ITEMS} products per batch.
          </p>
          {creates.map((row, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'center' }}>
              <input
                placeholder="Product title *"
                value={row.title}
                onChange={(e) => {
                  const rows = [...creates];
                  rows[i] = { title: e.target.value };
                  setCreates(rows);
                }}
                style={inputStyle}
              />
              {creates.length > 1 && (
                <button type="button" onClick={() => setCreates(creates.filter((_, j) => j !== i))}>Remove</button>
              )}
              {results?.created[i] && <ResultBadge result={results.created[i]} />}
            </div>
          ))}
          {creates.length < MAX_ITEMS && (
            <button type="button" onClick={() => setCreates([...creates, { title: '' }])}>+ Add row</button>
          )}
          {creates.length >= MAX_ITEMS && (
            <p style={{ color: '#f59e0b', fontSize: '0.875rem' }}>Maximum of {MAX_ITEMS} items reached.</p>
          )}
        </div>
      )}

      {/* Updates tab */}
      {tab === 'updates' && (
        <div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
            Enter the product UUID and new title. Max {MAX_ITEMS} per batch.
          </p>
          {updates.map((row, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'center' }}>
              <input
                placeholder="Product UUID"
                value={row.id}
                onChange={(e) => { const rows = [...updates]; rows[i] = { ...rows[i], id: e.target.value }; setUpdates(rows); }}
                style={{ ...inputStyle, flex: 1 }}
              />
              <input
                placeholder="New title"
                value={row.title}
                onChange={(e) => { const rows = [...updates]; rows[i] = { ...rows[i], title: e.target.value }; setUpdates(rows); }}
                style={{ ...inputStyle, flex: 1 }}
              />
              {updates.length > 1 && (
                <button type="button" onClick={() => setUpdates(updates.filter((_, j) => j !== i))}>Remove</button>
              )}
              {results?.updated[i] && <ResultBadge result={results.updated[i]} />}
            </div>
          ))}
          {updates.length < MAX_ITEMS && (
            <button type="button" onClick={() => setUpdates([...updates, { id: '', title: '' }])}>+ Add row</button>
          )}
        </div>
      )}

      {/* Deletes tab */}
      {tab === 'deletes' && (
        <div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
            Enter product UUIDs to soft-delete. Max {MAX_ITEMS} per batch.
          </p>
          {deletes.map((row, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'center' }}>
              <input
                placeholder="Product UUID"
                value={row.id}
                onChange={(e) => { const rows = [...deletes]; rows[i] = { id: e.target.value }; setDeletes(rows); }}
                style={inputStyle}
              />
              {deletes.length > 1 && (
                <button type="button" onClick={() => setDeletes(deletes.filter((_, j) => j !== i))}>Remove</button>
              )}
              {results?.deleted[i] && <ResultBadge result={results.deleted[i]} />}
            </div>
          ))}
          {deletes.length < MAX_ITEMS && (
            <button type="button" onClick={() => setDeletes([...deletes, { id: '' }])}>+ Add row</button>
          )}
        </div>
      )}

      {/* Submit */}
      <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.75rem' }}>
        <button
          onClick={() => void handleSubmit()}
          disabled={isSubmitting}
          style={{ fontWeight: 600 }}
        >
          {isSubmitting ? 'Running batch…' : 'Run batch'}
        </button>
        <Link to="/products">
          <button type="button">Cancel</button>
        </Link>
      </div>

      {/* Summary results */}
      {results && !isSubmitting && (
        <div style={{ marginTop: '1.5rem', padding: '1rem', background: '#f9fafb', borderRadius: 6 }}>
          <h2 style={{ fontSize: '1rem', marginTop: 0 }}>Results</h2>
          {[
            { label: 'Created', items: results.created },
            { label: 'Updated', items: results.updated },
            { label: 'Deleted', items: results.deleted },
          ].map(({ label, items }) =>
            items.length > 0 ? (
              <div key={label} style={{ marginBottom: '0.75rem' }}>
                <strong>{label}:</strong>{' '}
                {items.filter((r) => r.success).length} succeeded,{' '}
                {items.filter((r) => !r.success).length} failed
              </div>
            ) : null,
          )}
        </div>
      )}
    </div>
  );
}

function ResultBadge({ result }: { result: BatchItemResult }) {
  if (result.success) {
    return <span style={{ color: '#065f46', fontSize: '0.75rem', whiteSpace: 'nowrap' }}>✓ OK</span>;
  }
  return (
    <span style={{ color: '#991b1b', fontSize: '0.75rem', whiteSpace: 'nowrap' }} title={result.errorMessage ?? ''}>
      ✗ {result.errorCode ?? 'Error'}
    </span>
  );
}

const inputStyle: React.CSSProperties = {
  flex: 1, padding: '0.4rem 0.6rem', border: '1px solid #d1d5db',
  borderRadius: 4, fontSize: '0.875rem', boxSizing: 'border-box',
};
