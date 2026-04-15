import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>404 — Page not found</h1>
      <p>
        <Link to="/products">Back to products</Link>
      </p>
    </div>
  );
}
