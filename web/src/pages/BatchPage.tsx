import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useBatchProducts } from '@/hooks/useProducts';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import type { BatchItemResult } from '@/types/product';
import { ApiError } from '@/types/product';

const MAX_ITEMS = 100;
type Tab = 'creates' | 'updates' | 'deletes';

interface CreateRow { title: string }
interface UpdateRow { id: string; title: string }
interface DeleteRow { id: string }
interface Results { created: BatchItemResult[]; updated: BatchItemResult[]; deleted: BatchItemResult[] }

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
    if (!hasAny) errs.general = 'Add at least one operation before submitting.';
    creates.forEach((r, i) => {
      if (r.title !== '' && r.title.trim() === '') errs[`create_title_${i}`] = 'Title is required';
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

  const TABS: { key: Tab; label: string }[] = [
    { key: 'creates', label: 'Creates' },
    { key: 'updates', label: 'Updates' },
    { key: 'deletes', label: 'Deletes' },
  ];

  return (
    <div className="max-w-3xl space-y-6">
      {/* Header */}
      <div>
        <nav className="flex items-center gap-2 text-sm text-slate-500 mb-4">
          <Link to="/products" className="hover:text-violet-600 transition-colors">Products</Link>
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
          </svg>
          <span className="text-slate-900">Batch operations</span>
        </nav>
        <h1 className="text-2xl font-semibold text-slate-900">Batch operations</h1>
        <p className="mt-1 text-sm text-slate-500">Create, update, or delete up to {MAX_ITEMS} products in one request.</p>
      </div>

      {/* Error/feedback */}
      {errors.general && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">{errors.general}</div>
      )}
      {serverError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{serverError}</div>
      )}

      <Card>
        {/* Tab bar */}
        <div className="flex border-b border-slate-100">
          {TABS.map(({ key, label }) => (
            <button
              key={key}
              onClick={() => setTab(key)}
              type="button"
              className={`px-5 py-3 text-sm font-medium transition-colors cursor-pointer focus-visible:outline-none ${
                tab === key
                  ? 'text-violet-700 border-b-2 border-violet-600 -mb-px'
                  : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="p-6 space-y-3">
          {/* Creates */}
          {tab === 'creates' && (
            <>
              {creates.map((row, i) => (
                <div key={i} className="flex gap-2">
                  <div className="flex-1">
                    <Input
                      placeholder="Product title *"
                      value={row.title}
                      onChange={(e) => { const rows = [...creates]; rows[i] = { title: e.target.value }; setCreates(rows); }}
                    />
                    {errors[`create_title_${i}`] && <p className="text-xs text-red-600 mt-1">{errors[`create_title_${i}`]}</p>}
                  </div>
                  {results?.created[i] && <ResultPill result={results.created[i]} />}
                  {creates.length > 1 && (
                    <Button aria-label="Remove" type="button" variant="ghost" size="sm" onClick={() => setCreates(creates.filter((_, j) => j !== i))}>
                      <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </Button>
                  )}
                </div>
              ))}
              {creates.length < MAX_ITEMS ? (
                <Button type="button" variant="ghost" size="sm" onClick={() => setCreates([...creates, { title: '' }])}>+ Add row</Button>
              ) : (
                <p className="text-xs text-amber-600">Maximum of {MAX_ITEMS} items reached.</p>
              )}
            </>
          )}

          {/* Updates */}
          {tab === 'updates' && (
            <>
              {updates.map((row, i) => (
                <div key={i} className="flex gap-2">
                  <Input className="flex-1" placeholder="Product UUID" value={row.id}
                    onChange={(e) => { const rows = [...updates]; rows[i] = { ...rows[i], id: e.target.value }; setUpdates(rows); }} />
                  <Input className="flex-1" placeholder="New title" value={row.title}
                    onChange={(e) => { const rows = [...updates]; rows[i] = { ...rows[i], title: e.target.value }; setUpdates(rows); }} />
                  {results?.updated[i] && <ResultPill result={results.updated[i]} />}
                  {updates.length > 1 && (
                    <Button aria-label="Remove" type="button" variant="ghost" size="sm" onClick={() => setUpdates(updates.filter((_, j) => j !== i))}>
                      <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </Button>
                  )}
                </div>
              ))}
              {updates.length < MAX_ITEMS && (
                <Button type="button" variant="ghost" size="sm" onClick={() => setUpdates([...updates, { id: '', title: '' }])}>+ Add row</Button>
              )}
            </>
          )}

          {/* Deletes */}
          {tab === 'deletes' && (
            <>
              {deletes.map((row, i) => (
                <div key={i} className="flex gap-2">
                  <Input className="flex-1" placeholder="Product UUID" value={row.id}
                    onChange={(e) => { const rows = [...deletes]; rows[i] = { id: e.target.value }; setDeletes(rows); }} />
                  {results?.deleted[i] && <ResultPill result={results.deleted[i]} />}
                  {deletes.length > 1 && (
                    <Button aria-label="Remove" type="button" variant="ghost" size="sm" onClick={() => setDeletes(deletes.filter((_, j) => j !== i))}>
                      <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </Button>
                  )}
                </div>
              ))}
              {deletes.length < MAX_ITEMS && (
                <Button type="button" variant="ghost" size="sm" onClick={() => setDeletes([...deletes, { id: '' }])}>+ Add row</Button>
              )}
            </>
          )}
        </div>

        <div className="px-6 py-4 border-t border-slate-100 flex items-center justify-between gap-4">
          <Link to="/products"><Button type="button" variant="ghost" size="sm">Cancel</Button></Link>
          <Button variant="primary" onClick={() => void handleSubmit()} loading={batchMutation.isPending}>
            Run batch
          </Button>
        </div>
      </Card>

      {/* Results summary */}
      {results && !batchMutation.isPending && (
        <Card className="p-6">
          <h3 className="text-sm font-semibold text-slate-900 mb-4">Results</h3>
          <div className="space-y-2">
            {[
              { label: 'Created', items: results.created },
              { label: 'Updated', items: results.updated },
              { label: 'Deleted', items: results.deleted },
            ].filter(({ items }) => items.length > 0).map(({ label, items }) => {
              const ok = items.filter((r) => r.success).length;
              const fail = items.filter((r) => !r.success).length;
              return (
                <div key={label} className="flex items-center justify-between text-sm py-2 border-b border-slate-50 last:border-0">
                  <span className="font-medium text-slate-700">{label}</span>
                  <div className="flex items-center gap-3">
                    {ok > 0 && <span className="text-emerald-600 font-medium">{ok} succeeded</span>}
                    {fail > 0 && <span className="text-red-600 font-medium">{fail} failed</span>}
                  </div>
                </div>
              );
            })}
          </div>
        </Card>
      )}
    </div>
  );
}

function ResultPill({ result }: { result: BatchItemResult }) {
  return result.success ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium bg-emerald-50 text-emerald-700 rounded-lg flex-shrink-0">
      <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
      </svg>
      OK
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium bg-red-50 text-red-700 rounded-lg flex-shrink-0" title={result.errorMessage ?? ''}>
      <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
      </svg>
      {result.errorCode ?? 'Error'}
    </span>
  );
}
