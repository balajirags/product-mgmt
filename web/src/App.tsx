import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProductListPage } from '@/pages/ProductListPage';
import { ProductDetailPage } from '@/pages/ProductDetailPage';
import { ProductFormPage } from '@/pages/ProductFormPage';
import { BatchPage } from '@/pages/BatchPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/products" replace />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/new" element={<ProductFormPage mode="create" />} />
        <Route path="/products/batch" element={<BatchPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/products/:id/edit" element={<ProductFormPage mode="edit" />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
