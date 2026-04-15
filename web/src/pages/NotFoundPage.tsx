import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/Button';

export function NotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="w-20 h-20 rounded-2xl bg-violet-50 flex items-center justify-center mb-6">
        <svg className="w-10 h-10 text-violet-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p className="text-4xl font-bold text-slate-900 mb-2">404</p>
      <p className="text-base text-slate-500 mb-8">Page not found</p>
      <Link to="/products">
        <Button variant="primary">Back to products</Button>
      </Link>
    </div>
  );
}
