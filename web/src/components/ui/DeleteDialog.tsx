interface DeleteDialogProps {
  productTitle: string;
  onConfirm: () => void;
  onCancel: () => void;
  isDeleting: boolean;
}

export function DeleteDialog({ productTitle, onConfirm, onCancel, isDeleting }: DeleteDialogProps) {
  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-dialog-title"
      style={{
        position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
        display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50,
      }}
    >
      <div style={{ background: '#fff', borderRadius: 8, padding: '2rem', maxWidth: 440, width: '100%' }}>
        <h2 id="delete-dialog-title" style={{ marginTop: 0 }}>Delete product?</h2>
        <p>
          Are you sure you want to delete <strong>{productTitle}</strong>? All associated images,
          options, option values, and variants will also be removed. This cannot be undone.
        </p>
        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
          <button onClick={onCancel} disabled={isDeleting}>Cancel</button>
          <button onClick={onConfirm} disabled={isDeleting} style={{ background: '#dc2626', color: '#fff' }}>
            {isDeleting ? 'Deleting…' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}
