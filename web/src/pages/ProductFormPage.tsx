import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useCreateProduct, useUpdateProduct } from '@/hooks/useProducts';
import { PRODUCT_STATUSES } from '@/types/product';
import type { ProductStatus, ProductImageRequest, ProductOptionRequest, ProductVariantRequest } from '@/types/product';
import { ApiError } from '@/types/product';

interface Props {
  mode: 'create' | 'edit';
}

interface OptionRow {
  title: string;
  values: string; // comma-separated
}

interface VariantRow {
  title: string;
  sku: string;
  barcode: string;
  manageInventory: boolean;
  allowBackorder: boolean;
}

interface FormState {
  title: string;
  handle: string;
  status: ProductStatus;
  description: string;
  subtitle: string;
  weight: string;
  height: string;
  width: string;
  length: string;
  thumbnail: string;
  externalId: string;
  metadata: string; // JSON string
  images: string[]; // URLs
  options: OptionRow[];
  variants: VariantRow[];
}

const EMPTY_FORM: FormState = {
  title: '', handle: '', status: 'DRAFT', description: '', subtitle: '',
  weight: '', height: '', width: '', length: '', thumbnail: '', externalId: '',
  metadata: '', images: [''], options: [], variants: [],
};

function parseDecimal(s: string): number | null {
  const n = parseFloat(s);
  return isNaN(n) ? null : n;
}

export function ProductFormPage({ mode }: Props) {
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState('');

  const isEdit = mode === 'edit';
  const { data: existing, isLoading: loadingProduct } = useProduct(isEdit ? id : '');
  const createMutation = useCreateProduct();
  const updateMutation = useUpdateProduct(id);

  // Pre-populate form from existing product
  useEffect(() => {
    if (!existing) return;
    setForm({
      title: existing.title,
      handle: existing.handle ?? '',
      status: existing.status,
      description: existing.description ?? '',
      subtitle: existing.subtitle ?? '',
      weight: existing.weight !== null ? String(existing.weight) : '',
      height: existing.height !== null ? String(existing.height) : '',
      width: existing.width !== null ? String(existing.width) : '',
      length: existing.length !== null ? String(existing.length) : '',
      thumbnail: existing.thumbnail ?? '',
      externalId: existing.externalId ?? '',
      metadata: existing.metadata ? JSON.stringify(existing.metadata, null, 2) : '',
      images: existing.images.length > 0
        ? [...existing.images].sort((a, b) => a.rank - b.rank).map((i) => i.url)
        : [''],
      options: existing.options.map((o) => ({ title: o.title, values: o.values.join(', ') })),
      variants: existing.variants.map((v) => ({
        title: v.title, sku: v.sku ?? '', barcode: v.barcode ?? '',
        manageInventory: v.manageInventory, allowBackorder: v.allowBackorder,
      })),
    });
  }, [existing]);

  function set(field: keyof FormState, value: unknown) {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => { const next = { ...prev }; delete next[field]; return next; });
  }

  function validate(): boolean {
    const errs: Record<string, string> = {};
    if (!form.title.trim()) errs.title = 'Title is required';
    form.images.forEach((url, i) => {
      if (url.trim() === '') errs[`image_${i}`] = 'Image URL is required';
    });
    form.options.forEach((opt, i) => {
      if (!opt.title.trim()) errs[`option_title_${i}`] = 'Option name is required';
      if (!opt.values.trim()) errs[`option_values_${i}`] = 'At least one value required';
    });
    if (form.metadata.trim()) {
      try { JSON.parse(form.metadata); } catch { errs.metadata = 'Invalid JSON'; }
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    const images: ProductImageRequest[] = form.images
      .filter((u) => u.trim())
      .map((url) => ({ url }));

    const options: ProductOptionRequest[] = form.options
      .filter((o) => o.title.trim())
      .map((o) => ({ title: o.title, values: o.values.split(',').map((v) => v.trim()).filter(Boolean) }));

    const variants: ProductVariantRequest[] = form.variants.map((v) => ({
      title: v.title || null,
      sku: v.sku || null,
      barcode: v.barcode || null,
      manageInventory: v.manageInventory,
      allowBackorder: v.allowBackorder,
    }));

    let metadata: Record<string, unknown> | null = null;
    if (form.metadata.trim()) {
      try { metadata = JSON.parse(form.metadata) as Record<string, unknown>; } catch { /* validated above */ }
    }

    const body = {
      title: form.title.trim(),
      handle: form.handle.trim() || null,
      status: form.status,
      description: form.description.trim() || null,
      subtitle: form.subtitle.trim() || null,
      weight: parseDecimal(form.weight),
      height: parseDecimal(form.height),
      width: parseDecimal(form.width),
      length: parseDecimal(form.length),
      thumbnail: form.thumbnail.trim() || null,
      externalId: form.externalId.trim() || null,
      metadata,
      images: images.length > 0 ? images : null,
      options: options.length > 0 ? options : null,
      variants: variants.length > 0 ? variants : null,
    };

    try {
      let saved;
      if (isEdit) {
        saved = await updateMutation.mutateAsync(body);
      } else {
        saved = await createMutation.mutateAsync(body);
      }
      navigate(`/products/${saved.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message);
        if (err.problem.field_errors) {
          setErrors((prev) => ({ ...prev, ...err.problem.field_errors }));
        }
      } else {
        setServerError('An unexpected error occurred.');
      }
    }
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  if (isEdit && loadingProduct) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading…</div>;
  }

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: '2rem 1rem' }}>
      <div style={{ marginBottom: '1rem', fontSize: '0.875rem', color: '#6b7280' }}>
        <Link to="/products">Products</Link>
        {isEdit && existing && <> / <Link to={`/products/${id}`}>{existing.title}</Link></>}
        {' '} / {isEdit ? 'Edit' : 'New product'}
      </div>

      <h1 style={{ marginBottom: '1.5rem' }}>{isEdit ? 'Edit product' : 'Create product'}</h1>

      {serverError && (
        <div style={{ padding: '0.75rem', background: '#fee2e2', borderRadius: 4, marginBottom: '1rem' }}>
          {serverError}
        </div>
      )}

      <form onSubmit={(e) => { void handleSubmit(e); }} noValidate>
        <Field label="Title *" error={errors.title}>
          <input value={form.title} onChange={(e) => set('title', e.target.value)} style={inputStyle} />
        </Field>

        <Field label="Handle" hint="Auto-generated from title if left blank">
          <input value={form.handle} onChange={(e) => set('handle', e.target.value)} style={inputStyle} />
        </Field>

        <Field label="Status">
          <select value={form.status} onChange={(e) => set('status', e.target.value as ProductStatus)} style={inputStyle}>
            {PRODUCT_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </Field>

        <Field label="Description">
          <textarea value={form.description} onChange={(e) => set('description', e.target.value)} rows={3} style={inputStyle} />
        </Field>

        <Field label="Subtitle">
          <input value={form.subtitle} onChange={(e) => set('subtitle', e.target.value)} style={inputStyle} />
        </Field>

        <Field label="Thumbnail URL">
          <input value={form.thumbnail} onChange={(e) => set('thumbnail', e.target.value)} style={inputStyle} />
        </Field>

        <Field label="External ID">
          <input value={form.externalId} onChange={(e) => set('externalId', e.target.value)} style={inputStyle} />
        </Field>

        {/* Dimensions */}
        <fieldset style={{ border: '1px solid #e5e7eb', borderRadius: 4, padding: '1rem', marginBottom: '1rem' }}>
          <legend style={{ fontSize: '0.875rem', fontWeight: 600, padding: '0 4px' }}>Dimensions</legend>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            {(['weight', 'height', 'width', 'length'] as const).map((dim) => (
              <Field key={dim} label={dim.charAt(0).toUpperCase() + dim.slice(1)}>
                <input
                  type="number" step="any" value={form[dim]}
                  onChange={(e) => set(dim, e.target.value)}
                  style={inputStyle}
                />
              </Field>
            ))}
          </div>
        </fieldset>

        {/* Metadata */}
        <Field label="Metadata (JSON)" error={errors.metadata}>
          <textarea
            value={form.metadata}
            onChange={(e) => set('metadata', e.target.value)}
            rows={3}
            placeholder='{"brand": "Acme"}'
            style={{ ...inputStyle, fontFamily: 'monospace', fontSize: '0.8rem' }}
          />
        </Field>

        {/* Images */}
        <fieldset style={{ border: '1px solid #e5e7eb', borderRadius: 4, padding: '1rem', marginBottom: '1rem' }}>
          <legend style={{ fontSize: '0.875rem', fontWeight: 600, padding: '0 4px' }}>Images</legend>
          {form.images.map((url, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'center' }}>
              <input
                value={url}
                onChange={(e) => {
                  const imgs = [...form.images];
                  imgs[i] = e.target.value;
                  set('images', imgs);
                }}
                placeholder="https://cdn.example.com/image.jpg"
                style={{ ...inputStyle, flex: 1 }}
              />
              {form.images.length > 1 && (
                <button type="button" onClick={() => set('images', form.images.filter((_, j) => j !== i))}>
                  Remove
                </button>
              )}
              {errors[`image_${i}`] && <span style={{ color: '#dc2626', fontSize: '0.75rem' }}>{errors[`image_${i}`]}</span>}
            </div>
          ))}
          <button type="button" onClick={() => set('images', [...form.images, ''])}>+ Add image</button>
        </fieldset>

        {/* Options */}
        <fieldset style={{ border: '1px solid #e5e7eb', borderRadius: 4, padding: '1rem', marginBottom: '1rem' }}>
          <legend style={{ fontSize: '0.875rem', fontWeight: 600, padding: '0 4px' }}>Options</legend>
          {form.options.map((opt, i) => (
            <div key={i} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <input
                  placeholder="Name (e.g. Size)"
                  value={opt.title}
                  onChange={(e) => {
                    const opts = [...form.options];
                    opts[i] = { ...opts[i], title: e.target.value };
                    set('options', opts);
                  }}
                  style={inputStyle}
                />
                {errors[`option_title_${i}`] && <span style={{ color: '#dc2626', fontSize: '0.75rem' }}>{errors[`option_title_${i}`]}</span>}
              </div>
              <div style={{ flex: 1 }}>
                <input
                  placeholder="Values (comma-separated)"
                  value={opt.values}
                  onChange={(e) => {
                    const opts = [...form.options];
                    opts[i] = { ...opts[i], values: e.target.value };
                    set('options', opts);
                  }}
                  style={inputStyle}
                />
                {errors[`option_values_${i}`] && <span style={{ color: '#dc2626', fontSize: '0.75rem' }}>{errors[`option_values_${i}`]}</span>}
              </div>
              <button type="button" onClick={() => set('options', form.options.filter((_, j) => j !== i))}>Remove</button>
            </div>
          ))}
          <button type="button" onClick={() => set('options', [...form.options, { title: '', values: '' }])}>+ Add option</button>
        </fieldset>

        {/* Variants */}
        <fieldset style={{ border: '1px solid #e5e7eb', borderRadius: 4, padding: '1rem', marginBottom: '1.5rem' }}>
          <legend style={{ fontSize: '0.875rem', fontWeight: 600, padding: '0 4px' }}>Variants</legend>
          {form.variants.map((v, i) => (
            <div key={i} style={{ borderBottom: '1px solid #f3f4f6', paddingBottom: '0.75rem', marginBottom: '0.75rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <input placeholder="Title (auto-generated)" value={v.title} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], title: e.target.value }; set('variants', vs); }} style={inputStyle} />
                <input placeholder="SKU" value={v.sku} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], sku: e.target.value }; set('variants', vs); }} style={inputStyle} />
                <input placeholder="Barcode" value={v.barcode} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], barcode: e.target.value }; set('variants', vs); }} style={inputStyle} />
              </div>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', fontSize: '0.875rem' }}>
                <label>
                  <input type="checkbox" checked={v.manageInventory} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], manageInventory: e.target.checked }; set('variants', vs); }} />
                  {' '}Track inventory
                </label>
                <label>
                  <input type="checkbox" checked={v.allowBackorder} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], allowBackorder: e.target.checked }; set('variants', vs); }} />
                  {' '}Allow backorder
                </label>
                <button type="button" onClick={() => set('variants', form.variants.filter((_, j) => j !== i))}>Remove</button>
              </div>
            </div>
          ))}
          <button type="button" onClick={() => set('variants', [...form.variants, { title: '', sku: '', barcode: '', manageInventory: false, allowBackorder: false }])}>+ Add variant</button>
        </fieldset>

        {/* Actions */}
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button type="submit" disabled={isSubmitting} style={{ fontWeight: 600 }}>
            {isSubmitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create product'}
          </button>
          <Link to={isEdit ? `/products/${id}` : '/products'}>
            <button type="button">Cancel</button>
          </Link>
        </div>
      </form>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────

const inputStyle: React.CSSProperties = {
  width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #d1d5db',
  borderRadius: 4, fontSize: '0.875rem', boxSizing: 'border-box',
};

function Field({ label, error, hint, children }: { label: string; error?: string; hint?: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: '0.75rem' }}>
      <label style={{ display: 'block', fontWeight: 500, fontSize: '0.875rem', marginBottom: '0.25rem' }}>
        {label}
        {hint && <span style={{ fontWeight: 400, color: '#9ca3af', marginLeft: '0.25rem' }}>— {hint}</span>}
      </label>
      {children}
      {error && <p style={{ margin: '0.25rem 0 0', color: '#dc2626', fontSize: '0.75rem' }}>{error}</p>}
    </div>
  );
}
