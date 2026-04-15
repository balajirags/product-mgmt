import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useCreateProduct, useUpdateProduct } from '@/hooks/useProducts';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Input, Select, Textarea, Field } from '@/components/ui/Input';
import { PRODUCT_STATUSES } from '@/types/product';
import type { ProductStatus, ProductImageRequest, ProductOptionRequest, ProductVariantRequest } from '@/types/product';
import { ApiError } from '@/types/product';

interface Props { mode: 'create' | 'edit' }
interface OptionRow { title: string; values: string }
interface VariantRow { title: string; sku: string; barcode: string; manageInventory: boolean; allowBackorder: boolean }
interface FormState {
  title: string; handle: string; status: ProductStatus; description: string; subtitle: string;
  weight: string; height: string; width: string; length: string;
  thumbnail: string; externalId: string; metadata: string;
  images: string[]; options: OptionRow[]; variants: VariantRow[];
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
      externalId: existing.external_id ?? '',
      metadata: existing.metadata ? JSON.stringify(existing.metadata, null, 2) : '',
      images: existing.images.length > 0
        ? [...existing.images].sort((a, b) => a.rank - b.rank).map((i) => i.url)
        : [''],
      options: existing.options.map((o) => ({ title: o.title, values: o.values.join(', ') })),
      variants: existing.variants.map((v) => ({
        title: v.title, sku: v.sku ?? '', barcode: v.barcode ?? '',
        manageInventory: v.manage_inventory, allowBackorder: v.allow_backorder,
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
      if (url.length > 0 && url.trim() === '') errs[`image_${i}`] = 'Image URL is required';
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

    const images: ProductImageRequest[] = form.images.filter((u) => u.trim()).map((url) => ({ url }));
    const options: ProductOptionRequest[] = form.options.filter((o) => o.title.trim()).map((o) => ({
      title: o.title, values: o.values.split(',').map((v) => v.trim()).filter(Boolean),
    }));
    const variants: ProductVariantRequest[] = form.variants.map((v) => ({
      title: v.title || null, sku: v.sku || null, barcode: v.barcode || null,
      manage_inventory: v.manageInventory, allow_backorder: v.allowBackorder,
    }));
    let metadata: Record<string, unknown> | null = null;
    if (form.metadata.trim()) {
      try { metadata = JSON.parse(form.metadata) as Record<string, unknown>; } catch { /* validated above */ }
    }

    const body = {
      title: form.title.trim(), handle: form.handle.trim() || null, status: form.status,
      description: form.description.trim() || null, subtitle: form.subtitle.trim() || null,
      weight: parseDecimal(form.weight), height: parseDecimal(form.height),
      width: parseDecimal(form.width), length: parseDecimal(form.length),
      thumbnail: form.thumbnail.trim() || null, external_id: form.externalId.trim() || null,
      metadata, images: images.length > 0 ? images : null,
      options: options.length > 0 ? options : null, variants: variants.length > 0 ? variants : null,
    };

    try {
      const saved = isEdit
        ? await updateMutation.mutateAsync(body)
        : await createMutation.mutateAsync(body);
      navigate(`/products/${saved.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message);
        if (err.problem.field_errors) setErrors((prev) => ({ ...prev, ...err.problem.field_errors }));
      } else {
        setServerError('An unexpected error occurred.');
      }
    }
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  if (isEdit && loadingProduct) {
    return (
      <div className="max-w-2xl space-y-4">
        <div className="h-8 w-48 bg-slate-200 rounded-md animate-pulse" />
        <Card className="p-6 space-y-4">
          {[1,2,3,4].map((i) => <div key={i} className="h-10 bg-slate-100 rounded-lg animate-pulse" />)}
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-2xl space-y-6">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-slate-500">
        <Link to="/products" className="hover:text-violet-600 transition-colors">Products</Link>
        {isEdit && existing && (
          <>
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
            </svg>
            <Link to={`/products/${id}`} className="hover:text-violet-600 transition-colors truncate max-w-xs">{existing.title}</Link>
          </>
        )}
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
        </svg>
        <span className="text-slate-900">{isEdit ? 'Edit' : 'New product'}</span>
      </nav>

      <h1 className="text-2xl font-semibold text-slate-900">{isEdit ? 'Edit product' : 'Create product'}</h1>

      {serverError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{serverError}</div>
      )}

      <form onSubmit={(e) => { void handleSubmit(e); }} noValidate className="space-y-6">

        {/* Core info */}
        <Card className="p-6 space-y-4">
          <h2 className="text-sm font-semibold text-slate-900">Basic information</h2>
          <Field label="Title" required error={errors.title}>
            <Input value={form.title} onChange={(e) => set('title', e.target.value)} placeholder="e.g. Classic Sneaker" />
          </Field>
          <Field label="Handle" hint="Auto-generated from title if blank">
            <Input value={form.handle} onChange={(e) => set('handle', e.target.value)} placeholder="classic-sneaker" />
          </Field>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="Status">
              <Select value={form.status} onChange={(e) => set('status', e.target.value as ProductStatus)}>
                {PRODUCT_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
              </Select>
            </Field>
            <Field label="External ID">
              <Input value={form.externalId} onChange={(e) => set('externalId', e.target.value)} placeholder="EXT-001" />
            </Field>
          </div>
          <Field label="Description">
            <Textarea value={form.description} onChange={(e) => set('description', e.target.value)} rows={3} placeholder="Full product description…" />
          </Field>
          <Field label="Subtitle">
            <Input value={form.subtitle} onChange={(e) => set('subtitle', e.target.value)} placeholder="Short tagline" />
          </Field>
          <Field label="Thumbnail URL">
            <Input value={form.thumbnail} onChange={(e) => set('thumbnail', e.target.value)} placeholder="https://cdn.example.com/thumb.jpg" />
          </Field>
        </Card>

        {/* Dimensions */}
        <Card className="p-6 space-y-4">
          <h2 className="text-sm font-semibold text-slate-900">Dimensions</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {(['weight', 'height', 'width', 'length'] as const).map((dim) => (
              <Field key={dim} label={dim.charAt(0).toUpperCase() + dim.slice(1)}>
                <Input type="number" step="any" value={form[dim]} onChange={(e) => set(dim, e.target.value)} placeholder="0.00" />
              </Field>
            ))}
          </div>
        </Card>

        {/* Images */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-900">Images</h2>
            <Button type="button" variant="ghost" size="sm" onClick={() => set('images', [...form.images, ''])}>
              + Add image
            </Button>
          </div>
          {form.images.map((url, i) => (
            <div key={i} className="flex gap-2">
              <div className="flex-1">
                <Input
                  value={url}
                  onChange={(e) => { const imgs = [...form.images]; imgs[i] = e.target.value; set('images', imgs); }}
                  placeholder="https://cdn.example.com/image.jpg"
                />
                {errors[`image_${i}`] && <p className="text-xs text-red-600 mt-1">{errors[`image_${i}`]}</p>}
              </div>
              {form.images.length > 1 && (
                <Button aria-label="Remove" type="button" variant="ghost" size="sm" onClick={() => set('images', form.images.filter((_, j) => j !== i))}>
                  <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </Button>
              )}
            </div>
          ))}
        </Card>

        {/* Options */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-900">Options</h2>
            <Button type="button" variant="ghost" size="sm" onClick={() => set('options', [...form.options, { title: '', values: '' }])}>
              + Add option
            </Button>
          </div>
          {form.options.length === 0 && <p className="text-sm text-slate-400">No options added yet.</p>}
          {form.options.map((opt, i) => (
            <div key={i} className="flex gap-3 items-start">
              <div className="flex-1 space-y-1">
                <Input placeholder="Name (e.g. Size)" value={opt.title}
                  onChange={(e) => { const opts = [...form.options]; opts[i] = { ...opts[i], title: e.target.value }; set('options', opts); }} />
                {errors[`option_title_${i}`] && <p className="text-xs text-red-600">{errors[`option_title_${i}`]}</p>}
              </div>
              <div className="flex-1 space-y-1">
                <Input placeholder="Values (comma-separated)" value={opt.values}
                  onChange={(e) => { const opts = [...form.options]; opts[i] = { ...opts[i], values: e.target.value }; set('options', opts); }} />
                {errors[`option_values_${i}`] && <p className="text-xs text-red-600">{errors[`option_values_${i}`]}</p>}
              </div>
              <Button aria-label="Remove" type="button" variant="ghost" size="sm" onClick={() => set('options', form.options.filter((_, j) => j !== i))} className="mt-0.5">
                <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </Button>
            </div>
          ))}
        </Card>

        {/* Variants */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-900">Variants</h2>
            <Button type="button" variant="ghost" size="sm" onClick={() => set('variants', [...form.variants, { title: '', sku: '', barcode: '', manageInventory: false, allowBackorder: false }])}>
              + Add variant
            </Button>
          </div>
          {form.variants.length === 0 && <p className="text-sm text-slate-400">No variants added yet.</p>}
          {form.variants.map((v, i) => (
            <div key={i} className="rounded-xl border border-slate-100 bg-slate-50 p-4 space-y-3">
              <div className="grid grid-cols-3 gap-3">
                <Input placeholder="Title" value={v.title} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], title: e.target.value }; set('variants', vs); }} />
                <Input placeholder="SKU" value={v.sku} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], sku: e.target.value }; set('variants', vs); }} />
                <Input placeholder="Barcode" value={v.barcode} onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], barcode: e.target.value }; set('variants', vs); }} />
              </div>
              <div className="flex items-center gap-6">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" className="w-4 h-4 rounded accent-violet-600 cursor-pointer" checked={v.manageInventory}
                    onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], manageInventory: e.target.checked }; set('variants', vs); }} />
                  <span className="text-sm text-slate-600">Track inventory</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" className="w-4 h-4 rounded accent-violet-600 cursor-pointer" checked={v.allowBackorder}
                    onChange={(e) => { const vs = [...form.variants]; vs[i] = { ...vs[i], allowBackorder: e.target.checked }; set('variants', vs); }} />
                  <span className="text-sm text-slate-600">Allow backorder</span>
                </label>
                <Button type="button" variant="ghost" size="sm" className="ml-auto text-slate-400 hover:text-red-500"
                  onClick={() => set('variants', form.variants.filter((_, j) => j !== i))}>
                  Remove
                </Button>
              </div>
            </div>
          ))}
        </Card>

        {/* Metadata */}
        <Card className="p-6 space-y-4">
          <h2 className="text-sm font-semibold text-slate-900">Metadata</h2>
          <Field label="JSON metadata" error={errors.metadata}>
            <Textarea value={form.metadata} onChange={(e) => set('metadata', e.target.value)}
              rows={4} placeholder={'{\n  "brand": "Acme",\n  "season": "SS26"\n}'} className="font-mono text-xs" />
          </Field>
        </Card>

        {/* Actions */}
        <div className="flex items-center justify-end gap-3 pt-2">
          <Link to={isEdit ? `/products/${id}` : '/products'}>
            <Button type="button" variant="secondary">Cancel</Button>
          </Link>
          <Button type="submit" variant="primary" loading={isSubmitting}>
            {isEdit ? 'Save changes' : 'Create product'}
          </Button>
        </div>
      </form>
    </div>
  );
}
